/*
 * CFG.h
 *
 *  Created on: May 10, 2009
 *      Author: bwachter
 */

#ifndef CFG_H_
#define CFG_H_

#include "CFGDef.h"

namespace pred {

/** \brief lazy abstraction-refinement engine for probabilistic programs
  */
class CFG {
public:

	CFG(lang::Model& model);

	/* construction */
	Location     addLocation(const std::vector<CVC3::Expr>& e);
	Action       addAction  (Location loc, lang::Command&);
	Distribution addDistribution   (Action a, std::vector<Location>& succ);

	void addInit(Location loc) { init.insert(loc); }
	void setGoal(Location loc)    { goal = loc; }

	void setInitial(const CVC3::Expr& init) { initial_expr = init; }

	/* value iteration */
	void valIter(bool min);
	void valIter(bool lower, bool min);
	bool valIter(bool lower, bool min, Location);
	bool valIterInit(bool lower, bool min, Location loc);


	void printValIterResults();

	/* refinement */

	bool addPredicate(Location loc, const CVC3::Expr&);
	void initialize();
	void AR(bool min);
	void ARChecker(bool min) const;

	typedef std::unordered_map<Location,std::set<CVC3::Expr> > Refinement;
	typedef std::pair<const Location,std::set<CVC3::Expr> > RefinementPair;
	void getRefinement(Refinement& refinement);
	bool insertRefinement(Refinement& refinement);


	Block getPivot(const std::unordered_set<Block>& blocks) const;


	/*
	 * rebuild explicit representation (ODD and vectors)
	 */
	void resetValIter(Location);
	void rebuildODD(Location);

	void computeAbstractTransitions(Distribution distr);

	int precision;

	/* assertion checking */

	/** \brief check consistency of blocks in a location */
	bool checkLocation(Location) const;

	bool valIterCheck(bool lower, bool min, Location);



private:

	lang::Model& model;

	TransitionConstraint tc;

	/** Boost graph */
	Graph graph;

	DFSOrder dfs_order;
	DFSReverseOrder reverse_dfs_order;

	/* locations sorted in reverse DFS order */
	std::vector<Location> locs;

	std::unordered_set<Location> init;

	CVC3::Expr initial_expr;

	Location goal;

	/** decorations of edges */
	std::vector<boost::shared_ptr<CFGLocation> > locations;
	std::vector<boost::shared_ptr<lang::Command> >  commands;
	std::vector<boost::shared_ptr<CFGCommand> >  cfg_commands;
	std::vector<NondetMatrix> matrices;


    bdd::BDD  abstractCover(const CVC3::Expr& e,
	        	   	const PredSet& preds,
			        const bdd::BDD& care_set);

    bdd::BDD abstractInterior(const CVC3::Expr & e,
				  const PredSet & preds,
				  const bdd::BDD& care_set);

    bdd::BDD Abstract(const CVC3::Expr& e,
		  const std::vector<CVC3::Expr>& exprs,
		  const std::vector<bdd::BDD>& variables,
		  const bdd::BDD& care_set);



	inline CFGLocation& getCFGLocation(Location loc) const {
		assert(graph[loc].isLocation());
		assert(graph[loc].id < locations.size());
		return *locations[graph[loc].id];}

	inline const CFGCommand& getCFGCommandConst(Distribution distr) const {
		assert(graph[distr].isDistr());
		assert(graph[distr].id < cfg_commands.size());
		return *cfg_commands[graph[distr].id];
	}

	inline CFGCommand& getCFGCommand(Distribution distr) const {
		assert(graph[distr].isDistr());
		assert(graph[distr].id < cfg_commands.size());
		return *cfg_commands[graph[distr].id];
	}

	inline const lang::Command& getCommand(Action a) const {
		assert(graph[a].isAction());
		assert(graph[a].id < commands.size());
		return *commands[graph[a].id];
	}

	bool validLocation(Location loc) const {
		bool result(graph[loc].isLocation());

		if(result) {
			bool contained(false);

			foreach(Location loc1, locs) {
				contained = contained || (loc1 == loc);
			}
			result = result && contained;
		}
		return result;
	}


	Location getLocation(Action a) const;
	Action   getAction(Distribution d) const;


	void computeOrder();

	void qualReach(std::set<Location>& workset, bool forward);
	bdd::BDD qualForwardReach(const Location& loc);
	bdd::BDD qualBackwardReach(const Location& loc);


	void getPredecessors(vertex_descriptor loc, std::set<vertex_descriptor>& result);
	void getPredecessorLocations(Location loc, std::set<Location>& result);
	void getSuccessorLocations(Location loc, std::set<Location>& result);

	void getForwardReach(std::set<Location>& locs);
	void getBackwardReach(std::set<Location>& locs);

	void getDistributions(Location loc, std::set<Distribution>& result);

	bdd::DDManager dd_mgr;
	EncodingManager em;


	Block getMaxDeviation() const;

	/* \brief Find splitters for refinement for a given block
	 * \param pivot block to be refined
	 * \param result splitters for refinement
	 * \return true if we found a splitter
	 */
	bool getSplitters(const Block& pivot, std::vector<Splitter>& result, bool checking_mode = false) const;

	/* \brief synthesize predicates from a splitter
	 * \param sp splitter
	 * \param preds result
	 */
	CVC3::Expr getPredicate(const Splitter& sp);


	void visitBlock(const Transition& incoming,
		const Block& block,
		std::unordered_map<Block,Transition>& back,
		std::unordered_set<Block>& seen,
		std::unordered_set<Block>& frontier);

	void computePath                  (const Block& init, const Block& pivot, const std::unordered_map<Block,Transition>& back, CFGPath& path);
	void computeTransitionConstraints (const CFGPath& path, std::vector<CVC3::Expr>& constraints);
	lbool checkPath (CFGPath& path, Refinement& refinement);

	void toBlockVector(Block pivot, Distribution d, unsigned choice, std::vector<Block>& result) const;

	CVC3::Expr getExpr(const Block& b) const;
	CVC3::Expr getExprWithinLocation(const Block& b) const;

	void aiSee(std::ostream &stream) const;
};
}


#endif /* CFG_H_ */
