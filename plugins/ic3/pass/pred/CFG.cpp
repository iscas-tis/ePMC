/*
 * CFG.cpp
 *
 *  Created on: May 11, 2009
 *      Author: bwachter
 */

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
#include "dp/MathSat.h"

#include "model_checker/ActionGraph.h"
#include "model_checker/MDPSparse.h"
#include "EncodingManager.h"
#include "Cluster.h"

#include "AbsExpression.h"
#include "AbsCommand.h"

#include "TransitionConstraint.h"

#include "AbsModel.h"
#include "AbsModelImpl.h"

#include "Decomposition.h"
#include "CFGCommand.h"
#include "CFG.h"

#include <boost/graph/strong_components.hpp>
#include <boost/graph/depth_first_search.hpp>

namespace pred {

CFG::CFG(lang::Model& __model) :
	model(__model),
	tc(__model),
	dfs_order(graph),
	reverse_dfs_order(graph),
	em (dd_mgr,
	    EncodingManager::getChoiceVarNeed( model.getCommands().size() ) + 20, // nondeterminism
	    EncodingManager::getChoiceVarNeed( model.getMaxNrOfAlt() ) ,     // probabilistic choice
	    model.getMaxNrOfAlt()+1)
{
	frexp(util::Database::term_crit_param,&precision);
}

/**
 * \return index of the created location
 */
Location CFG::addLocation(const std::vector<CVC3::Expr>& e) {
	boost::shared_ptr<CFGLocation> cfgl(new CFGLocation());

	locations.push_back(cfgl);
	unsigned id = locations.size()-1;

	cfgl->e = e;
	cfgl->b.reach = cfgl->b.reach_prime = em.getDDManager().True();

	CFGNodeProp prop(CFGNodeProp::Location,id);

	Location result = add_vertex(prop, graph);

	locs.push_back(result);
	return result;
}

/**
 * \return index of the created action
 */
Action CFG::addAction  (Location loc, lang::Command& c) {
	assert(graph[loc].isLocation());
	boost::shared_ptr<lang::Command> cptr(&c);
	commands.push_back(cptr);
	unsigned id = commands.size() - 1;
	CFGNodeProp prop(CFGNodeProp::Action,id);
	Location result = add_vertex(prop, graph);
	add_edge(loc, result, graph);
	return result;
}

/**
 * \return index of the created distr
 */
Distribution CFG::addDistribution   (Action a, std::vector<Location>& succ) {
	assert(graph[a].isAction());
	matrices.resize(matrices.size()+1);
	unsigned id = matrices.size()-1;
	CFGNodeProp prop(CFGNodeProp::Distr,id);
	Location result = add_vertex(prop, graph);
	add_edge(a, result, graph);
	std::vector<CVC3::Expr> invar;
	const std::vector<CVC3::Expr>& v(locations[graph[getLocation(a)].id]->e);
	invar.insert(invar.end(),v.begin(),v.end());
	lang::Command& command(*commands[graph[a].id]);
	unsigned i = 0;
	foreach(Location& loc, succ) {
		const std::vector<CVC3::Expr>& v1(locations[graph[loc].id]->e);
		std::pair<edge_descriptor,bool> p( add_edge(result,loc,graph));
		graph[p.first] = i;
		foreach(const CVC3::Expr& expr, v1) {
			invar.push_back(command.WP(expr,i));
		}
		++i;
	}
	boost::shared_ptr<CFGCommand> cfgc(new CFGCommand(command,em, invar));
	cfg_commands.push_back(cfgc);
	return result;
}

void CFG::initialize(void) {
	computeOrder();

	/* initializing predicates */
	foreach(const Location& loc, locs) {
		CFGNodeProp& prop(graph[loc]);
		CFGLocation& cfg_loc(*locations[prop.id]);

		std::hash_set<CVC3::Expr> collection;
		for (adjacency_iter_pair itc(adjacent_vertices(loc, graph)); itc.first!= itc.second; ++itc.first) {
			Action action(*itc.first);
			const lang::Command& command(getCommand(action));
			lang::ExprManager::CollectExprs(command.getGuard(),collection);
		}
		for(std::hash_set<CVC3::Expr>::const_iterator it = collection.begin(); it!=collection.end(); ++it) {
			const CVC3::Expr& e(*it);
			addPredicate(loc,e);
		}
	}


	foreach(const Location& loc, locs) {
		CFGNodeProp& prop(graph[loc]);
		CFGLocation& cfg_loc(*locations[prop.id]);

		/* if initial location recompute abstraction of initial states */
		if(init.find(loc)!=init.end()) {
			/* reconstruct abstraction of initial states with new predicate */
			cfg_loc.b.init = AbsExpression::abstractCoverDecomp(initial_expr,em,cfg_loc.b.preds,cfg_loc.b.reach,cfg_loc.e);;
		}

		for (adjacency_iter_pair itc(adjacent_vertices(loc, graph)); itc.first!= itc.second; ++itc.first) {
			Action command_vertex(*itc.first);
			assert(graph[command_vertex].isAction());

			std::vector<CVC3::Expr> invar;
			for (adjacency_iter_pair itd(adjacent_vertices(command_vertex, graph)); itd.first!= itd.second; ++itd.first) {

				Distribution distr(*itd.first);
				CFGCommand& cfg_command(getCFGCommand(distr));

				/* recompute the abstract transitions */
				std::vector<const PredSet*>& predset_vec(cfg_command.predset_vec);
				predset_vec.clear();
				predset_vec.reserve(out_degree(command_vertex, graph)+1);
				predset_vec.push_back(&cfg_loc.b.preds);

				cfg_command.odd_vec.push_back(&cfg_loc.b.odd);


				/* gather the predicate sets */
				for (out_edge_iter_pair its(out_edges(distr, graph)); its.first!= its.second; ++its.first) {
					vertex_descriptor succ_vertex(target(*its.first, graph));
					assert(graph[succ_vertex].isLocation());
					assert(graph[succ_vertex].id < locations.size());
					CFGLocation& loc(*locations[graph[succ_vertex].id]);
					predset_vec.push_back(&loc.b.preds);
					cfg_command.odd_vec.push_back(&loc.b.odd);
				}
				computeAbstractTransitions(distr);
			}
		}
	}

	std::set<Location> workset;
	workset.insert(init.begin(),init.end());
	qualReach(workset,true);


	foreach(const Location& loc, locs) {
		rebuildODD(loc);
	}

	foreach(const Location& loc, locs) {
		CFGNodeProp& prop(graph[loc]);
		CFGLocation& cfg_loc(*locations[prop.id]);
		for (out_edge_iter_pair itc(out_edges(loc, graph)); itc.first!= itc.second; ++itc.first) {
			Action command_vertex(target(*itc.first, graph));
			assert(graph[command_vertex].isAction());

			std::vector<CVC3::Expr> invar;
			for (out_edge_iter_pair itd(out_edges(command_vertex, graph)); itd.first!= itd.second; ++itd.first) {
					Distribution distr_vertex(target(*itd.first, graph));
					CFGCommand& cfg_command(getCFGCommand(distr_vertex));
					cfg_command.sac.constrainTransitions(cfg_loc.b.reach);
					//MSG(0,"CFG::initialize: " + cfg_command.command.toString() + "\n");
					cfg_command.sac.toMatrix(cfg_command.odd_vec, matrices[graph[distr_vertex].id]);
			}
		}
	}

	foreach(const Location& loc, locs) {
		resetValIter(loc);
	}
}


void CFG::computeAbstractTransitions(Distribution distr) {
	assert(graph[distr].isDistr());

	Action action(getAction(distr));
	Location loc(getLocation(action));

	/* get the CFGCommand */
	CFGNodeProp& prop(graph[loc]);
	CFGLocation& cfg_loc(*locations[prop.id]);

	CFGCommand& cfg_command(getCFGCommand(distr));

	/* run CFG Command to compute the transitions as a BDD */
	cfg_command.Update();
	/* compute abstract transitions */

	cfg_command.computeAbstractPost(cfg_loc.b.reach);
	cfg_command.Finalize();
}

void CFG::rebuildODD(Location loc) {
	CFGLocation& cfg_loc(getCFGLocation(loc));
	cfg_loc.b.odd.rebuild(cfg_loc.b.reach,cfg_loc.b.variables);


	if(!cfg_loc.b.init.isNull()) {

		cfg_loc.b.odd.toBoolVector(cfg_loc.b.init,cfg_loc.b.init_vec);
	}

#if 0
		if(cfg_loc.b.init.isFalse()) {
			assert(false);
		}

		assert(!cfg_loc.b.init.isFalse());
		assert(cfg_loc.b.init <= cfg_loc.b.reach);
		bool b(false);
		for(unsigned i=0;i<cfg_loc.b.odd.getNumOfStates();++i) {
			if(cfg_loc.b.init_vec[i])
				MSG(0,"CFG::")
			b |= cfg_loc.b.init_vec[i];
		}
		assert(b);

	}
	/** debugging */
	//assert(checkLocation(loc));
#endif
}

void CFG::resetValIter(Location loc) {
	CFGLocation& cfg_loc(getCFGLocation(loc));
	unsigned n(cfg_loc.b.odd.getNumOfStates());
	graph[loc].resizeValIterResult(n);
	for (adjacency_iter_pair itc(adjacent_vertices(loc, graph)); itc.first!= itc.second; ++itc.first) {
		Action action(*itc.first);
		graph[action].resizeValIterResult(n);
		for (adjacency_iter_pair itd(adjacent_vertices(action, graph)); itd.first!= itd.second; ++itd.first) {
			Distribution distr(*itd.first);
			graph[distr].resizeValIterResult(n);
		}
	}
}



void CFG::qualReach(std::set<Location>& workset, bool forward) {

	if(forward)
		getForwardReach(workset);
	else
		getBackwardReach(workset);

	std::set<Location> closure(workset);

	foreach(const Location& loc, workset) {
		CFGLocation& cfg_loc(getCFGLocation(loc));
		if(init.count(loc)==0)
			cfg_loc.b.reach_prime = dd_mgr.False();
		else
			cfg_loc.b.reach_prime = cfg_loc.b.init;
	}

	/* do fixpoint iteration */
	while(!workset.empty()) {
		Location loc = *workset.begin();
		assert(graph[loc].isLocation());
		workset.erase(loc);

		assert(closure.count(loc)>0);


		CFGLocation& cfg_loc(getCFGLocation(loc));
		bdd::BDD reach_new(forward ? qualForwardReach(loc) : qualBackwardReach(loc));

		if(!cfg_loc.b.init.isNull()) {
			reach_new |= cfg_loc.b.init;
		}

		if(cfg_loc.b.reach_prime != reach_new) {
			if(forward)
				getSuccessorLocations(loc,workset);
			else
				getPredecessorLocations(loc,workset);
		}
		cfg_loc.b.reach_prime = reach_new;
	}

	/* find out what parts of the model have changed
	 * (check only for those that can potentially have changes)
	 * */

	/* check which ones have actually changed */
	foreach(const Location& loc, closure) {
		CFGLocation& cfg_loc(getCFGLocation(loc));
		if(		cfg_loc.b.reach_prime != cfg_loc.b.reach ||
				cfg_loc.b.reach_prime.CountMinterm(cfg_loc.b.variables.size())!=cfg_loc.b.odd.getNumOfStates() ||
				cfg_loc.b.variables.size() != cfg_loc.b.odd.getVarIndices().size()
				) {
			workset.insert(loc);
		}
		cfg_loc.b.reach = cfg_loc.b.reach_prime;
	}
}

bdd::BDD CFG::qualForwardReach(const Location& loc) {
	assert(graph[loc].isLocation());
	bdd::BDD result(dd_mgr.False());
	CFGLocation& cfg_loc(getCFGLocation(loc));

	// traverse all predecessors
	for (in_edge_iter_pair itd(in_edges(loc, graph)); itd.first!= itd.second; ++itd.first) {
		unsigned branch(graph[*itd.first]);
		vertex_descriptor distr(source(*itd.first, graph));
		CFGCommand& cfg_command(getCFGCommand(distr));
		Action action(getAction(distr));
		Location pred(getLocation(action));
		CFGLocation& pred_loc(getCFGLocation(pred));
		result |= cfg_command.sac.getPost(pred_loc.b.reach_prime,branch);

		/** debugging */
#if 0
				bdd::ODD2 odd;
				odd.rebuild(result,cfg_loc.b.variables);
				// go through the blocks and check consistency of each
				int n = odd.getNumOfStates();
				Cube c;
				for(unsigned i=0; i<n; ++i) {
					Block b(loc,i);
					CVC3::Expr e;

					odd.toCube(i,c);
					std::vector<CVC3::Expr> exprs(cfg_loc.b.preds.size());
					for(unsigned i= 0; i < cfg_loc.b.variables.size();++i) {
						const StateVar sv(em.getStateVarFromBDDIndex(cfg_loc.b.variables[i]));
						exprs[i] = sv.getPredicate().getExpr();
					}

					e = lang::ExprManager::getExprCube(c,exprs);

					if(lang::ExprManager::IsFalse(e)) {
						const lang::Command& command(getCommand(action));
						MSG(0,"CFG::qualForwardReach:" + command.toString() + "\n");
						MSG(0,"CFG::qualReach location %d (# blocks %d) distr %d branch %d\n",loc,n,distr,branch);
						MSG(0,odd.toString()+"\n");
						MSG(0,"CFG::qualReach block "+e.toString()+"\n");

						dp::MathSat mathsat;
						std::vector<CVC3::Expr> vec;
						vec.push_back(e);
						std::vector<CVC3::Expr> core;
						mathsat.getUnsatCore(vec, core );

						foreach(CVC3::Expr c, core)
						MSG(0,"unsatisfiable core " + c.toString()+"\n");
						assert(false);
					}
				}

				/*************/
#endif



	}
	return result;
}

bdd::BDD CFG::qualBackwardReach(const Location& loc) {
	assert(graph[loc].isLocation());
	bdd::BDD result(dd_mgr.False());
	// traverse all predecessors

	for (out_edge_iter_pair itc(out_edges(loc, graph)); itc.first!= itc.second; ++itc.first) {
		Action action(target(*itc.first, graph));
		assert(graph[action].isAction());
		std::vector<CVC3::Expr> invar;
		for (out_edge_iter_pair itd(out_edges(action, graph)); itd.first!= itd.second; ++itd.first) {
				Distribution distr(target(*itd.first, graph));
				assert(graph[distr].isDistr());
				const CFGCommand& cfg_command(getCFGCommand(distr));
				for (out_edge_iter_pair its(out_edges(distr, graph)); its.first!= its.second; ++its.first) {
					unsigned branch(graph[*its.first]);
					Location succ(target(*its.first, graph));
					const bdd::BDD in(getCFGLocation(succ).b.reach_prime);
					result |= cfg_command.sac.getPre(in,branch);
				}
		}
	}
	return result;
}

void CFG::ARChecker(bool min) const {
	// check if all symbolic abstract commands are initialized
	foreach(boost::shared_ptr<CFGCommand> cfg_command, cfg_commands) {
		assert(!cfg_command->sac.getTransitions().isNull());
	}


}


bool CFG::insertRefinement(Refinement& refinement) {

	bool done = true;

	foreach(RefinementPair& p, refinement) {
		Location loc(p.first);
		std::set<CVC3::Expr> preds(p.second);
		bool change (false);
		foreach(CVC3::Expr e, preds) {

			MSG(1,"CFG::insertRefinement: " + e.toString());
			bool added (addPredicate(loc,e));
			change = change || added;
			if(!added) p.second.erase(e);
			MSG(1,"%s\n",added ? "added" : "not added");

		}
		if(change && init.find(loc)!=init.end()) {
			CFGLocation& cfg_loc(getCFGLocation(loc));
			/* reconstruct abstraction of initial states with new predicate */
			cfg_loc.b.init = AbsExpression::abstractCoverDecomp(initial_expr,em,cfg_loc.b.preds,cfg_loc.b.reach,cfg_loc.e);

			MSG(0,"CFG::inserRefinement: intial location %d\n",loc);

			assert( cfg_loc.b.init <= cfg_loc.b.reach);
			if(! ( cfg_loc.b.init <= cfg_loc.b.reach) ) {
				MSG(0,"CFG::inserRefinement: " + (em.getExpr2(cfg_loc.b.init)).toString() + "\n");
				assert(false);
			}
		}
		done = done && !change;
	}
	return done;
}

class Collector :public boost::default_dfs_visitor {
  std::set<Location>& collection;
public:
  Collector(std::set<Location>& s) : collection(s) {}

  void discover_vertex(vertex_descriptor u, const Graph & g) const
  {
    if(g[u].isLocation())
    	collection.insert(u);
  }
  void discover_vertex(vertex_descriptor u, const boost::reverse_graph<Graph,Graph&>& g) const
  {
    if(g[u].isLocation())
    	collection.insert(u);
  }

};

void CFG::getForwardReach(std::set<Location>& locs) {

	std::set<Location> result;
	Collector coll(result);

	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
	      graph[*vp.first].color = boost::white_color;
	      coll.initialize_vertex(*vp.first, graph);
	 }

	foreach(const Location& start, locs)
		depth_first_visit(graph, start, coll, get(&CFGNodeProp::color, graph));
	locs = result;
}

void CFG::getBackwardReach(std::set<Location>& locs) {
	std::set<Location> result;
	Collector coll(result);
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
	      graph[*vp.first].color = boost::white_color;
	      coll.initialize_vertex(*vp.first, graph);
	}
	foreach(const Location& start, locs)
		depth_first_visit(boost::make_reverse_graph(graph), start, coll, get(&CFGNodeProp::color, graph));
	locs = result;
}

class LocationOrderVisitor :public boost::default_dfs_visitor {
  Graph& graph;
  unsigned& time;
public:
  LocationOrderVisitor(Graph& __graph, unsigned& __t) : graph(__graph), time(__t) {}

  void discover_vertex(vertex_descriptor u, const boost::reverse_graph<Graph,Graph&>& g) const
  {
    graph[u].time = time++;
  }

};


void CFG::computeOrder() {
	/*
	unsigned nrOfSCCs = strong_components(graph, get(&CFGNodeProp::scc_index, graph),
			root_map(get(&CFGNodeProp::root, graph)). discover_time_map(get(
					&CFGNodeProp::time, graph)). color_map(get(
					&CFGNodeProp::color, graph)));
					*/
	unsigned counter = 0;
	LocationOrderVisitor vis(graph,counter);
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
	      graph[*vp.first].color = boost::white_color;
	      graph[*vp.first].time = 0;
	}
	depth_first_visit(boost::make_reverse_graph(graph), goal, vis, get(&CFGNodeProp::color, graph));


	sort(locs.begin(),locs.end(),dfs_order);
}


void CFG::getDistributions(Location loc, std::set<Distribution>& result) {
	for (adjacency_iter_pair itc(adjacent_vertices(loc, graph)); itc.first!= itc.second; ++itc.first) {
		Action action(*itc.first);
		for (adjacency_iter_pair itd(adjacent_vertices(action, graph)); itd.first!= itd.second; ++itd.first) {
			Distribution distr(*itd.first);
			result.insert(distr);
		}
	}
}

void CFG::getPredecessors(vertex_descriptor v, std::set<vertex_descriptor>& result) {
	for (in_edge_iter_pair itd(in_edges(v, graph)); itd.first!= itd.second; ++itd.first) {
		vertex_descriptor v1(source(*itd.first, graph));
		result.insert(v1);
	}
}


void CFG::getSuccessorLocations(Location loc, std::set<Location>& result) {
	assert(graph[loc].isLocation());
	for (adjacency_iter_pair ita(adjacent_vertices(loc, graph)); ita.first!= ita.second; ++ita.first) {
		Action action(*ita.first);
		for (adjacency_iter_pair itd(adjacent_vertices(action, graph)); itd.first!= itd.second; ++itd.first) {
			Distribution distr(*itd.first);
			for(adjacency_iter_pair itl(adjacent_vertices(distr, graph)); itl.first!= itl.second; ++itl.first) {
				Location succ(*itl.first);
				assert(graph[succ].isLocation());
				//if(loc != succ)
					result.insert(succ);
			}
		}
	}
}

void CFG::getPredecessorLocations(Location loc, std::set<Location>& result) {
	assert(graph[loc].isLocation());
	for (in_edge_iter_pair itd(in_edges(loc, graph)); itd.first!= itd.second; ++itd.first) {
		vertex_descriptor distr_vertex(source(*itd.first, graph));
		for (in_edge_iter_pair ita(in_edges(distr_vertex, graph));
	             ita.first!= ita.second; ++ita.first) {
			vertex_descriptor action_vertex(source(*ita.first, graph));
			for (in_edge_iter_pair itl(in_edges(action_vertex, graph));
		             itl.first!= itl.second; ++itl.first) {
 				Location predecessor(source(*itl.first,graph));
 				//if(loc != predecessor) {
 					result.insert(predecessor);
			}
		}
	}
}

/*-----------------------------------------------------------------------------*/
/*                            value iteration                                  */
/*-----------------------------------------------------------------------------*/
void CFG::valIter(bool min) {

	// initialize goal with value one
	for(unsigned i=0; i<graph[goal].lb.val.size();++i) {
		graph[goal].resizeValIterResult(1,1.0);
	}
#if 0
	foreach(const Location& loc, locs) {
//		valIterInit(true,min,loc);
//		valIterInit(false,min,loc);
		valIterCheck(true,min,loc);
		valIterCheck(false,min,loc);
	}
#endif
	MSG(0,"CFG::valIter lower %s", min ? "min" : "max")
	valIter(true,min);
	MSG(0,"CFG::valIter upper %s", min ? "min" : "max")
	valIter(false,min);
}

void CFG::printValIterResults() {
	foreach(const Location& loc, init) {
		const CFGLocation& cfg_loc(getCFGLocation(loc));
		for(unsigned i=0; i<graph[loc].lb.val.size();++i) {

			/** debugging */
			if(cfg_loc.b.init_vec.size() != graph[loc].lb.val.size()) {
				MSG(0, "CFG::printValIterResults: %d %d\n",cfg_loc.b.init_vec.size(), graph[loc].lb.val.size());

			}

			assert(cfg_loc.b.init_vec.size() == graph[loc].lb.val.size());

			if(cfg_loc.b.init_vec[i])
			MSG(0,"Block %d [%E %E]", i, graph[loc].lb.val[i], graph[loc].ub.val[i])
		}
	}
}

void CFG::valIter(bool lower, bool min) {
	MSG(0,"CFG::valIter: start \n");
	unsigned iters(0);
	util::Timer t;
	t.Start();
	bool done = false;
	while(!done && iters< 10000) {
		done = true;
		/*
		std::string filen("valiter"+util::intToString(iters)+(lower ? "lower" : "upper") +".gdl");
				std::ofstream file(filen.c_str());
				aiSee(file);
		 */
		foreach(const Location& loc, locs) {
			//valIterCheck(lower,min,loc);
			if(!valIter(lower,min,loc)) {
				done = false;
			}
			//valIterCheck(lower,min,loc);

		}
		++iters;
	}
	t.Stop();
	MSG(0,"CFG::valIter %d %5.2f\n",iters,t.Read() * 1000);
}

/*
 * check the probabilities and the scheduler
 */
bool CFG::valIterCheck(bool lower, bool min, Location loc) {
	CFGNodeProp& prop(graph[loc]);
	ValIterResult& v(lower ? prop.lb : prop.ub);
	unsigned nr_of_blocks(v.val.size());
	// evaluate distributions, and actions
	for (out_edge_iter_pair itc(out_edges(loc, graph)); itc.first!= itc.second; ++itc.first) {
		vertex_descriptor action(target(*itc.first, graph));
		/******************
		 * distributions  *
		 ******************/
		for (out_edge_iter_pair itd(out_edges(action, graph)); itd.first!= itd.second; ++itd.first) {
			vertex_descriptor distr(target(*itd.first, graph));
			CFGNodeProp& prop_distr(graph[distr]);
			ValIterResult& v_distr(lower ? prop_distr.lb : prop_distr.ub);
			const NondetMatrix& matrix(matrices[prop_distr.id]);

			for(unsigned block = 0; block<nr_of_blocks; ++block) {
				if(v_distr.str[block]==-1) continue;
				if(matrix.starts[block] > (unsigned)v_distr.str[block]
				                            ||
				   matrix.starts[block+1] <= (unsigned)v_distr.str[block]
				) {

					MSG(0, "CFG::valIterCheck: starts[%d]=%d v_distr.str[%d]=%d matrix.starts[%d]=%d\n",
							block,matrix.starts[block],
							block,v_distr.str[block],
							block+1,matrix.starts[block+1]);
					assert(false);
				}
				v_distr.val[block];
			}
		} // distributions
		CFGNodeProp& prop_action(graph[action]);

		ValIterResult& v_action(lower ? prop_action.lb : prop_action.ub);
		for(unsigned block = 0; block<nr_of_blocks; ++block) {
			v_action.str[block];
			v_action.val[block];
		}
	} // actions

	for(unsigned block = 0; block<nr_of_blocks; ++block) {
		v.str[block];
		v.val[block];
	}
	return true;
}

bool CFG::valIterInit(bool lower, bool min, Location loc) {
	CFGNodeProp& prop(graph[loc]);
	ValIterResult& v(lower ? prop.lb : prop.ub);
	unsigned nr_of_blocks(v.val.size());

	return true;

	// evaluate distributions, and actions
	for (adjacency_iter_pair itc(adjacent_vertices(loc, graph)); itc.first!= itc.second; ++itc.first) {
		vertex_descriptor action(*itc.first);
		/******************
		 * distributions  *
		 ******************/
		for (adjacency_iter_pair itd(adjacent_vertices(action, graph)); itd.first!= itd.second; ++itd.first) {
			vertex_descriptor distr(*itd.first);
			CFGNodeProp& prop_distr(graph[distr]);
			ValIterResult& v_distr(lower ? prop_distr.lb : prop_distr.ub);
			const NondetMatrix& matrix(matrices[prop_distr.id]);

			for(unsigned block = 0; block<nr_of_blocks; ++block) {
				unsigned l1(matrix.starts[block]), h1(matrix.starts[block+1]);
				if(l1==h1) continue;
				if(v_distr.str[block] == -1)
					v_distr.str[block] = l1;

			}
		} // distributions
		CFGNodeProp& prop_action(graph[action]);

		ValIterResult& v_action(lower ? prop_action.lb : prop_action.ub);
		for(unsigned block = 0; block<nr_of_blocks; ++block) {
			if(v_action.str[block]==-1)
			for (adjacency_iter_pair itd(adjacent_vertices(action, graph)); itd.first!= itd.second; ++itd.first) {
				vertex_descriptor distr(*itd.first);
				ValIterResult& v_distr(lower ? graph[distr].lb : graph[distr].ub);
				v_action.str[block] = distr;
				if(v_distr.str[block]!=-1) break;
			}
		}
	} // actions

	for(unsigned block = 0; block<nr_of_blocks; ++block) {
		if(v.str[block]==-1)
		for (adjacency_iter_pair itc(adjacent_vertices(loc, graph)); itc.first!= itc.second; ++itc.first) {
			vertex_descriptor action(*itc.first);
			ValIterResult& v_action(lower ? graph[action].lb : graph[action].ub);
			v.str[block] = action;
			if(v_action.str[block]!=-1) break;
		}
	}

}


/** \brief update of a location node within value iteration
  * \note  valIter evaluates recursively the action and distribution subtree pertaining to a location
  * \param lower  lower or upper bound
  * \param min    minimal or maximal reachability
  * \param vertex the vertex to be updated
  */
bool CFG::valIter(bool lower, bool min, Location loc) {
	// rien a faire
	if(out_degree(loc, graph) == 0)
		return true;
}

Location CFG::getLocation(Action a) const {
	Location result(goal);
	assert(in_degree(a, graph)>0);
	for (in_edge_iter_pair itd(in_edges(a, graph)); itd.first!= itd.second; ++itd.first) {
		vertex_descriptor loc_vertex(source(*itd.first, graph));
		result = loc_vertex;
	}
	assert(result!=goal);
	return result;
}

Action CFG::getAction(Distribution d) const {
	Action result(0);
	assert(in_degree(d, graph)>0);
	for (in_edge_iter_pair itd(in_edges(d, graph)); itd.first!= itd.second; ++itd.first) {
		vertex_descriptor action_vertex(source(*itd.first, graph));
		result = action_vertex;
	}
	return result;
}



/** \brief check consistency of blocks in a location */
bool CFG::checkLocation(Location loc) const {
	const CFGLocation& cfg_loc(getCFGLocation(loc));

	bool result(true);

	// go through the blocks and check consistency of each
	int n = cfg_loc.b.odd.getNumOfStates();

	for(unsigned i=0; i<n; ++i) {
		Block b(loc,i);
		CVC3::Expr e(getExpr(b));

		if(lang::ExprManager::IsFalse(e)) {

			MSG(0,"CFG::checkLocation location %d\n",loc);
			MSG(0,"CFG::checkLocation block "+e.toString()+"\n");

			dp::MathSat mathsat;
			std::vector<CVC3::Expr> vec;
			vec.push_back(e);
			std::vector<CVC3::Expr> core;
			mathsat.getUnsatCore(vec, core );

			foreach(CVC3::Expr c, core)
			MSG(0,"CFG::checkLocation " + c.toString()+"\n");

			bool within_loc_unsat(lang::ExprManager::IsFalse(getExprWithinLocation(b)));
			MSG(0,"CFG::checkLocation %s within loc\n",(within_loc_unsat ? "UNSAT" : "SAT"));

			assert(false);
		}
	}

	return result;
}



}


