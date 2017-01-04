#include "util/Util.h"
#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/Cube.h"
#include <fstream>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"

#include "TransitionConstraint.h"

#include "dp/SMT.h"
#include "dp/YicesSMT.h"
#include "bdd/BDD.h"
#include "bdd/ODD.h"


#include "model_checker/ActionGraph.h"
#include "model_checker/MDPSparse.h"



//#include "model_checker/GameGraph.h"
//#include "model_checker/Lazy.h"




#include "EncodingManager.h"
#include "Cluster.h"
#include "AbsExpression.h"
#include "AbsCommand.h"
#include "AbsModel.h"

#include "Decomposition.h"
#include "CFGCommand.h"
#include "CFG.h"



#include <cmath>
#include "pred/CEAnalysis.h"

using namespace lang;
using namespace dp;
using namespace bdd;
using namespace model_checker;

namespace pred {

/*********************************************************************/
//constructor
/*********************************************************************/
AbsModel::AbsModel (Model& __model, SMT& __smt) :
	model(__model), smt(__smt) ,
	em (dd_mgr,
	    EncodingManager::getChoiceVarNeed( model.getCommands().size() ) + 20, // nondeterminism
	    EncodingManager::getChoiceVarNeed( model.getMaxNrOfAlt() ) ,     // probabilistic choice
	    model.getMaxNrOfAlt()+1)
{
}

AbsModel& AbsModel::operator=(const AbsModel& a) {
	model = a.model;
	invar = a.invar;
	preds = a.preds;
	b     = a.b;
	dd_mgr = a.dd_mgr;
	initial_states = a.initial_states;
	abs_gt = a.abs_gt;
	properties = a.properties;
	return *this;
}

bool AbsModel::addPredicate(const Predicate &p, bool expensive_checks) {
	const int verbosity = 1;
	bool result = false;
	MSG(verbosity,"AbsModel::addPredicate: "+p.toString()+" verdict: ");
	switch(preds.Add(p,expensive_checks)) {
		case PredSet::TRIVIAL:
			MSG(verbosity,"trivial");
			break;
		case PredSet::CONTAINED:
			MSG(verbosity,"contained");
			break;
		case PredSet::COVERED:
			MSG(verbosity,"covered");
			break;
		case PredSet::ADDED:
			MSG(verbosity,"added");
			em.createStateVar(p);
			result = true;
			break;
	}
	MSG(verbosity,"\n");
	return result;

}

void AbsModel::ComputeBooleanVariables() {
	const unsigned n = preds.size();





	b.resize(std::max(dd_mgr.getNrOfVariables(),100+(model.getMaxNrOfAlt()+1)*n));
	pres_vars_vector.resize(n);
	next_vars_vector.resize(n);
	pres_vars_bdd.resize(n);
	next_vars_bdd.resize(n);

	const std::vector<StateVar*>& state_vars (em.getStateVarVector());

	if(n != state_vars.size() ) {
		MSG(0,"Predicates " +preds.toString()+"\n");;
		MSG(0,"EncodingManager " +em.toString()+"\n");;
	}

	pres_vars_cube = dd_mgr.True();
	for(unsigned pi = 0; pi<state_vars.size(); ++pi) {
		StateVar& var(*state_vars[pi]);
		BDD pres_bdd = var.getBDD();;
		MTBDD pres_mtbdd = dd_mgr.BddToMtbdd(pres_bdd);
		BDD next_bdd = var.getBDD(1);
		MTBDD next_mtbdd = dd_mgr.BddToMtbdd(next_bdd);
		pres_vars_vector[pi] = pres_mtbdd;
		next_vars_vector[pi] = next_mtbdd;
		pres_vars_bdd[pi] = pres_bdd;
		pres_vars_cube   &= pres_bdd;
		next_vars_bdd[pi] = next_bdd;
	}

	const ChoiceRange& cr (em.getProbChoiceRange());
	const std::vector<bdd::BDD>& cv (cr.getChoiceVars());
	prob_vars_vector.resize(cv.size());
	for(int i=0; i< (int)cv.size(); ++i) {
		prob_vars_vector[i] = cv[i].toMTBDD();
	}

}

bdd::BDD AbsModel::getBDD (const Predicate& pred, unsigned instance) const {
	BDD result;
	assert(instance >= 0 && instance < 2);
	try {
		result = em.getStateVar(pred).getBDD(instance);
	} catch(util::RuntimeError re) {
		throw util::RuntimeError("AbsModel::getIndex failed : "+re.toString()+ "\n");
	}
	return result;
}

int AbsModel::getIndex(const Predicate& pred, unsigned instance) const {
	int result;
	assert(instance >= 0 && instance < 2);
	try {
		result = em.getStateVar(pred).getBDDVariableIndex(instance);
	} catch(util::RuntimeError re) {
		throw util::RuntimeError("AbsModel::getIndex("+pred.toString() + "  "+ util::intToString(instance) + " failed : "+re.toString()+ "\n");
	}
	return result;
}

void AbsModel::FixDeadlocks() {
  // find states with at least one transition

	MTBDD no_deadlock (trans01_bdd.Exist(nondet_vars_vector).Exist(next_vars_vector).toMTBDD());

	// find reachable states with no transitions
	MTBDD deadlocks = reach * !no_deadlock;

	MTBDD tmp;

	if (deadlocks != dd_mgr.Constant(0)) {
		MSG(0,"Removing deadlocks!\n");
		// remove deadlocks by adding self-loops
		tmp = dd_mgr.Encode(nondet_vars_vector, (long) 0, 1);
		tmp = tmp * dd_mgr.Encode(prob_vars_vector, (long) 0, 1);
		tmp = tmp * dd_mgr.MtbddIdentity(pres_vars_vector, next_vars_vector);
		tmp = deadlocks * tmp;
		trans01 = trans01 + tmp;
		trans = trans + tmp;

	}
	nondetMask = !trans01.OrAbstract(next_vars_vector) * reach;

}

int AbsModel::getNondetIndex(unsigned index) const {
	return index;
}




lbool AbsModel::Check(const Property* prop, PredSet& new_preds, std::map<const Property*,MTBDD>& table) {
	MSG(1,"~~~~~~~~~~~~~~~~~~~~~~\n");
	MSG(1,"AbsModelImpl::Check() \n");
	MSG(1,"~~~~~~~~~~~~~~~~~~~~~~\n");

	if(typeid(*prop)==typeid(PropExpr)) {
		CVC3::Expr e (((PropExpr*)prop)->getExpr());

		goal_expr = e;


		//convert expression in program variables into expression over boolean variables
		PredSet e_preds;
		BDD  e_bdd = AbsExpression::abstractCoverDecomp(e,em,preds,reach01,invar);
		//log the result
		table[prop] = e_bdd.toMTBDD();
		return l_true;

	} else if(typeid(*prop)==typeid(PropNeg)) {
		Check(((PropNeg*)prop)->getProp(),new_preds,table);
		return l_true;
	} else if(typeid(*prop)==typeid(PropBinary)) {
		const PropBinary* pb = (PropBinary*)prop;
		Check(pb->getProp1(),new_preds,table);
		Check(pb->getProp2(),new_preds,table);
		return l_true;
	} else if(typeid(*prop)==typeid(Quant)) {
		const Quant* quant = (Quant*)prop;

		//const Quant::Bound& b  = quant->getBound();

		bool min = quant->isMin();

		Until* until = (Until*) quant->getProp();
		const Property* p1 = until->getProp1();
		const Property* p2 = until->getProp2();

		Check(p1,new_preds,table);
		Check(p2,new_preds,table);


		assert(!goal_expr.isNull());
		lbool reach_result = Reach( quant->getBound(), until, min, table[p1], table[p2], util::Database::term_crit_param, new_preds, table );

		return reach_result;
	} else if(typeid(*prop)==typeid(Next)) {
		const Next* next = (Next*)prop;
		Check(next->getProp(),new_preds,table);
	} else if(typeid(*prop)==typeid(Until)) {
		//const Until* until = (Until*)prop;
		//const Time& time = until->getTime();

		return l_undef;
	}
	return l_undef;
}


lbool AbsModel::Reach(
		const Bound& b,		
		Until* until,
		bool min,
		MTBDD b1,
       	MTBDD b2,
		double bound_on_deviation,
		PredSet& new_preds,
	    std::map<const Property*,MTBDD>& table) {


	double threshold = b.bound;

	const Time& time = until->getTime();
	b1 *= reach;
	b2 *= reach;
	MTBDD yes;
	MTBDD no;

	MTBDD maybe;

	yes = b2;
	MSG(1,"yes = %f\n",yes.CountMinterm(pres_vars_vector.size()));

		if(util::Database::MCPreprocessing) {
			BDD back_reach = BDD::StateTraversal(
					dd_mgr,
					pres_vars_bdd,
					next_vars_bdd,
					b2.GreaterThan(0),
					trans2, false);
			MSG(1,"Backwards Reachability (%E)\n", back_reach.CountMinterm(preds.size()));
			

			no = (reach01 & !back_reach).toMTBDD();
			maybe = reach * !(yes | no);

			MTBDD states;

			// add successors of maybe-states if they are no-states
			switch(util::Database::strategy) {
				case util::Database::optDiff:
				case util::Database::optDiffPath:
				case util::Database::onlyScheduler:
				case util::Database::anyDiff:
					states = maybe | BDD::Post(dd_mgr, pres_vars_cube, next_vars_bdd, maybe.GreaterThan(0), trans2).toMTBDD();
					yes *= states;
					reach = maybe;
					reach01 = reach.GreaterThan(0);
					break;
				default:
			 		states = yes | maybe | BDD::Post(dd_mgr, pres_vars_cube, next_vars_bdd, maybe.GreaterThan(0), trans2).toMTBDD();
					yes *= states;
					break;
			}

			odd.rebuild(states,pres_vars_vector);
			util::Statistics::nr_of_states = states.CountMinterm(preds.size());
		} else {
			maybe = reach * !yes;
			no = dd_mgr.Constant(0);
			util::Statistics::nr_of_states = reach01.CountMinterm(preds.size());
		}


	MSG(1,"yes = %f no = %f maybe = %f\n",
	yes.CountMinterm(pres_vars_vector.size()),no.CountMinterm(pres_vars_vector.size()),maybe.CountMinterm(pres_vars_vector.size()));

	DBG(
	printf("3===>\n");
	Cudd_DebugCheck(dd_mgr.getDdManager());
	printf("<===\n"); fflush(stdout);
	);

	switch(time.kind) {
		case Time::GE:  {     // >=t1
			double t1 = time.t1;
			MSG(1,"time bound >= %E\n",t1);
			break;
		}
		case Time::LE:  {      // <=t2
			double t2 = time.t2;
			MSG(1,"time bound <= %E\n",t2);
			CEAnalysis ceanalysis(model,*this,util::Database::CEMaxNrOfPaths);
			ActionGraph graph;
			double opt = 0;

			switch(model.getModelType()) {
				case DTMC:
				case MDP: {
					CEAnalysis ceanalysis(model,*this,util::Database::CEMaxNrOfPaths);
					double lower(0), upper = 0;
					switch(util::Database::MCTechnique) {
						default: {
							ActionGraph graph;

							Witnesses w;

							model_checker::MDPSparse sparse (
								dd_mgr,
								trans * maybe,
								pres_vars_vector,
								next_vars_vector,
								nondet_vars_vector,
								prob_vars_vector,
								num_interleaving_choice_vars,
								odd,
								util::Database::max_iters,
								util::Database::term_crit,
								util::Database::term_crit_param);


							sparse.BoundedUntil
								(dd_mgr,
								 odd,
								 initial_mtbdd,
								 yes,
								 maybe,
								 min,
								 graph,
								 w,
								 lower,
								 upper,
								 (unsigned) t2);
							bool verdict = approxEquality(upper,lower,util::Database::epsilon);

							util::Statistics::upper_bound = upper;
							util::Statistics::lower_bound = lower;

							if(verdict) {
								return l_true;
							} else
							{
								if(util::Database::DEBUG) {
									std::ofstream of("out.tgf");
									of << graph.toTGF() ;
								}

								//std::cout << "-----------" << std::endl;
								//graph.PrintDOT();
								//std::cout << "-----------" << std::endl;
								//g_upper.PrintDOT();
								//graph.PrintDOT();

								MSG(0,"Counterexample analysis \n");


								std::hash_set<CVC3::Expr> fresh_preds;


								ceanalysis.checkCE(graph, (((PropExpr*)until->getProp2())->getExpr()), upper,preds);
								fresh_preds = ceanalysis.getPreds();

								for(std::hash_set<CVC3::Expr>::const_iterator i=fresh_preds.begin();
									i!=fresh_preds.end(); ++i) {
									new_preds.Add(*i);
								}
								return l_undef;
							}
						}
					}

				}
					break;
				case CTMC:
				case CTMDP:
					opt = MDPSparse::CTBoundedUntil
						(dd_mgr,
						odd,
						pres_vars_vector,
						next_vars_vector,
						nondet_vars_vector,
						prob_vars_vector,
						num_interleaving_choice_vars,
						initial_mtbdd,
						trans,
						yes,
						maybe,
						min,
						t2,
						graph);
					break;
				default:
					break;
			}
			MSG(0,"Probability %E\n",opt);
			double diff (opt-threshold);
			bool verdict = diff < 1.0E-12;
			util::Statistics::upper_bound = opt;

			if(verdict) {
				MSG(0,"Probability bound reached: difference %E\n",diff);
				return l_true;
			} else
			{
				if(util::Database::DEBUG) {
					std::ofstream of("out.dt");
					of << graph.toString() ;
				}

				std::hash_set<CVC3::Expr> fresh_preds;

				ceanalysis.checkCE(graph, (((PropExpr*)until->getProp2())->getExpr()),opt,preds);
				fresh_preds = ceanalysis.getPreds();

				for(std::hash_set<CVC3::Expr>::const_iterator i=fresh_preds.begin();
					i!=fresh_preds.end(); ++i) {
					new_preds.Add(*i);
				}
				return l_undef;
			}

			break;
		}
		case Time::INTERVAL:  { // [t1,t2]
			double t1 = time.t1,
			       t2 = time.t2;
			MSG(1,"time bound [%E,%E]\n",t1,t2);
			break;
		}
		case Time::UNBOUNDED: // [0,infty]

			MTBDD soln;

			CEAnalysis ceanalysis(model,*this,util::Database::CEMaxNrOfPaths);
			double lower(0), upper = 0;
			switch(util::Database::MCTechnique) {
				case util::Database::sparse: default: {
					ActionGraph graph;

					Witnesses w;

					model_checker::MDPSparse sparse (
						dd_mgr,
						trans * maybe,
						pres_vars_vector,
						next_vars_vector,
						nondet_vars_vector,
						prob_vars_vector,
						num_interleaving_choice_vars,
						odd,
						util::Database::max_iters,
						util::Database::term_crit,
						util::Database::term_crit_param);

					if(util::Database::DEBUG) {
						sparse.state_info.resize(sparse.n);
						for(unsigned i=0; i<sparse.n; ++i) {
							sparse.state_info[i] = getString(i);
						}

						sparse.action_info.clear();
						foreach(boost::shared_ptr<Command> c, model.getCommands()) {
							sparse.action_info.push_back(c->toString());
						}

					}

					sparse.UnboundedUntil
						(dd_mgr,
						 odd,
						 initial_mtbdd,
						 yes,
						 maybe,
						 min,
						 graph,
						 w,
						 lower,
						 upper);

					lbool verdict;
					/* what kind of bound is to be checked */
					switch(b.kind) {
						case Bound::DK: // find out the bound, proceed according to relative difference
							if(approxEquality(upper,lower,util::Database::epsilon)) {
								verdict = l_true;  // no requirement to fulfill, result is precise enough
							} else {
								verdict = l_undef; // don't know continue
							}
							break;
						case Bound::GR:  // >  bound ... greater
							if( lower > threshold ) {
								verdict = l_true;
							} else if( upper <= threshold ) {
								verdict = l_false;
							} else {
								verdict = l_undef;
							}
							break;               			
						case Bound::GEQ: // >= bound ... greater or equal
							if( lower >= threshold || approxEquality(threshold,lower,util::Database::epsilon)) {
								verdict = l_true;
							} else if( upper < threshold ) {
								verdict = l_false;
							} else {
								verdict = l_undef;
							}							
							break;               			
						case Bound::LE:  // <  bound ... strictly less
							if( lower >= threshold ) {
								verdict = l_false;
							} else if( upper < threshold ) {
								verdict = l_true;
							} else {
								verdict = l_undef;
							}							
							break;               			
						case Bound::LEQ: // <= bound ... less or equal
							if( lower > threshold ) {
								verdict = l_false;
							} else if( upper <= threshold || approxEquality(upper,threshold,util::Database::epsilon)) {
								verdict = l_true;
							} else {
								verdict = l_undef;
							}
							break;
               			case Bound::EQ:  // =  bound ... equal
							if( approxEquality(upper,lower,util::Database::epsilon)) { // precise enough
								bool eq(approxEquality(upper,threshold,util::Database::epsilon));								
								verdict = eq ? l_true : l_false;							
							} else {
								// our only chance to say something is to refute now
								if( lower > threshold ) {
									verdict = l_false;
								} else if( upper < threshold ) {
									verdict = l_false;
								} else {
									verdict = l_undef;
								}
							}
   							break;
					}

					util::Statistics::upper_bound = upper;
					util::Statistics::lower_bound = lower;

					switch(verdict) {
						case l_true:
						case l_false:
							return verdict;
							break;
						default:
						{
							if(util::Database::DEBUG) {
								std::ofstream of("out.tgf");
								of << graph.toTGF() ;
							}

							//std::cout << "-----------" << std::endl;
							//graph.PrintDOT();
							//std::cout << "-----------" << std::endl;
							//g_upper.PrintDOT();
							//graph.PrintDOT();

							MSG(0,"Counterexample analysis \n");


							std::hash_set<CVC3::Expr> fresh_preds;


							ceanalysis.checkCE(graph, (((PropExpr*)until->getProp2())->getExpr()), upper,preds);
							fresh_preds = ceanalysis.getPreds();

							for(std::hash_set<CVC3::Expr>::const_iterator i=fresh_preds.begin();
								i!=fresh_preds.end(); ++i) {
								new_preds.Add(*i);
							}
							return l_undef;
						}

					break;
					}
				}
				case util::Database::game: {

					//model_checker::GameGraph gg;

					model_checker::MDPSparse sparse (
						dd_mgr,
						trans * maybe,
						pres_vars_vector,
						next_vars_vector,
						nondet_vars_vector,
						prob_vars_vector,
						num_interleaving_choice_vars,
						odd,
						util::Database::max_iters,
						util::Database::term_crit,
						util::Database::term_crit_param
						);

					//model_checker::RegionMap rm;
					//buildGameGraph(gg,rm,sparse,dd_mgr,odd,initial_mtbdd,trans,yes,maybe,min);

					CFG cfg(model);

					//MSG(0,"Building CFG\n")
					buildCFG(cfg, sparse, dd_mgr, odd, initial_mtbdd, trans, yes, maybe, min);

					cfg.AR(min);

					MSG(0,"Done with CFG\n");

					//model_checker::Lazy l(model,gg,rm);
					//l.run(min);

					return l_true;

					break;
				}
			}
		break;
	}
	return l_undef;
}

int AbsModel::CEGAR() {

	int return_code = 0;

	// CEGAR loop
	// step A) compute initial abstraction
	// step B) check property
	// step C) if necessary, refine

	// step D) refine
	// (1) recompute abstraction
	// (2) rebuild transition matrix
	// goto step B)

	bool done = false;

	InitialExtract();
	int counter = 0;

	bool progress = true;
	std::set<const Property*> verified;

	lbool verdict = l_undef;
	while(!done) {
		done = true;
		PredSet candidate_preds;
		for(Properties::const_iterator i =
			model.getProperties().begin();
			i!=model.getProperties().end(); ++i)
		{
			const Property* prop(i->get());

			if(verified.count(prop)>0)
				continue;

			std::map<const Property*,MTBDD> table;

			verdict = Check(prop,candidate_preds,table);
			switch(verdict) {
				case l_true:
				case l_false:
					verified.insert(prop);
					done = true;
					return_code = 0;
					break;
				default:
				break;
			}
		}
		PredSet new_preds;
		++counter;
		if (candidate_preds.size()>0) {
			progress = false;
			for(unsigned i=0;i<candidate_preds.size();++i) {
				const Predicate& p(candidate_preds[i]);
				if(addPredicate(p)) {
					progress = true;
					new_preds.Add(p);
					MSG(0,"fresh predicate: %-30s\n",p.toString().c_str());
				}
			}
			MSG(1,em.toString());
		} else progress = false;

		if(progress) {
			MSG(0,"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			MSG(0,"~~ Refinement step %2d                                                     ~~\n",counter);
			MSG(0,"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
			Refine(new_preds);
			++util::Statistics::nr_of_refine;
			done = false;
		} else {
			MSG(0,"No Progress!\n");
			done = true;
			return_code = 1;
		}

	}

	MSG(0,"Predicates\n" + preds.toString()+"\n")
	switch(verdict) {
		case l_true:
			MSG(0,"Result: property holds ( bounds precise enough )\n");					
			break;
		case l_false:
			MSG(0,"Result: property violated\n");					
			break;
		default:
			MSG(0,"Result: inconclusive\n");					
			break;
	}
	
	return return_code;
}

void AbsModel::CheckProperties() {

	PredSet new_preds;
	for(Properties::const_iterator i = model.getProperties().begin(); i!=model.getProperties().end(); ++i) {

		std::map<const Property*,MTBDD> table;
		MSG(0,"Checking property: "+(*i)->toString()+" ... \n");
		Check((*i).get(),new_preds,table);
	}


}

const PredSet& AbsModel::getPreds() const {
	return preds;
}


std::string AbsModel::getString(const Cube& c, const PredSet& preds) {
	std::string result;
	for(unsigned i=0; i<c.size();++i) {
		switch(c[i]) {
			case l_true:
				result += preds[i].toString() + "\n";
				break;
			case l_false:
				result += "~"+preds[i].toString() + "\n";
				break;
			case l_undef:
				break;
		}

	}
	return result;
}

std::string AbsModel::getString(int state, const PredSet& preds) {
	Cube c;
	odd.toCube(state,c);
	return getString(c,preds);
}


/* TODO:
	* to do without intermediate sparse matrix
*/
/*
void AbsModel::buildGameGraph(
	model_checker::GameGraph& game_graph,
	model_checker::RegionMap& rm,
	model_checker::MDPSparse& sm,
	bdd::DDManager& dd_mgr,
	bdd::ODD& odd,
	bdd::MTBDD &init,
	bdd::MTBDD &trans,
	bdd::MTBDD &yes,
	bdd::MTBDD &maybe,
	bool min)
{

	std::vector<model_checker::State> states(sm.n) ;

	std::vector<double> yes_vec;
	odd.toDoubleVector(yes, yes_vec);
	std::vector<double> init_vec;
	odd.toDoubleVector(init,init_vec);
	std::vector<double> maybe_vec;
	odd.toDoubleVector(maybe, maybe_vec);


	util::Timer t;
	t.Start();

	// create mapping of states : with unique bad state
	model_checker::State bad_state = game_graph.addState();
	game_graph.addGoal(bad_state);

	Cube c;

	std::vector<CVC3::Expr> pred_exprs;



	foreach(const Predicate& p, preds.getPreds()) {
		pred_exprs.push_back(p.getExpr());
	}

	MSG(0,"pred_exprs.size() %d\n",pred_exprs.size());

	rm[bad_state].p.push_back(goal_expr);


	for(int i=0; i<sm.n; ++i) {

		std::string classification;

		if(yes_vec[i]==1.0) {
			states[i] = bad_state;
			game_graph[bad_state].stateProp.state_nr = i;
			classification = "yes-state";
		} else {
			if(maybe_vec[i]==1.0) {
				states[i] = game_graph.addState();
				classification = "maybe-state";
			} else {
				states[i] = game_graph.addState();
				classification = "no-state";
			}

			odd.toCube(i,c);
			lang::ExprManager::addExprCube(c, pred_exprs , rm[states[i]].p);

		}
		if(init_vec[i]==1.0) {
			game_graph.addInit(states[i]);
			if(yes_vec[i]==1.0) {
				MSG(0,"one bad state is initial " + getString(i,preds)+"\n");
			}

		}

		if(util::Database::DEBUG) {
			game_graph.setAnnotation(states[i],classification + " " + util::intToString(i)+"\n" + getString(i));
		}

	}

	int l1, h1, l2, h2;
	for (int i = 0; i < sm.n; ++i) {
		if(yes_vec[i]==1.0) {
			continue;
		}

		l1 = sm.row_starts[i]; h1 = sm.row_starts[i+1];

#if 0
		std::vector<int> commands;
		std::vector<model_checker::Distribution> distributions;

#endif
		model_checker::ChoiceSet choice_set(0);

		int prev_action = -1;

		// traverse distributions
		for (int j = l1; j < h1; ++j) {

			long action = sm.actions[j];
			l2 = sm.choice_starts[j]; h2 = sm.choice_starts[j+1];
			model_checker::Distribution distr = game_graph.createDistribution(action);

			for (int k = l2; k < h2; ++k) {

				game_graph.addProbChoice(distr, states[sm.cols[k]], sm.non_zeros[k]);
			}

#if 0
			distributions.push_back(distr);
			commands.push_back(action);
#endif
			game_graph.setAnnotation(distr,model.getCommands()[action]->toString());

			if(action!=prev_action) {
				choice_set = game_graph.createChoiceSet();
				game_graph.addChoiceSet(states[i], choice_set);
				prev_action = action;
			}

			game_graph.addDistribution(choice_set,distr);
		}

#if 0
		if(distributions.size() == 0) {
			MSG(1,"AbsModel::buildGameGraph: no transitions from state %d\n",i);
		} else

			addChoiceSets(game_graph,state_expr,states[i],distributions,commands);

		{
			// create a choice set for each action
			foreach(int a, commands) {
				model_checker::ChoiceSet choice_set (game_graph.createChoiceSet());
				for(unsigned j=0;j<distributions.size();++j) {
					game_graph.addDistribution(choice_set,distributions[j]);
				}
				game_graph.addChoiceSet(states[i], choice_set);
			}
		}
#endif
	}

	t.Stop();


	MSG(0,"time: %0.3f\n",t.Read());
}
*/

void AbsModel::buildCFG(
	CFG& cfg,
	model_checker::MDPSparse& sm,
	bdd::DDManager& dd_mgr,
	bdd::ODD& odd,
	bdd::MTBDD &init,
	bdd::MTBDD &trans,
	bdd::MTBDD &yes,
	bdd::MTBDD &maybe,
	bool min)
{

	std::vector<Location> locs (sm.n) ;

	std::vector<double> yes_vec;
	odd.toDoubleVector(yes, yes_vec);
	std::vector<double> init_vec;
	odd.toDoubleVector(init,init_vec);
	std::vector<double> maybe_vec;
	odd.toDoubleVector(maybe, maybe_vec);


	util::Timer t;
	t.Start();

	std::vector<CVC3::Expr> pred_exprs;

	foreach(const Predicate& p, preds.getPreds()) {
		pred_exprs.push_back(p.getExpr());
	}

	MSG(0,"pred_exprs.size() %d\n",pred_exprs.size());


	std::vector<CVC3::Expr> char_fct;

	char_fct.push_back(goal_expr);

	// create mapping of states : with unique bad state
	MSG(0,"goal_expr: " + goal_expr.toString() + "\n")
	Location goal = cfg.addLocation(char_fct);
	cfg.setGoal(goal);

	cfg.setInitial(model.getInitial());

	Cube c;


	for(int i=0; i<sm.n; ++i) {

		char_fct.clear();

		if(yes_vec[i]==1.0) {
			locs[i] = goal;
		} else {
			odd.toCube(i,c);
			assert(i<(int)locs.size());
			lang::ExprManager::addExprCube(c, pred_exprs,char_fct);
			locs[i] = cfg.addLocation(char_fct);
		}
		if(init_vec[i]==1.0) {
			cfg.addInit(locs[i]);
			if(yes_vec[i]==1.0) {
				MSG(0,"one bad state is initial " + getString(i,preds)+"\n");
			}

		}
	}

	int l1, h1, l2, h2;
	for (int i = 0; i < sm.n; ++i) {
		if(yes_vec[i]==1.0) {
			continue;
		}

		l1 = sm.row_starts[i]; h1 = sm.row_starts[i+1];

		Action current_action;

		int prev_action = -1;

		// traverse distributions
		for (int j = l1; j < h1; ++j) {

			long action = sm.actions[j];

			if(action!=prev_action) {
				current_action = cfg.addAction(locs[i],*model.getCommands()[action]);
				prev_action = action;
			}

			std::vector<Location> succ;
			l2 = sm.choice_starts[j]; h2 = sm.choice_starts[j+1];
			for (int k = l2; k < h2; ++k) {
				succ.push_back(locs[sm.cols[k]]);
			}

			cfg.addDistribution(current_action,succ);
		}
	}

	t.Stop();


	MSG(0,"time: %0.3f\n",t.Read());
}



CVC3::Expr AbsModel::getExpr(int state) const
{
	Cube c;
	odd.toCube(state,c);
	return ExprManager::getExprCube(c,em.getPredExprs());
}

std::string AbsModel::getString(int state) const
{
	std::string result;
	Cube c;
	odd.toCube(state,c);
	for(unsigned i=0;i<c.size();++i) {
		switch(c[i]) {
			case l_true: result += preds[i].toString() + "\n";   break;
			case l_false: result += "! "+ preds[i].toString()+ "\n"; break;
			case l_undef: break;
		}
	}
	return result;
}


CVC3::Expr AbsModel::getWP(unsigned action, const std::vector<unsigned>& states) const {
	const Commands& commands (model.getCommands());
	assert(action < commands.size());
	const Command& c(*commands[action]);
	const Alternatives & alt(c.getAlternatives ());
	assert(alt.size()==states.size());
	std::vector<CVC3::Expr> conj(states.size());
	for(unsigned i=0; i<states.size();++i) {
		conj[i] = (*alt[i])(getExpr(states[i]));
	}
	/*! TODO filter out some redundancies? */
	return ExprManager::Conjunction(conj);
}

bool AbsModel::getDiff(unsigned state1, unsigned state2, unsigned& position) const {
	if(state1 == state2) return false;

	Cube c1;
	odd.toCube(state1,c1);
	Cube c2;
	odd.toCube(state2,c2);
	assert(c1.size() == c2.size());
	position = -1;
	for(unsigned i=0;i<c1.size();++i) {
		if(c1[i]!=c2[i]) {
			position = i;
		}
	}
	assert(position >= 0);
	return true;

}


void AbsModel::createInstantiationOfVariables(int time, CVC3::ExprHashMap<CVC3::Expr>& result) {
	for(HashMap<std::string,CVC3::Expr>::iterator vi= model.variables.begin();
						      vi!= model.variables.end();++vi) {
		CVC3::Expr var = vi->second;
		result[var] = ExprManager::getVariableInstance(var,time);
	}
}

} // end of namespace pred
