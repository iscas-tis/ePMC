#include "util/Util.h"
#include "util/Cube.h"
#include "lang/ExprManager.h"

#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include <fstream>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"
#include "util/Cube.h"
#include "bdd/BDD.h"
#include "bdd/ODD.h"

#include "dp/SMT.h"
#include "dp/YicesSMT.h"

#include "theory_arith.h"
#include "theory_bitvector.h"
#include "theory_arith.h"

#include "model_checker/ActionGraph.h"
#include "model_checker/MDPSparse.h"

#include "EncodingManager.h"
#include "Cluster.h"
#include "AbsExpression.h"
#include "AbsCommand.h"
#include "AbsModel.h"
#include "AbsModelImpl.h"



using namespace lang;
using namespace dp;



namespace pred {

AbsModelImpl::AbsModelImpl(Model& __model, SMT& __smt) : AbsModel(__model,__smt) {}


// build abstract model for the first time
void AbsModelImpl::InitialExtract() {
	//computeGameBDD(vc.trueExpr());
	//ExtractPredicates();
	const Properties& properties = model.getProperties();
	ExtractProperties(properties);
	ExtractPredicates();
	/* introduce state variable for each predicate */
	Refine(preds);
}

/*********************************************************************/
//compute smaller local predicate set for each guarded transition
//side effect: store predset in local_preds
/*********************************************************************/
void AbsModelImpl::ComputeLocalPreds(const Command& gt, PredSet& predset) {
	for(Alternatives::const_iterator j = gt.getAlternatives().begin(); j!=gt.getAlternatives().end(); ++j) {
		const Alternative& assig = (**j);
		for(CVC3::ExprHashMap<CVC3::Expr>::const_iterator i=assig.getMap().begin();i!=assig.getMap().end();++i) {
			const CVC3::Expr& curr_rhs = i->first;
			const CVC3::Expr& curr_lhs = i->second;
			for(unsigned i = 0; i<preds.size();++i) {
				const Predicate& p = preds[i];
				const CVC3::Expr& e = p.getExpr();
				if(ExprManager::LvalueCompatible(e,curr_rhs) ||
				   ExprManager::LvalueCompatible(e,curr_lhs)) {
					predset.Add(p);
				}
			}
		}
	}
}

/*!
	\brief compute abstraction enumerating the transitions of the abstract MDP on the fly.
	\note
	The method follows a three-stage process.

	Overall approach
	----------------

	We maintain a set of reachable states stored in a BDD.
	  The incremental phase enlarges this set of states on the fly
	and enumerates the transitions of the model.
	It is therefore necessary to re-visit a command discovering abstract
	transitions each time. To decide which commands to visit,
	we compute the abstraction of the guard (a BDD) of each command
	and only visit a command when newly discovered abstract states
	match with the guard of the respective command.

	The final phase computes the resulting transition MTBDD
	(this cannot be done on the fly because it requires that the number of
	 non-deterministic choices is known).

	(1) setup phase: this phase is traversed once
		- compute abstraction of initial states
		- prepare transitions
			* compute the guard BDDs, clusters and symbolic clusters
	(2) incremental phase: this step is repeated until all reachable states have been found
		- fire the commands which are enabled on
	(3) final phase
		- revisit each command and cluster to compute the local transitions
		- compute the global transition relation

	Data structures
	---------------
	- reach ... states discovered so far
	- frontier ... outer onion ring (newly discovered states)
	- For each command,
		* a cluster
		* a symbolic cluster
		* its guard BDD
*/
void AbsModelImpl::Refine(const PredSet& new_preds) {
	MSG(1,"----------------------\n");
	MSG(1,"AbsModelImpl::Refine()\n");
	MSG(1,"~~~~~~~~~~~~~~~~~~~~~~\n");
	util::Statistics::abstractionTimer.Start();

	ComputeBooleanVariables();


	if(reach01.isNull())
		reach01 = dd_mgr.True();

	//abs_gt.clear();
	invar.clear();
	//retrieve invariants
	invar = model.getInvar();

	//abstract the initial states
	CVC3::Expr initial_expr = model.getInitial();
	util::Statistics::extractInitialTimer.Start();
	MSG(1,"| # reach01 %E\n", reach01.CountMinterm(preds.size()));

	initial_states = AbsExpression::abstractCoverDecomp(initial_expr, em, preds, reach01,invar);
	
	MSG(1,"Abstracting initial states %E\n", initial_states.CountMinterm(preds.size()));
	

	util::Statistics::extractInitialTimer.Stop();
	//abstract the guarded transitions
	const Commands& gts = model.getCommands();

	abs_gt.resize(gts.size());


	SymbolicAbstractModel sam(em,gts.size());
	SymbolicAbstractModel::Collection sacs;
	MSG(1,em.toString()+"\n");

	MSG(0,"Computing Guards\n");
	for(unsigned i = 0; i!= gts.size();++i) {
		const Command& gt = *gts[i];

		MSG(1,"Nr: "+util::intToString(i)+"****** "+gt.toString()+"\n");
		if(abs_gt[i]==0) {
			boost::shared_ptr<AbsCommand> acptr(new AbsCommand(*this,gt));
			abs_gt[i] = acptr;
		}
		sacs.push_back(&abs_gt[i]->sac);


		abs_gt[i]->setLogCompletion(true);
		abs_gt[i]->Update(preds,new_preds,invar, reach01);
		abs_gt[i]->setLogCompletion(false);
	}

	MSG(0,"Computing Transitions\n");

	/*! (2) incremental phase: forward reachability  */
	bdd::BDD frontier = initial_states;

	bdd::BDD everything(reach01.isNull() ? dd_mgr.True() : reach01);

	reach01    = initial_states;


	int limit = util::Database::incremental ? 500 : (int)1E+10;

	bool done(true);

	util::Timer non_incremental_timer;
	non_incremental_timer.Start();
	for(unsigned i = 0; i< gts.size();++i) {


		MSG(1,"Command %d\n",i)
		abs_gt[i]->setLogCompletion(true);

		util::Timer timer;
		timer.Start();

		abs_gt[i]->computeAbstractPost(everything,limit);

		timer.Stop();
		MSG(1,"Command %d ( with %d alt ) time %5.2f\n",i,gts[i]->getNrOfAlt(),timer.Read() * 1000);
		abs_gt[i]->setLogCompletion(false);

		if(!abs_gt[i]->isDone()) {
			MSG(0,"Command %d ( with %d alt ) not done\n",i,gts[i]->getNrOfAlt());
			done = false;
		}


	}
	non_incremental_timer.Stop();

	MSG(0,"Computing Transitions %E\n",non_incremental_timer.Read());

	if(!done && util::Database::incremental) {
		util::Timer incremental_timer;
		incremental_timer.Start();
		MSG(0,"On-the-fly Abstraction\n");

		std::vector<boost::shared_ptr<AbsCommand> > unfinished_command;

		bdd::BDD curr_trans01(dd_mgr.False());
		for(unsigned i = 0; i< gts.size();++i) {
			if(abs_gt[i]->isDone())
				curr_trans01 |= abs_gt[i]->getAbstractPost();
			else
				unfinished_command.push_back(abs_gt[i]);
		}

		while(frontier != dd_mgr.False()) {
			MSG(1,"| # frontier %d\n",(int)frontier.CountMinterm(preds.size()));

			MSG(1,"current frontier " + em.getExpr(frontier).toString() + "\n");

			bdd::BDD post (dd_mgr.False());

			/*! enumerate transitions on the fly */

			for(unsigned i = 0; i< unfinished_command.size();++i) {
				bdd::BDD command_trans01(unfinished_command[i]->computeAbstractPost(frontier,(int)1E+10));
				curr_trans01 |= command_trans01;
			}

			util::Statistics::bddTimer.Start();
			post = bdd::BDD::Post(dd_mgr,
				      pres_vars_cube,
				      next_vars_bdd,
				      frontier,
				      curr_trans01);


			frontier = post & !reach01;
			reach01 |= frontier;
			util::Statistics::bddTimer.Stop();
		}
		incremental_timer.Stop();
		MSG(1,"| # final reach %d %E\n",(int)reach01.CountMinterm(preds.size()),incremental_timer.Read());
	}

	/*! (3) final phase */

	trans2 = dd_mgr.False();
	for(unsigned i = 0; i!= gts.size();++i) {


		trans2 |= abs_gt[i]->getAbstractPost();


		MSG(1,"Size of intermediate transition BDD: trans2.size() : %d\n",trans2.size());

		util::Statistics::bddTimer.Start();
		abs_gt[i]->setLogCompletion(true);
		abs_gt[i]->Finalize(preds);
		abs_gt[i]->setLogCompletion(false);
		util::Statistics::bddTimer.Stop();
	}

	if( reach01 == initial_states ) {
		util::Statistics::bddTimer.Start();
		reach01 = bdd::BDD::StateTraversal(
			dd_mgr,
			pres_vars_bdd,
			next_vars_bdd,
			initial_states,
			trans2
			);
		util::Statistics::bddTimer.Stop();

	}

	util::Statistics::bddTimer.Start();
	trans2 = trans2 & reach01;



	trans_matrix_mtbdd = sam.computeTransitionMatrixMTBDD(sacs,false);

	const std::vector<bdd::BDD>& sam_nondet_vars_vector = sam.getNondetVars();
	nondet_vars_vector.resize(sam_nondet_vars_vector.size());

	for(unsigned i= 0; i<nondet_vars_vector.size(); ++i) {
		/** \note ugly!!! */
		nondet_vars_vector[i] = sam_nondet_vars_vector[i].toMTBDD();
	}
 	MSG(1,"guarded transitions %d computed \n",gts.size());
	util::Statistics::nr_of_preds = preds.size();

	reach = reach01.toMTBDD();

	trans = trans_matrix_mtbdd * reach;
	initial_mtbdd = initial_states.toMTBDD();

	DBG(Cudd_DebugCheck(dd_mgr.getDdManager()));

	/** needed for encoding actions */
	num_interleaving_choice_vars = EncodingManager::getChoiceVarNeed(model.getCommands().size());
	util::Statistics::bddTimer.Stop();

	MSG(0,"AbsModelImpl::Refine %d\n",2 * preds.size());
	MSG(1,"Transitions BDD: %E MTBDD (nnz): %E \n",trans2.CountMinterm(2* preds.size()), trans_matrix_mtbdd.CountMinterm(sam.getNumberOfVariables()));

	util::Statistics::abstractionTimer.Stop();
	MSG(1,"~~~~~~~~~~~~~~~~~~~~~~\n");
	MSG(1,"AbsModelImpl::Refine() (END) \n");
	MSG(1,"~~~~~~~~~~~~~~~~~~~~~~\n");
}

//compute the overapproximation of an expression
//result: a set of cubes stored in a BDD
bdd::BDD AbsModelImpl::abstractCover(const CVC3::Expr& e,
				const PredSet& predset,
				const bdd::BDD& care_set) {

	MSG(1,"AbsModelImpl::abstractCover\n");

	return AbsExpression::abstractCover(vc.andExpr(e,ExprManager::Conjunction(invar)),em,predset,care_set,invar);
}

/**
	\brief compute transition constraint
	\note The transition constraint uses variable instantiations
	      for the primed variables: R(X,X_offset,X_{offset+1}_X_{offset+2},...)

	\param offset             see above
	\param instantiation      maps state numbers to a variable instantiation (unprimed to primed variable map)
	\param exclude_transition generate constraint to express that there's no transition
	\return the transition constraint
 */

bdd::BDD AbsModelImpl::abstractInterior(const CVC3::Expr & e,
					const PredSet & preds,
					const bdd::BDD& care_set)
{
	/**
	 * under-approximation distributes over conjunction
	 * abstractInterior( e1 & ... & e_n ) = abstractInterio(e1) & ... & abstractInterior(e2)
	 */
	std::vector<CVC3::Expr> conj;
	ExprManager::getTopLevelConjuncts(e,conj);

	/**
	 * underapproximation is the dual of over-approximation
	 * abstractInterior( e ) = ! abstractCover( !e )
	 */
	bdd::BDD result(dd_mgr.True());
	for(unsigned i=0;i<conj.size();++i) {
		result &= !AbsExpression::abstractCoverDecomp(vc.notExpr(conj[i]),em, preds, care_set,invar);
	}
	return result;
}


/*********************************************************************/
// Try to infer likely predicates from guards and initial condition
// add the user-provided predicates in any case
// see also: [ComputeLocalPreds, ComputeClusters]
/*********************************************************************/
void AbsModelImpl::ExtractPredicates() {
	/* first include the user-provided predicates */
	MSG(1,"AbsModelImpl::ExtractPredicates() ... \n");
	MSG(1,"AbsModelImpl::ExtractPredicates(): user predicates\n");

 	foreach(const CVC3::Expr& e, model.getUserPreds()) {
		// check the type of the expression
		// 1. if we have a Boolean expression, we just add the expression as a predicate
		// 2. if we have a variable of non-Boolean type, we perform value-blasting,
		//    we encode the variable's domain by predicates
		//    e.g., assume we have variable "i :  [1,4]", then we get predicates i=1, i=2, i=3, i=4  
		//  NOTE: value-blasting is only supported for integer and Boolean variables

		// check the type of the expression
		CVC3::Type type = e.getType();

		switch(type.getExpr().getKind()) {
			case CVC3::INT: {
				
				// check if it is a variable
				bool isVariable(e.isVar());

				// if the expression is not a variable, report an error
				if(!isVariable)
					throw util::RuntimeError("Cannot perform value-blasting on non-variable expression "+e.toString()+ "\n");

				// if the expression is a variable, find out its value range			
				std::pair <int,int> range(0,0);

				if( !model.hasBounds(e) )
					throw util::RuntimeError("Cannot value blast since there are no bounds for variable "+e.toString()+ "\n");

				std::pair < CVC3::Expr, CVC3::Expr > boundExpr ( model.getBounds( e ) );

				// interpret the expressions that define the value range
				if(!boundExpr.first.isRational() || !boundExpr.second.isRational())
					throw util::RuntimeError("Cannot value blast: range of "+e.toString()+ " is non-constant " 
						+ "[" + boundExpr.first.toString() + "," + boundExpr.second.toString() + "] \n");								
				
				CVC3::Rational minRat(boundExpr.first.getRational()),
					maxRat(boundExpr.second.getRational());

				range.first = minRat.getInt();
				range.second = maxRat.getInt();

				// enumerate the values of the range and create corresponding predicates
				for(int i=range.first; i<=range.second; ++i) {
					Predicate p(vc.eqExpr(e,vc.ratExpr(i,1)));
					addPredicate(p,false);
				}

				break;
			}
			break;
			case CVC3::BOOLEAN: {
				Predicate p(e);
				addPredicate(p,true);
			}
			break;
			case CVC3::BITVECTOR: 
				// complain that we do not support value-blasting for bitvectors
				throw util::RuntimeError("Cannot perform value-blasting on bitvector expression "+e.toString()+ "\n");

				break;
			case CVC3::REAL:
				// complain that we do not support value-blasting for reals
				throw util::RuntimeError("Cannot perform value-blasting on real-valued expression "+e.toString()+ "\n");

				break;
		}
	}
	std::hash_set<CVC3::Expr> collection;

	if(util::Database::PredsFromInit) {
		MSG(1,"AbsModelImpl::ExtractPredicates(): initial condition\n");
		/* gather predicates from initial condition */
		ExprManager::CollectExprs(model.getInitial(),collection);
	}

	if(util::Database::PredsFromGuards) {
		MSG(1,"AbsModelImpl::ExtractPredicates(): guards\n");
		/* gather predicates from guarded transitions */
		const Commands& gts = model.getCommands();

		for(Commands::const_iterator i = gts.begin();i!=gts.end();++i) {
			assert(*i);
			ExprManager::CollectExprs((**i).getGuard(),collection);
		}
	}

	MSG(1,"AbsModelImpl::ExtractPredicates(): combination\n");
	/* put all the discovered predicates into the collection of predicates */
	for(std::hash_set<CVC3::Expr>::const_iterator i = collection.begin();i!=collection.end();++i) {
		Predicate p(*i,Predicate::REQUIRED);
		addPredicate(p,true);
	}

	MSG(1,"predicates extracted from model "+preds.toString()+"\n");
}

/*********************************************************************/
//compute properties
/*********************************************************************/
void AbsModelImpl::ExtractProperties(const Properties& prop) {
	properties.clear();

	std::hash_set<CVC3::Expr> collected_exprs ;
	for(Properties::const_iterator i = prop.begin();i!=prop.end();++i) {
		MSG(1,"Working on property "+(*i)->toString()+"\n");
		Property::CollectExprs((*i).get(),collected_exprs);
	}
	CVC3::ExprHashMap<CVC3::Expr> table;
	for(std::hash_set<CVC3::Expr>::iterator i = collected_exprs.begin();i!=collected_exprs.end();++i) {
		MSG(1,"Working on expression "+(*i).toString()+"\n");
		Predicate p(*i,Predicate::REQUIRED);
		bool neg = false;
		bool result = addPredicate(p,true);

		MSG(1,"Added predicate %d %s \n",result,p.toString().c_str());

		int pos = preds.find(p);
		if(pos==-1) {
			pos = preds.find(!p);
			if(pos == -1) {
				MSG(0,"predicate from expression "+(*i).toString() + " "+p.toString()+" not found\n");
				MSG(0,"current set of predicates:\n"+preds.toString()+"\n")

				assert(false);
			}
			else
				neg = true;
		}

		std::string name = "b"+util::intToString(pos);
		const CVC3::Expr id_expr = vc.varExpr(name,vc.boolType());
		if(neg) {
			table.insert(*i,!id_expr);
		} else {
			table.insert(*i,id_expr);
		}
	}

}

}

