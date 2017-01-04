#include <algorithm>

#include "util/Util.h"
#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/Cube.h"
#include <fstream>
#include <algorithm>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "Predicate.h"
#include "PredSet.h"
#include "lang/Model.h"
#include "bdd/BDD.h"
#include "bdd/ODD.h"
#include "dp/SMT.h"
#include "dp/YicesSMT.h"

#include "model_checker/ActionGraph.h"
#include "model_checker/MDPSparse.h"
#include "EncodingManager.h"
#include "Cluster.h"
#include "AbsModel.h"
#include "AbsModelImpl.h"

#include "model_checker/ActionGraph.h"

#include "TransitionConstraint.h"


#include "CEAnalysis.h"


using namespace std;
using namespace lang;
using namespace dp;
using namespace util;
using namespace model_checker;

namespace pred {

CEAnalysis::CEAnalysis(lang::Model& __model,
		       AbsModel& __absmodel,
		       unsigned __max_nr)
               : model(__model), absmodel(__absmodel), tc(__model), max_nr(__max_nr), interpolator(SMT::getInterpolator())
{
}

/************************** counterexample analysis by state splitting ********************************/


lbool CEAnalysis::getPredicates(const model_checker::Witness& w, std::hash_set<CVC3::Expr>& exprs) {
	lbool result(l_undef);
	MSG(0,"CEAnalysis::getPredicates\n");
	/**
		Case split: do we have (potentially) intrinsic or introduced non-determinism?
	 */
	if(w.lower_action != w.upper_action) {
		/** 	\brief This could be intrinsic non-determinism => check this
			\todo Make this more efficient by caching and underapproximation
			Remember game-based people save this part.
			\
		*/
		MSG(0,"CEAnalysis::getPredicates: disjoint actions\n")

		std::vector<CVC3::Expr> f(2);
		MSG(1,"CEAnalysis::getPredicates: pivot state: %d, lower action: %d upper action: %d\n",w.state,w.lower_action,w.upper_action);
		f[0] = absmodel.getExpr(w.state);

		f[0] = vc.andExpr(f[0],model.getCommands()[w.lower_action]->getGuard());
		f[1] = model.getCommands()[w.upper_action]->getGuard();

		std::vector<CVC3::Expr> interpolants;

		result = interpolator.Interpolate(f,interpolants);

		for(unsigned i=0; i< interpolants.size();++i)
			exprs.insert(interpolants[i]);
	} else {
		/** find a place where the two states differ */
		assert(w.lower_states.size()==w.upper_states.size());
		const Commands& commands (model.getCommands());
		assert(w.lower_action < commands.size());
		const Command& c(*commands[w.lower_action]);
		const Alternatives & alt(c.getAlternatives ());
		assert(alt.size()==w.lower_states.size());

		unsigned pos;

		for(unsigned i = 0; i < w.lower_states.size(); ++i) {

			if(absmodel.getDiff(w.lower_states[i],w.upper_states[i],pos)) {
				// compute the WP of the predicate
				exprs.insert((*alt[i])(absmodel.getPreds()[pos].getExpr()));
				result = l_true;
			}
		}
		MSG(0,"CEAnalysis::getPredicates: joint action %d\n",w.lower_action);
		MSG(0,"CEanalysis::getPredicates: difference at predicate "+absmodel.preds[pos].toString()+"\n");

	}
	return result;
}

lbool CEAnalysis::getPredicates(const std::vector<CVC3::Expr>& path, const model_checker::Witness& w, std::hash_set<CVC3::Expr>& exprs) {
	lbool result(l_undef);
	MSG(0,"CEAnalysis::getPredicates\n");

	assert(w.lower_action == w.upper_action);

	/** find a place where the two states differ */
	assert(w.lower_states.size()==w.upper_states.size());
	const Commands& commands (model.getCommands());
	assert(w.lower_action < commands.size());
	const Command& c(*commands[w.lower_action]);

	assert(c.getNrOfAlt ()==w.lower_states.size());

	unsigned pos;

	std::vector<CVC3::Expr> lower, upper(path);

	for(unsigned i = 0; i < w.lower_states.size(); ++i) {
		if(absmodel.getDiff(w.lower_states[i],w.upper_states[i],pos)) {
			// determine the time frame

			int time = path.size() + i;
			// compute transition constraint
			const Alternative& alt = c[i];

			// add the expression for the state
//			tc.getTransitionConstraint(alt,path.size()-1,time,)

			lower.push_back(alt(absmodel.getExpr(w.lower_states[i])));
			upper.push_back(alt(absmodel.getExpr(w.upper_states[i])));
			result = l_true;
			break;
		}
	}

	std::vector<CVC3::Expr> f(2);
	f[0] = lang::ExprManager::Conjunction(upper);
	f[1] = lang::ExprManager::Conjunction(lower);

	std::vector<CVC3::Expr> new_path(path);

	new_path.push_back(f[0]);
	new_path.push_back(f[1]);

	MSG(0,"CEAnalysis::getPredicates: joint action %d\n",w.lower_action);
	MSG(0,"CEanalysis::getPredicates: difference at predicate "+absmodel.preds[pos].toString()+"\n");

	return getInterpolants(f,exprs);
}


std::map<std::vector<int>,CVC3::Expr> guardCache;


/************************** counterexample analysis by path analysis ********************************/

lbool CEAnalysis::checkCE(model_checker::ActionGraph& g,
			  const CVC3::Expr& bad,
			  double prob,
			  const PredSet& preds) {

	bool progress = false;

	ShortestPaths paths;
	MSG(0,"CEAnalysis::checkCE: initializing shortest paths ...");
	g.initShortestPaths(prob,paths,util::Database::CEMaxNrOfPaths);
    MSG(0,"done\n");

	util::Statistics::ceanalysisTimer.Start();
	lbool result = l_true;

	vector<double> confirmed;
	vector<double> spurious;

	unsigned int nr_of_paths_analyzed = 0;
	unsigned int max_depth = 0;

	bool skip = false;
	unsigned spurious_counter = 0;

	double confirmed_prob = 0;
	unsigned confirmed_paths = 0;
	double spurious_prob = 0;
	unsigned spurious_paths = 0;	
	unsigned precise_paths = 0;

	util::Timer stopwatch;

	stopwatch.Start();

	for(unsigned i = 0; !skip ;++i) {

		vector< LabeledTransition > path;
		double path_prob (paths.getPath(i,path));
	
		if(path_prob == 0) {
			MSG(0,"CEAnalysis::checkCE: no more paths (analyzed %d paths)\n",i-1);
			break;
		}

		/*
		if( i > 0  && i % 100 == 0) { 
			MSG(0,"Prob: %d real %E, %d spurious %E | Paths: # %d maxdepth %d\n",confirmed_paths,confirmed_prob,spurious_paths,spurious_prob,nr_of_paths_analyzed,max_depth);
		}
		*/

		MSG(1,"\n");
		MSG(1,">>> new multipgraph path (length %d prob %E )\n",path.size(),path_prob);

		max_depth = std::max(size_t(max_depth),path.size());

		double actual_path_prob = 0;
		lbool path_result;
		int feasible_paths = 0;


		switch(util::Database::strategy) {
			case util::Database::optDiff:
			case util::Database::optDiffPath:
			case util::Database::anyDiff:
			case util::Database::onlyScheduler:
				path_result = analyzePath(path,predicates,actual_path_prob);
				break;
			case util::Database::strongestEvidence: {
					
				bool precise = true;

				foreach( LabeledTransition& lt , path ) {
					precise = precise && lt.isPrecise();
				}

				if(precise) {
					path_result = l_true;
					confirmed_prob += path_prob;
					++confirmed_paths;
					++feasible_paths;
					++precise_paths;
				} else {
					vector<int> prob_choice;
					bool prob_choice_left = true;

					bool infeasible(true);
					double sum = 0;
					while(prob_choice_left) {
						int last_uncertainty;
						prob_choice_left = nextProbChoice(path,prob_choice,last_uncertainty);


						if( (nr_of_paths_analyzed >= 100 && nr_of_paths_analyzed % 100 == 0)
						 || (nr_of_paths_analyzed < 100  && nr_of_paths_analyzed % 10 == 0))  						 {	 
							stopwatch.Stop();
							double time(stopwatch.Read());
							stopwatch.Start();	
						MSG(0,"%f %0.4f %%",confirmed_paths /100.0,(confirmed_prob / prob));
						MSG(0,"%d real %d",confirmed_paths,spurious_paths);
						MSG(0,"time: %f \n",time);
						}

						++nr_of_paths_analyzed;
						std::hash_set<CVC3::Expr> new_preds;
						switch( analyzeCE(g,path,prob_choice,bad,new_preds,actual_path_prob) ) {
							case l_true:
								infeasible = false;
								confirmed_prob += actual_path_prob;		
								++confirmed_paths;
						
								break;
							case l_false:			
								++spurious_counter;									
								++spurious_paths;				
										
								spurious_prob += actual_path_prob;
						
								break;
							case l_undef:
								break;
						} // end of case
						sum += actual_path_prob;


						for(std::hash_set<CVC3::Expr>::const_iterator i=new_preds.begin();i!=new_preds.end();++i) {
							bool worth_adding(!preds.isRedundant(*i,true));								
							if(worth_adding) {
								predicates.insert(*i);
								//MSG(0,(*i).toString()+ "\n");
								prob_choice_left = false;
							} else if(util::Database::displayCEX) {
								MSG(0,"checkCE: " + (*i).toString()+ "\n");
							}

									
							progress = progress || worth_adding;
										
						}	

					} // end of while
				
					if(!infeasible) {		
						
						++feasible_paths;
					} else {
						result = l_false;
												
						// skip if UNSAT
					}
				} // end of if
			} // end of strongestEvidence block
			break;
		}
		MSG(1,"path feasible %d\n",feasible_paths);

		switch(util::Database::CEGARStrategy) {
			case 0: skip = (confirmed_prob >= prob) || (spurious_counter > util::Database::CEGARMaxSpurious);


				if(i > max_nr) {
					MSG(0,"Exceeded path limit\n");
				}
				if(confirmed_prob >= prob)
					MSG(0,"Realizable Probability higher than bound\n");
				if(spurious_counter > util::Database::CEGARMaxSpurious)
					MSG(0,"Exceeded spurious multipaths  %d",util::Database::CEGARMaxSpurious);
				break;
			case 1:	skip = (confirmed_prob >= prob); // bail out if enough probability
				break;
			case 3: skip = progress && (spurious_counter > util::Database::CEGARMaxSpurious);
				if(skip) {
					MSG(0,"Yipee! Found a predicate!\n");
				}

				break;
		} // end of switch
	}

	MSG(0,"Prob: %d real %E, %d spurious %E precise %d | Paths: # %d maxdepth %d\n",confirmed_paths,confirmed_prob,spurious_paths,spurious_prob,precise_paths,nr_of_paths_analyzed,max_depth);

	stopwatch.Stop();
	util::Statistics::ceanalysisTimer.Stop();
 	return result;
}


/*!     \brief remove last probabilstic choice alternative in path
	return true if there is one, false if there is no next choice
*/
bool CEAnalysis::nextProbChoice(const vector<LabeledTransition >& path,
		     vector<int>& prob_choice,
		     int& last_uncertainty) {

	int N = path.size();
	prob_choice.resize(N,0);
	for(int time = N-1;time>=0; --time) {
		const vector<int>& prob_choices (path[time].getProbChoices());
		int& choice = prob_choice[time];
		if(choice < (int)prob_choices.size()-1) {
			++choice;
			last_uncertainty = time;
			return true;
		}

	}
	return false;
}


lbool CEAnalysis::analyzeCE(model_checker::ActionGraph& g,
			    const vector<LabeledTransition >& path,
			    const vector<int>& prob_choice,
			    const CVC3::Expr& bad,
			    std::hash_set<CVC3::Expr>& preds,
			    double& prob ) {

	++util::Statistics::nr_of_paths;

	prob = 1.0;
	MSG(1,"Analyzing path\n");
	lbool result = l_true;

	int N = path.size();
	assert((int)prob_choice.size() ==N);

	if(N<1) {
		if(ExprManager::IsFalse(vc.andExpr(model.getInitial(),bad))) {
			prob = 0;			
			return l_false;
		} else {
			prob = 1;			
			return l_true;
		}
	}


	for(int j=0; j<N;++j) {
		if(path[j].isPrecise())
			continue;
		const Command& c (*model.getCommands()[path[j].getLabel()]);
		const std::vector<int>& prob_choices(path[j].getProbChoices());

		Alternative::Map base;

		std::vector<Alternative::Map> alt;
		c.factorize(prob_choices,base, alt);

		foreach(Alternative::Map& m,alt) {
			for(Alternative::Map::iterator i=m.begin();i!=m.end();++i) {

				preds.insert(
				i->first.getType().isBool() ?
				vc.iffExpr(i->first,i->second) :
				vc.eqExpr(i->first,i->second)
				);
			}
		}
		//MSG(0,"%d (%E)-%s-> ", path[j].getLabel(),path[j].getDiff(),path[j].isPrecise() ? "!" : "?");
	}


	tc.createVariableInstances(N+1);

	vector<vector<CVC3::Expr> > symbolic_path(N+2);

	const Commands& gts = model.getCommands();

	Timer wp_timer;
	wp_timer.Start();

#if 0
	for(int time = 0;time<N; ++time) {

		int command_nr = path[time].getLabel();
		MSG(0,"time %d command nr. %d\n", time, command_nr);
	}
#endif

	MSG(1,"======================\n");
	for(int time = 0;time<N; ++time) {

		int command_nr = path[time].getLabel();
		//get the corresponding command
		if(command_nr >= (int)gts.size()) {
			std::string message(
			"CEAnalysis::analyzeCE: invalid command number in counterexample path, time frame "
					+ util::intToString(time)
					+ " command nr: "+util::intToString(command_nr)
				        + " command range [0,"+util::intToString(gts.size()) + "]");
			throw util::RuntimeError(message);
		}


		const Command& gt = *gts[command_nr];
		unsigned a = path[time].getProbChoices()[prob_choice[time]];
		const Alternative& assign = *gt.getAlternatives()[a];
		prob *= assign.getRateAsDouble();


		if(util::Database::displayCEX) {

			
			

			MSG(0,g.getAnnotation(path[time].getFrom()));
			MSG(0,"step %d command %d (%d -> %d ) %s ", time, path[time].getLabel(), path[time].getFrom(), path[time].getTo(),path[time].isPrecise() ? "!" : "?");

			MSG(0,"block %d \n",path[time].getState());

			MSG(0,"indicator : " + absmodel.getString(path[time].getState()) + "\n");

			MSG(0,"guard " + gt.getGuard().toString() + " assignment " + assign.toString());
			if(time == N-1) {
				MSG(0,"@final: " + bad.toString());
			}
			MSG(0,"command %d prob: %E ",command_nr,assign.getRateAsDouble());
			MSG(0, gt.getGuard().toString() + " " + assign.toString());

			if(path[time+1].getFrom() == 0) {
				MSG(0,"Last transition in path!!!\n");
			}
			MSG(0,"\n");


			/* check satisfiability of transition */
			if(N > 1) {
				dp::YicesSMT yc;
				std::vector<CVC3::Expr> unsat_core;
				std::vector<CVC3::Expr> trans_vec;
				lang::ExprManager::getTopLevelConjuncts(absmodel.getExpr(path[time].getState()),trans_vec); // "present" block

				if(time == 0) 
					lang::ExprManager::getTopLevelConjuncts(model.getInitial(),trans_vec);

				if(time < N -1)
					lang::ExprManager::getTopLevelConjuncts(assign(absmodel.getExpr(path[time+1].getState())),trans_vec); // weakest precondition
				else
					lang::ExprManager::getTopLevelConjuncts(assign(bad),trans_vec); // weakest precondition
				lang::ExprManager::getTopLevelConjuncts(gt.getGuard(),trans_vec);
				switch(yc.getUnsatCore(trans_vec,unsat_core)) {
					case l_true:
						MSG(0,"transition okay\n");
						break;
					case l_false:
						MSG(0,"CEAnalysis::analyzeCE: spurious transition\n");

						foreach(CVC3::Expr& e, unsat_core) {
							MSG(0,"CEAnalysis::analyzeCE: transition unsat_core " + e.toString() + " \n");
						}
						assert(false);
						break;
					default:
						assert(false);
						break;
				}
			}

		}
		std::vector<int> choices;
		choices.push_back(prob_choice[time]);

		tc.getTransitionConstraint(assign, time, symbolic_path[time+1]);
		ExprManager::getTopLevelConjuncts(tc.getInstance(gt.getGuard(),time),symbolic_path[time+1]);

	}
	MSG(1,"======================\n");
	wp_timer.Stop();

	//! invariants
	const vector<CVC3::Expr>& invar = model.getInvar();
	for(size_t i=0;i<invar.size();++i) {

		CVC3::Expr instance = tc.getInstance(invar[i],0);
		symbolic_path[0].push_back(instance);
	}

	//! initial condition
	ExprManager::getTopLevelConjuncts(tc.getInstance(model.getInitial(),0),symbolic_path[0]);

	//! bad states

	//symbolic_path[N+1].push_back(getInstance(bad,N));
	// make sure connections between variables in bad states are made
	ExprManager::getTopLevelConjuncts(tc.getInstance(bad,N),symbolic_path[N+1]);

	result = GeneratePredicates(symbolic_path,preds);

#if 0
	std::vector<CVC3::Expr> conjuncts;
	for(size_t i = 0; i<symbolic_path.size(); ++i) {

		MSG(1,"frame %d\n",i);
		for(size_t j=0;j<symbolic_path[i].size();++j) {
			MSG(1,"TRANS " + symbolic_path[i][j].toString() + "\n");
			conjuncts.push_back(symbolic_path[i][j]);
		}
	}

	// split the problem into independent subproblems
	std::vector< std::set<CVC3::Expr> > partition;
	ExprManager::SplitExprSet(conjuncts,partition);

	// show the partitions
	//
	for(std::vector< std::set<CVC3::Expr> >::const_iterator pit=partition.begin();
						       pit!=partition.end();++pit) {
		const std::set<CVC3::Expr>& par = *pit;

		vector<vector<CVC3::Expr> > symbolic_path_red(symbolic_path.size());
		for(size_t i=0; i<symbolic_path.size(); ++i) {
			vector<CVC3::Expr>& frame = symbolic_path[i];
			for(size_t j=0; j<frame.size(); ++j) {
				CVC3::Expr entry = frame[j];
				if(par.find(entry)!=par.end()) {
					symbolic_path_red[i].push_back(entry);
				}
			}
		}

		lbool current =  GeneratePredicates(symbolic_path_red,preds);

		switch( current) {
			case l_false:
				result = l_false;
				break;
			case l_true:
				break;
			case l_undef:
				result = l_undef;
				break;
		}

		if(current == l_false) {
			MSG(1,"updates [");
			for(size_t i=0; i<symbolic_path_red.size(); ++i) {
				vector<CVC3::Expr>& frame = symbolic_path_red[i];
				if(frame.size()>0)
				for(size_t j=0; j<frame.size(); ++j) {
					CVC3::Expr entry = frame[j];
					MSG(1,","+entry.toString());
				}
			}
			MSG(1,"]\n");
		}

	}
#endif
	switch(result) {
		case l_true:
			MSG(1,"SAT !!!! prob %E length %d \n",prob, path.size());
			break;
		case l_false:
			MSG(1,"UNSAT !!!! prob %E length %d \n",prob, path.size());
			break;
		case l_undef:
			MSG(1,"DON'T KNOW !!!! prob %E length %d\n",prob, path.size());
			break;
	}
	return result;

}

lbool CEAnalysis::analyzePath(
			    const vector<LabeledTransition >& path,
			    std::hash_set<CVC3::Expr>& preds,
			    double& prob ) {

	++util::Statistics::nr_of_paths;

	prob = 1.0;

	lbool result = l_true;

	int N = path.size()-1;
	MSG(1,"CEAnalysis::analyzePath (N = %d) \n",N);

	// can we reach the state
	bool precise = true;

	if(util::Database::strategy != util::Database::onlyScheduler)
	for(int j=0; j<N-1;++j) {
		bool precise_trans( false && path[j].isPrecise());

		if(!precise_trans) {
			const Command& c (*model.getCommands()[path[j].getLabel()]);
			const std::vector<int>& prob_choices(path[j].getProbChoices());

			Alternative::Map base;

			std::vector<Alternative::Map> alt;
			c.factorize(prob_choices,base, alt);

			foreach(Alternative::Map& m,alt) {
				for(Alternative::Map::iterator i=m.begin();i!=m.end();++i) {
					preds.insert(
					i->first.getType().isBool() ?
					vc.iffExpr(i->first,i->second) :
					vc.eqExpr(i->first,i->second)
					);
				}
			}

		}



		precise &= precise_trans;
		MSG(0,"%d: block: %d ( %d ) %s-> ", path[j].getLabel(),path[j].getState(),path[j].getFrom(),path[j].isPrecise() ? "!" : "?");
	}
	MSG(0,"%d\n",path[N].getState());

	MSG(0,"\nCEAnalysis::analyzePath: %s\n",precise ? "must path" : "may path");
	/* TODO: better criterion for must path */
	precise = false;


	/* transition constraints representing the path */
	vector<vector<CVC3::Expr> > symbolic_path(N+2);
	std::vector<CVC3::Expr> conjuncts;

	if(N<0 || precise || util::Database::strategy == util::Database::onlyScheduler) {
		result = l_true;
	} else {
		tc.createVariableInstances(N+2);
		const Commands& gts = model.getCommands();

		Timer wp_timer;
		wp_timer.Start();

		for(int time = 0;time<N; ++time) {
			unsigned command_nr = path[time].getLabel();
			assert(command_nr < gts.size());
			const Command& c = *gts[command_nr];
			tc.getTransitionConstraint(c, path[time].getProbChoices(), time, symbolic_path[time+1]);
			ExprManager::getTopLevelConjuncts(tc.getInstance(c.getGuard(),time),symbolic_path[time+1]);
		}
		wp_timer.Stop();

		//! invariants
		const vector<CVC3::Expr>& invar = model.getInvar();
		for(size_t i=0;i<invar.size();++i) {

			CVC3::Expr instance = tc.getInstance(invar[i],0);
			symbolic_path[0].push_back(instance);
		}

		//! initial condition
		ExprManager::getTopLevelConjuncts(tc.getInstance(model.getInitial(),0),symbolic_path[0]);



		//! pivot state
		unsigned pivot = path[N].getState();
		CVC3::Expr pivot_expr(absmodel.getExpr(pivot));

		//MSG(0,"CEAnalysis::analyzePath: " + pivot_expr.toString()+"\n");

		ExprManager::getTopLevelConjuncts(tc.getInstance(pivot_expr,N),symbolic_path[N+1]);


		result = GeneratePredicates(symbolic_path,preds);
	}
	switch(result) {
		case l_true: { // no predicate found

				assert(N < (int)path.size());
				// take care of the witness
				const std::vector<Witness>& w(path[N].getWitnesses());
				MSG(0,"CEAnalysis::analyzePath: state %d reachable (witness size: %d)\n",path[N].getState(), w.size());
				for(unsigned i=0; i<w.size();++i ) {

					switch(util::Database::strategy) {
						case util::Database::optDiffPath:
							getPredicates(conjuncts,w[i],preds);
							break;
						case util::Database::onlyScheduler:
						case util::Database::optDiff:
						case util::Database::anyDiff:
							getPredicates(w[i],preds);
							break;
						default:
							assert(false);
							break;
					}
				}
			}
			break;
		case l_false:
			MSG(0,"CEAnalysis::analyzePath: state unreachable\n");
			break;
		case l_undef:
			break;
	}
	return result;

}

lbool CEAnalysis::getInterpolant(const std::vector<CVC3::Expr>& input, std::hash_set<CVC3::Expr>& exprs) {
	std::vector<CVC3::Expr> interpolants;
	lbool result = interpolator.Interpolate(input,interpolants);
	switch(result) {
		case l_false: // UNSAT

		// Now we need to transform the interpolant into valid
		// predicates by replacing the instantiated variables
		// with program variables
		for(size_t i = 0; i<interpolants.size();++i) {
			if(interpolants[i].isNull()) {
				MSG(0,"interpolation failed\n");
			} else if (interpolants[i].isBoolConst()) {
				continue;
			}
			MSG(1,"CEAnalysis::getInterpolants: interpolant " + interpolants[i].toString() + "\n");
			CVC3::Expr interpolant = interpolants[i].substExpr(tc.getOriginalFromInstance());
			MSG(1,"CEAnalysis::getInterpolants: Interpolant " + interpolant.toString() + "\n");
			#if 1
			ExprManager::CollectExprs(interpolant,exprs);
			#else
			exprs.insert(interpolant);
			#endif

			if(util::Database::displayCEX) {

				MSG(0,"interpolant # " + util::intToString(i) + " : " + interpolant.toString()+"\n");
			}
		}

		break;
		default: // SAT
			break;
	}
	return result;
}

lbool CEAnalysis::getInterpolants(const vector<CVC3::Expr>& f, std::hash_set<CVC3::Expr>& exprs) {
	lbool result = l_undef;

	vector<CVC3::Expr> input;
	vector<CVC3::Expr> unsat_core;
	vector<CVC3::Expr> expr_vec;

	vector<vector<CVC3::Expr> > conjuncts(f.size());
	for(unsigned i=0;i<f.size();++i) {

		ExprManager::getTopLevelConjuncts(f[i],conjuncts[i]);
		for(unsigned j=0;j<conjuncts[i].size();++j)
			expr_vec.push_back(conjuncts[i][j]);

	}

	if(expr_vec.size() == 1) {
		return ExprManager::IsFalse(expr_vec[0]) ? l_false: l_true;
	}

	vector<CVC3::Expr> new_f;

	dp::YicesSMT yl;

	result = yl.getUnsatCore(expr_vec,unsat_core);
	//shrink the formula
	switch(result) {
		case l_false: {
		std::hash_set<CVC3::Expr> expr_set;

		for(size_t i=0;i<unsat_core.size();++i) {
			expr_set.insert(unsat_core[i]);
		}

		if(util::Database::displayCEX) {
			MSG(0,"===\n");
			foreach(const CVC3::Expr& e, unsat_core) {
				MSG(0,"CEAnalysis::getInterpolants unsat_core: " + e.toString() + "\n");
			}
			MSG(0,"===\n");
		}

	
		for(size_t i = 0; i<conjuncts.size(); ++i) {
				MSG(2,"f frame %d\n",i);
				vector<CVC3::Expr> core_path;
				vector<CVC3::Expr> off_core_path;
				ExprManager::Triage(conjuncts[i],expr_set,core_path,off_core_path);


				CVC3::Expr core(ExprManager::Conjunction(core_path));
				input.push_back(core);

				new_f.push_back(ExprManager::Conjunction(off_core_path));
		}

		getInterpolant(input,exprs);

		// get the interpolants for the f with the current unsat core removed
		getInterpolants(new_f, exprs);
	}
	break;
	default:
		break;
	}
	return result;
}

CVC3::ExprHashMap<lbool> result_cache;

lbool CEAnalysis::GeneratePredicates(
			const vector<vector<CVC3::Expr> >& symbolic_path,
			std::hash_set<CVC3::Expr>& preds) {


	lbool result = l_undef;

	vector<CVC3::Expr> f;

	for(size_t i = 0; i<symbolic_path.size(); ++i) {
		MSG(2,"f frame %d\n",i);
		if(symbolic_path[i].size() > 0){
			f.push_back(ExprManager::Conjunction(symbolic_path[i]));
		}
	}


	Timer proof_timer;
	proof_timer.Start();

	CVC3::Expr formula = ExprManager::Conjunction(f);

	vector<CVC3::Expr> assumptions_used;

	/*! check if the path is realizable or not
	    if it is, create a corresponding concrete path and print it
	 */
	CVC3::ExprHashMap<lbool>::iterator rit = result_cache.find(formula);	
	bool from_cache ( false  && rit !=result_cache.end() );	
	if( from_cache ) {
		result = rit->second;
		
	}
	else
	{


		/*vector<CVC3::Expr> unsat_core;
		vector<CVC3::Expr> expr_vec;
		for(size_t i = 0; i<symbolic_path.size(); ++i) {
			for(size_t j = 0; j<symbolic_path[i].size(); ++j) {
				expr_vec.(symbolic_path[i][j]);
			}
		}*/
		result = getInterpolants(f, preds) ;

		result_cache[formula] = result ;
	}

#if 0
 		switch(result) {
			case l_true:
				MSG(1,"SAT!!! " +formula.toString() + " " + ( from_cache ? "(from cache)" : " ") + "\n");
								
				MSG(1,formula.toString()+"\n");
				if (ExprManager::IsFalse(formula))
					throw RuntimeError("wrong result UNSAT for " + formula.toString() + " ");				
				break;
			case l_false:
				MSG(1,"UNSAT!!! "+ formula.toString() + " " + ( from_cache ? "(from cache)" : " ") + "\n");
				if (!ExprManager::IsFalse(formula))
					throw RuntimeError("wrong result UNSAT for " + formula.toString() + " ");							
					break;
			case l_undef:
				MSG(1,"WARNING: SMT solver failed\n");
				break;
		}
#endif
	return result;
}

}
