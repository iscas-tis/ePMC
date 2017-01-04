/*
 * CFGRefine.cpp
 *
 *  Created on: Sep 3, 2009
 *      Author: bwachter
 */

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

#include "model_checker/ActionGraph.h"
#include "model_checker/MDPSparse.h"
#include "EncodingManager.h"
#include "Cluster.h"
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

Block CFG::getMaxDeviation() const {
	Block result;
	result.loc = goal;
	result.block_nr = 0;

	double max(0.0);

	std::vector<Splitter> dummy;

	foreach(const Location& loc, locs) {
		const CFGNodeProp& prop(graph[loc]);
		for(unsigned block_nr =0; block_nr <prop.lb.val.size();++block_nr) {
			double diff(prop.ub.val[block_nr] - prop.lb.val[block_nr]);
			Block b(loc,block_nr);



			if(diff > max && getSplitters(b, dummy,true)) {
				max = diff;
				result.loc = loc;
				result.block_nr = block_nr;
			}

		}
	}
	return result;
}

bool CFG::getSplitters(const Block& pivot, std::vector<Splitter>& result, bool checking_mode) const {
	if(pivot.loc == goal)
		return false;

	bool res = false;

	/* determine the action taken by lower-bound and upper-bound strategies, respectively */
	Location loc(pivot.loc);

	assert(pivot.block_nr < graph[loc].lb.str.size());
	assert(pivot.block_nr < graph[loc].ub.str.size());

	if(graph[loc].lb.str[pivot.block_nr]==-1 || graph[loc].ub.str[pivot.block_nr]==-1) {
		MSG(1,"out_degree(%d,graph) = %d \n",pivot.loc,out_degree(pivot.loc,graph));
		return false;
	}

	Action
		la(graph[loc].lb.str[pivot.block_nr]),
		ua(graph[loc].ub.str[pivot.block_nr]);

	assert(graph[la].isAction());
	assert(graph[ua].isAction());

	/* determine the distribution taken */
	assert(pivot.block_nr < graph[la].lb.str.size());
	assert(pivot.block_nr < graph[ua].ub.str.size());

	if(graph[la].lb.str[pivot.block_nr] == -1 || graph[ua].ub.str[pivot.block_nr] == -1)
		return false;


	Distribution
		ld(graph[la].lb.str[pivot.block_nr]),
		ud(graph[ua].ub.str[pivot.block_nr]);


	assert(graph[ld].isDistr());
	assert(graph[ud].isDistr());

	/* determine the matrix-level distributions */
	assert(pivot.block_nr < graph[ld].lb.str.size());
	assert(pivot.block_nr < graph[ld].ub.str.size());


	int
		lchoice(graph[ld].lb.str[pivot.block_nr]),
		uchoice(graph[ud].ub.str[pivot.block_nr]);

	if(lchoice == -1 || uchoice == -1)
		return false;



	/* create the result */
	if(la == ua) {
		if(ld == ud && lchoice == uchoice) {
			return false;
		}

		if(checking_mode) {
			MSG(1,"CFG::getSplitters: same action, %s\n", ld == ud ? "same distribution" : "different distributions");
			return true;
		}

		Splitter sp;
		sp.b.loc = loc;
		sp.b.block_nr = pivot.block_nr;

		sp.d1 = ld;
		sp.d2 = ud;
		toBlockVector(pivot,ld,lchoice,sp.v1);
		toBlockVector(pivot,ud,uchoice,sp.v2);
		if(ld == ud) {
			bool equal = true;
			for(unsigned i=0; i<sp.v1.size();++i) {
				if(sp.v1[i].block_nr!=sp.v2[i].block_nr)
					equal = false;
			}
			assert(!equal);
		}
		result.push_back(sp);
		res = true;
	} else {
		for (out_edge_iter_pair itd(out_edges(la, graph)); itd.first!= itd.second; ++itd.first) {
			vertex_descriptor distr(target(*itd.first, graph));
			const NondetMatrix& matrix(matrices[graph[distr].id]);
			unsigned stride(matrix.nr_of_branches);
			for(unsigned j=matrix.starts[pivot.block_nr]; j<matrix.starts[pivot.block_nr+1];j+=stride) {
				if(distr!=ld || (distr == ld && j!=lchoice)) {
					if(checking_mode) return true;
					Splitter sp;
					sp.b.loc = loc;
					sp.b.block_nr = pivot.block_nr;
					sp.d1 = ld;
					sp.d2 = distr;
					MSG(1,"CFG::getSplitters %d\n",matrix.nr_of_branches);
					MSG(1,"CFG::getSplitters pivot (location %d block_nr %d) lower ld %d distr %d lchoice %d j %d\n",
							loc,pivot.block_nr,ld,distr,lchoice,j);
					toBlockVector(pivot,ld,lchoice,sp.v1);
					toBlockVector(pivot,distr,j,sp.v2);
					if(ld == distr) {
						bool equal = true;
						for(unsigned i=0; i<sp.v1.size();++i) {
							if(sp.v1[i].block_nr!=sp.v2[i].block_nr)
								equal = false;
							}
							assert(!equal);
					}
					result.push_back(sp);
					res = true;
				}
			}
		}

		for (out_edge_iter_pair itd(out_edges(ua, graph)); itd.first!= itd.second; ++itd.first) {
			vertex_descriptor distr(target(*itd.first, graph));
			const NondetMatrix& matrix(matrices[graph[distr].id]);
			unsigned stride = matrix.distr.size();
			for(unsigned j=matrix.starts[pivot.block_nr]; j<matrix.starts[pivot.block_nr+1];j+=stride) {
				if(distr!= ud || (distr == ud && j!=uchoice)) {
					if(checking_mode) return true;
					Splitter sp;
					sp.b.loc = loc;
					sp.b.block_nr = pivot.block_nr;
					sp.d1 = ud;
					sp.d2 = distr;
					MSG(1,"CFG::getSplitters upper\n");
					toBlockVector(pivot,ud,uchoice,sp.v1);
					toBlockVector(pivot,distr,j,sp.v2);
					result.push_back(sp);
					res = true;
				}
			}
		}
	}
	MSG(1,"CFG::getSplitters: # of splitters %d\n",result.size());
	return res;
}

void CFG::toBlockVector(Block pivot, Distribution d, unsigned choice, std::vector<Block>& result) const {

	assert(validLocation(pivot.loc));

	assert(graph[d].isDistr());
	assert(graph[d].id< matrices.size());

	const NondetMatrix& matrix(matrices[graph[d].id]);

	assert(pivot.block_nr < getCFGLocation(pivot.loc).b.odd.getNumOfStates());
	assert(choice >= matrix.starts[pivot.block_nr]);

	MSG(1,"toBlockVector: pivot location %d\n",pivot.loc);
	MSG(1,"toBlockVector: Command " + getCommand(getAction(d)).toString() + "\n");

	assert(choice + out_degree(d,graph) - 1 < matrix.starts[pivot.block_nr+1]);

	result.resize(matrix.distr.size());
	for (out_edge_iter_pair it(out_edges(d, graph)); it.first!= it.second; ++it.first) {
		vertex_descriptor succ(target(*it.first, graph));
		unsigned branch(graph[*it.first]);
		result[branch].loc      = succ;

		if(choice + branch >= matrix.succ.size()) {
			const lang::Command& command(getCommand(getAction(d)));
			MSG(1,command.toString()+ "\n");
			MSG(1,"choice %d branch %d choice + branch %d >= matrix.succ.size() %d\n", choice, branch, choice+ branch, matrix.succ.size());
			assert(false);
		}
		assert(choice + branch < matrix.succ.size());
		result[branch].block_nr = matrix.succ[choice+branch];
		MSG(1,"CFG::toBlockVector: location %d block number %d\n", result[branch].loc, result[branch].block_nr);
	}
}


CVC3::Expr CFG::getPredicate(const Splitter& sp) {

	const CFGCommand& cfg_command(getCFGCommand(sp.d1));
	const lang::Command& command(getCommand(getAction(sp.d1)));

	CVC3::Expr e;
	int branch(-1);

	/* same CFG distribution => same predicate sets in successors */

	if(sp.d1 != sp.d2) {
		for(unsigned i=0;i<sp.v1.size();++i) {
			unsigned pos;
			const CFGLocation &loc1 ( getCFGLocation(sp.v1[i].loc) );
			const CFGLocation &loc2 ( getCFGLocation(sp.v2[i].loc) );
			if(sp.v1[i].loc==goal || sp.v2[i].loc == goal) {
				branch = i;
				e = getCFGLocation(goal).e[0];
				break;
				MSG(1,"CFG::getPredicate goal block %d\n",branch);
			}
			else if(loc1.getDiff(loc2,pos)) {
				branch = i;
				e = loc1.e[pos];
				break;
				MSG(1,"CFG::getPredicate %d\n",branch)
			}
		}
	} else {
		assert(sp.v1.size()>0);
		assert(sp.v1.size()==sp.v2.size());
		for(unsigned i=0;i<sp.v1.size();++i) {
			MSG(1,"CFG::getPredicate: same distribution\n")
			unsigned pos;
			const CFGLocation &loc1 ( getCFGLocation(sp.v1[i].loc) );
			assert(sp.v1[i].loc != goal);
			assert(sp.v1[i].loc == sp.v2[i].loc);
			/* find the predicate where the two blocks differ */
			if(sp.v1[i].block_nr != sp.v2[i].block_nr) {
				MSG(1,"CFG::getPredicate: different blocks\n")
				if(loc1.getDiff(sp.v1[i].block_nr,sp.v2[i].block_nr,pos)) {
					branch = i;

					const StateVar& state_var(cfg_command.sac.getStateVar(branch+1,pos));
					MSG(1,"CFG::getPredicate: state variable " + state_var.toString() + "\n");
					const Predicate& pred(state_var.getPredicate());
					MSG(1,"CFG::getPredicate " + pred.toString());
					e = pred.getExpr();
					break;
				}
			}
		}
	}

	assert(branch != -1);
	assert(!e.isNull());
	return command.WP(e,branch);
}

bool CFGLocation::getDiff(const CFGLocation& loc, unsigned& position) const {
	assert(e.size()==loc.e.size());

	for(unsigned i=0; i<e.size();++i) {
		if(e[i]!=loc.e[i]) {
			position = i;
			return true;
		}
	}
	return false;
}


bool CFGLocation::getDiff(unsigned block_nr1, unsigned block_nr2, unsigned& position) const {
	if(block_nr1 == block_nr2) return false;

	Cube c1;
	b.odd.toCube(block_nr1,c1);
	Cube c2;
	b.odd.toCube(block_nr2,c2);
	assert(c1.size() == c2.size());
	position = -1;
	for(unsigned i=0;i<c1.size();++i) {
		if(c1[i]!=c2[i]) {
			position = i;
			return true;
		}
	}
	assert(position >= 0);
	return true;

}

CVC3::Expr CFG::getExprWithinLocation(const Block& b) const
{
	Cube c;
	const CFGLocation& cfg_loc(getCFGLocation(b.loc));
	cfg_loc.b.odd.toCube(b.block_nr,c);

	std::vector<CVC3::Expr> exprs(cfg_loc.b.preds.size());
	for(unsigned i= 0; i < cfg_loc.b.variables.size();++i) {
		const StateVar sv(em.getStateVarFromBDDIndex(cfg_loc.b.variables[i]));
		exprs[i] = sv.getPredicate().getExpr();
	}

	return lang::ExprManager::getExprCube(c,exprs);
}

CVC3::Expr CFG::getExpr(const Block& b) const
{
	const CFGLocation& cfg_loc(getCFGLocation(b.loc));
	std::vector<CVC3::Expr> exprs(cfg_loc.e);
	exprs.push_back(getExprWithinLocation(b));
	return lang::vc.andExpr(exprs);
}



/** \brief get block with highest difference between lower and upper bound in the set of blocks "blocks"
  * \param blocks input set
  * \return       said block , returns goal block if nothing can be found
  */
Block CFG::getPivot(const std::unordered_set<Block>& blocks) const {
	Block result(goal,0);
	double highest_diff = -1.0;
	foreach(const Block& block, blocks) {
		const CFGNodeProp& prop(graph[block.loc]);

		if(!prop.isLocation()) continue;

		std::vector<Splitter> spvec;

		double diff ( prop.ub.val[block.block_nr] - prop.lb.val[block.block_nr]);
		if(diff > highest_diff && getSplitters(block,spvec,true) ) {
			highest_diff = diff;
			result = block;
		}
	}
	return result;
}

/** \brief compute a (BFS) path from block "init" to block "pivot"
  * \param init  start state
  * \param pivot goal state
  * \param back  BFS back pointers
  * \param path  result path
  */
void CFG::computePath(const Block& init, const Block& pivot, const std::unordered_map<Block,Transition>& back, CFGPath& path) {
	Block from (pivot);

	Transition last(pivot);

	path.clear();
	path.push_back(last);

	while(from != init) {
		std::unordered_map<Block,Transition>::const_iterator bit(back.find(from));
		assert(bit!=back.end());
		const Transition& transition(bit->second);
		assert(!transition.isTerminal());
		from = transition.getBlock();
		path.push_back(transition);
	}
	std::reverse(path.begin(),path.end());
}

void CFG::computeTransitionConstraints( const CFGPath& path, std::vector<CVC3::Expr>& constraints) {

	unsigned path_length(path.size());

	tc.createVariableInstances(path_length+1);

	unsigned constraints_size(2*path_length - 1);

	constraints.resize(constraints_size);

	std::vector<std::vector<CVC3::Expr> > symbolic_path(constraints.size());

	/* build up the SSA form and the updates */
	for(unsigned step = 0; step < path_length - 1; ++step) {
		const Transition& t(path[step]);
		assert(!t.isTerminal());
		const lang::Command& command(getCommand(t.getAction()));
		MSG(1,"command " + command.toString() + "\n");
		std::vector<int> choices;
		choices.push_back(t.getChoice());
		tc.getTransitionConstraint(command,choices, step, symbolic_path[2 * step + 2]);
		lang::ExprManager::getTopLevelConjuncts(tc.getInstance(command.getGuard(),step),symbolic_path[2 * step + 1]);
		MSG(1,"guard: " + lang::ExprManager::prettyPrint(tc.getInstance(command.getGuard(),step)) + "\n");
	}
	lang::ExprManager::getTopLevelConjuncts(tc.getInstance(initial_expr,0),symbolic_path[0]);

	lang::ExprManager::getTopLevelConjuncts(tc.getInstance(getExpr(path[path_length-1].getBlock()),path_length-1),symbolic_path[2 * path_length - 2]);

	/* put everything together */
	for(unsigned c = 0; c < constraints.size(); ++c) {
		MSG(1,"CFG::computeTransitionsConstraints ========== %d ========== \n",c);
		foreach(CVC3::Expr e, symbolic_path[c]) {
			MSG(1,"CFG::computeTransitionConstraints " + e.toString() + "\n");
		}
		constraints[c] = lang::vc.andExpr(symbolic_path[c]);
	}
}

lbool CFG::checkPath (CFGPath& path, Refinement& refinement) {
	lbool result(l_undef);


	if(path.size() < 2)
		return l_true;


	std::vector<CVC3::Expr> constraints;
	computeTransitionConstraints (path,constraints);


	dp::SMT& interpolator(dp::SMT::getInterpolator());

	/* run the decision procedure to check the path constraints      *
	 * 1) path constraints are SAT => path feasible: return l_true   *
	 * 2) path constraints UNSAT   => path infeasible return l_false *
	 * 3) unknown                  => return l_undef                 */

	std::vector<CVC3::Expr> interpolants;
	lbool answer_decision_procedure (interpolator.Interpolate(constraints, interpolants));

	switch(answer_decision_procedure) {
		case l_true: // interpolants have been found => path infeasible
			assert(interpolants.size() == 2 * path.size() - 2);

			for(unsigned step = 0; step < path.size() - 1; ++step) {
				const Block& block(path[step].getBlock());
				CVC3::Expr interpolant(interpolants[2 * step].substExpr(tc.getOriginalFromInstance()));
				MSG(0,"CFG::checkPath location %d\n",block.loc);
				MSG(0,"CFG::checkPath: interpolant: "+ interpolant.toString()+"\n");
				lang::ExprManager::CollectExprs(interpolant,refinement[block.loc]);

				interpolant = interpolants[2 * step + 1].substExpr(tc.getOriginalFromInstance());
				MSG(0,"CFG::checkPath: interpolant: "+ interpolant.toString()+"\n");
				lang::ExprManager::CollectExprs(interpolant,refinement[block.loc]);
			}
			result = l_false;
			break;
		case l_false: // no interpolants found => path is feasible
			result = l_true;
			break;
		case l_undef:
			result = l_undef;
			break;
	}
	return result;
}

/** \brief Visitor function for BFS traversal in CFG::getRefinement
  * \param incoming transition by which block "block" is reached
  * \param block    block to be visited
  * \param back     back pointer information
  * \param seen     blocks visited so far
  * \param frontier blocks to be examined
  */
void CFG::visitBlock(const Transition& incoming,
		const Block& block,
		std::unordered_map<Block,Transition>& back,
		std::unordered_set<Block>& seen,
		std::unordered_set<Block>& frontier) {
	if(seen.count(block)>0) return;

	back[block] = incoming;
	seen.insert(block);
	frontier.insert(block);
}


void CFG::getRefinement(Refinement& refinement) {

	// determine an initial block with maximal deviation of lower and upper bound
	Block init_block;
	double highest_diff = -1.0;

	foreach(const Location& loc, init) {
		const CFGLocation& cfg_loc(getCFGLocation(loc));
		const CFGNodeProp& prop(graph[loc]);
		for(unsigned i=0; i<graph[loc].lb.val.size();++i) {
			if(cfg_loc.b.init_vec[i]) {
				double diff ( prop.ub.val[i] - prop.lb.val[i]);
				if(diff > highest_diff ) {
					highest_diff = diff;
					init_block.loc      = loc;
					init_block.block_nr = i;
					MSG(0,"CFG::getRefinement: highest difference %E\n",highest_diff);
				}
			}
		}
 	}

	if(highest_diff == 0)
		return;

	/** do a BFS from the block to determine blocks reachable under strategy
	  * (refer to function "visitBlock")
	  */
	std::unordered_map<Block,Transition> back;
	std::unordered_set<Block> seen;
	std::unordered_set<Block> frontier;

	seen.insert(init_block);
	frontier.insert(init_block);


	while(!frontier.empty()) {
		Block block = *frontier.begin();
		frontier.erase(frontier.begin());

		const CFGNodeProp& prop(graph[block.loc]);
		const Strategy& str(prop.ub.str);
		if(block.block_nr >= str.size())
					continue;

		if(str[block.block_nr] == -1) continue;
		Action action(str[block.block_nr]);

		const Strategy& str_action(graph[action].ub.str);
		if(str_action[block.block_nr] == -1) continue;
		Distribution distr(str_action[block.block_nr]);

		const Strategy& str_distr(graph[distr].ub.str);
		if(str_distr[block.block_nr] == -1) continue;
		unsigned choice(str_distr[block.block_nr]);

		const NondetMatrix& matrix(matrices[graph[distr].id]);

		unsigned j=0;
		for (adjacency_iter_pair it(adjacent_vertices(distr, graph)); it.first!= it.second; ++j, ++it.first) {
			vertex_descriptor succ_loc(*it.first);
			Block succ_block(succ_loc,matrix.succ[choice+j]);
			Transition incoming_transition(block,action,distr,j);
			visitBlock(incoming_transition,succ_block,back,seen,frontier);
		}
	}

	// determine a pivot
	Block pivot(getPivot(seen));


	if(pivot.loc == goal) {
		MSG(0,"CFG::getRefinement:  no pivot location found\n");
		return;
	}

	CFGPath path;

	computePath (init_block,pivot,back,path);
	switch(checkPath(path,refinement)) {
		case l_true: // the path is feasible (exists in the concrete model)
			{
				// find path(s) to pivot
				std::vector<Splitter> spvec;
				getSplitters(pivot,spvec);

				MSG(0,"CFG::getRefinement: pivot location %d # splitters %d \n",pivot.loc,spvec.size());


				foreach(const Splitter& splitter, spvec) {
					refinement[pivot.loc].insert(getPredicate(splitter));
				}
			}
			break;
		case l_false: // the path is infeasible (does not exist and we get refinement predicates )

			break;
		case l_undef: // the path analysis has failed (unexpected)
			assert(false);
			break;
	}
}

void CFG::AR(bool min) {
	MSG(0,"CFG::AR(bool min)\n");
	initialize();


	/* short-circuit refinement loop:
	 * refine only lazily where needed */

	bool done = false;
	unsigned iter = 0;

	while(!done) {
		++iter;
		done = true;

		/* value iteration */
		MSG(0,"value iteration ... \n");


		unsigned blocks = 0;
		foreach(Location loc, locs) {

			CFGLocation cfg_loc(getCFGLocation(loc));
			if(loc !=goal) {

			}
		}	
		MSG(0,"CFG::AR: number of blocks %d\n",blocks);


#if 0
		std::string afilename("cfg" + util::intToString(iter) + "a.gdl");
		std::ofstream filea(afilename.c_str());
		aiSee(filea);
#endif

		valIter(min);
		printValIterResults();

#if 0
		std::string filename("cfg" + util::intToString(iter) + ".gdl");

		std::ofstream file(filename.c_str());
		aiSee(file);
#endif

		Refinement refinement;

		getRefinement(refinement);
		done = insertRefinement(refinement);


		/* recompute the abstraction within the impact zone */
		std::set<Location> workset;
		std::set<Distribution> impactZoneAbstractTransitions;

		foreach(RefinementPair& p, refinement) {
			if(p.second.size()>0) {
				MSG(0,"CFG::AR: change at location %d\n",p.first);
				if(init.count(p.first)>0)
					MSG(0,"CFG::AR: initial location\n");
				workset.insert(p.first);
				getDistributions(p.first,impactZoneAbstractTransitions);
				getPredecessors(p.first,impactZoneAbstractTransitions);
			}
		}

		MSG(0,"CFG::AR: recomputing abstraction\n");
		foreach(Distribution distr, impactZoneAbstractTransitions) {
			computeAbstractTransitions(distr);
		}

		ARChecker(min);


		/* perform qualitative BDD-based forward reachability *
		 * for locations that are reachable from pivot        */

		qualReach(workset,true);

		/* perform qualitative BDD-based backward reachability
		 * for locations in workset
		 */

		//std::set<Location> workset2(workset);
		//qualReach(workset2,false);
		// workset.insert(workset2.begin(),workset2.end());

		std::set<Distribution> distr_workset;

		/* recompute ODDs workset */
		foreach(const Location& loc, workset) {
			rebuildODD(loc);
			getDistributions(loc,distr_workset);
			getPredecessors(loc,distr_workset);
			getPredecessorLocations(loc,workset);
		}

		MSG(0,"CFG::AR: rebuilding matrices\n");
		foreach(const Distribution& distr, distr_workset) {
			CFGCommand& cfg_command(getCFGCommand(distr));
			const CFGLocation& cfg_loc(getCFGLocation(getLocation(getAction(distr))));
			MSG(1,"Location: %d variables %d # blocks %d\n",getLocation(getAction(distr)),cfg_loc.b.variables.size(),cfg_command.odd_vec[0]->getNumOfStates());
			cfg_command.sac.constrainTransitions(cfg_loc.b.reach);
			assert(cfg_loc.b.reach == cfg_loc.b.odd.getReach());

			cfg_command.sac.toMatrix(cfg_command.odd_vec, matrices[graph[distr].id]);
		}

		std::set<Location> backward_reach;
		backward_reach.insert(workset.begin(),workset.end());
		getBackwardReach(backward_reach);

		foreach(const Location& loc, backward_reach) {
			resetValIter(loc);
		}

		// reconstruct ODD

		// rebuild matrix

		MSG(0,"CFG::AR recomputing probabilistic reachability for %d / %d locations\n",backward_reach.size(),locs.size());

	}
	/* start all over again */


	std::string finalf("final.gdl");

	std::ofstream finalfile(finalf.c_str());
	aiSee(finalfile);



}

bool CFG::addPredicate(Location loc, const CVC3::Expr& e) {
	bool result = false;
	CFGLocation& cfg_loc(getCFGLocation(loc));
	MSG(0,"CFG::addPredicate at location %d\n",loc);
	assert(!e.isNull());
	Predicate p(e);
	MSG(0,"fresh pred " + p.toString() + "\n");

	switch(cfg_loc.b.preds.Add(p,true)) {
	case PredSet::TRIVIAL:
		MSG(0,"trivial\n");
		break;
	case PredSet::CONTAINED:
		MSG(0,"contained\n");
		break;
	case PredSet::COVERED:
		MSG(0,"covered\n");
		break;
	case PredSet::ADDED: {
			MSG(0,"added\n");
			StateVar& state_var = !em.existStateVar(p) ? em.createStateVar(p) : em.getStateVar(p);
			cfg_loc.b.variables.push_back(state_var.getBDDVariableIndex());

			// restore sortedness
			inplace_merge (cfg_loc.b.variables.begin(),cfg_loc.b.variables.begin()+
					cfg_loc.b.variables.size()-1,cfg_loc.b.variables.end());

			result = true;
		}
		break;
	}
	return result;
}



}
