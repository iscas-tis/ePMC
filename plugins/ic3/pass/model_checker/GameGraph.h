#ifndef GAME_GRAPH_H
#define GAME_GRAPH_H

#include <boost/config.hpp>
#include <vector>
#include <boost/property_map/property_map.hpp>
#include <boost/graph/graph_traits.hpp>
#include <boost/graph/adjacency_list.hpp>
#include "GraphDecl.h"
#include "GameSparse.h"
#include "ActionGraph.h"
#include "BackMap.h"

namespace model_checker {

class AiSeePrinter;

struct Pivot {
	State state;
	ChoiceSet c;
	Distribution d;
};

class GameGraph {
	friend class AiSeePrinter;

public:
	GameGraph();

	bool checkConsistency();

	/** Visualization */
	void printAiSee() const;
	void printAiSee(std::string) const;
	void printAiSee(std::ostream &) const;
	void printGML(std::ostream &stream);

	/** Construction */
	State addState();
	void setAnnotation(State, const std::string&);
	void addInit(State init);
	void addGoal(State bad);
	State getBad() const {
		return bad;
	}

	ChoiceSet createChoiceSet();
	void addChoiceSet(State state, ChoiceSet);
	Distribution createDistribution(Action);
	void addDistribution(ChoiceSet, Distribution);
	void removeDistribution(State, ChoiceSet, Distribution);
	void addProbChoice(Distribution, State, double);

	/** Refinement */
	bool isDone();

	State doSplit(const Pivot& pivot);

	/** Value iteration */
	void until(bool P2min);
	double getMinResult();
	double getMaxResult();

	/** Graph algorithms */
	int computeSCCs();

	/** Traversal */
	void getChoiceSets(State state, std::vector<ChoiceSet>&);
	void getDistributions(ChoiceSet, std::vector<Distribution>&);
	Action getAction(Distribution);
	void getProbChoices(Distribution, std::vector<std::pair<State,double> >&);
	void getActionSets(State state, std::vector<std::vector<int> >& vec);
	void getActionSet(edge_descriptor choice, std::vector<int>& vec);
	void getHyperTransition(State s, edge_descriptor choice,
			HyperTransition& ht);
	void getTransition(State s, Distribution distr, Transition& t);

	void getSuccessorStates(Distribution u, StateSet& succ);

	void computeActionGraph(ActionGraph &, bool);
	void computeConflictingHyperTransitions(
			std::vector<std::pair<HyperTransition,HyperTransition> >& c);

	/* \brief detect back edges in the game graph and set the corresponding flag for an edge */
	void detectCycles();
	void removeCycles();

	VertexProp& operator[](vertex_descriptor v) {
		return graph[v];
	}

private:
	friend class Lazy;
	void
			getSuccessors(vertex_descriptor u,
					std::vector<vertex_descriptor>& succ);
	void getSuccessorStates(vertex_descriptor u,
			std::vector<vertex_descriptor>& succ);

	void exploreSCC(State root, StrongComponent& sc);

	void clearResults();
	/**
	 * Returns true iff @a u is a state vertex.
	 *
	 * @param u vertex to decide whether it is a state vertex
	 * @return true iff @a u is a state vertex
	 */
	inline bool isState(vertex_descriptor u) {
		return S == graph[u].vertexType;
	}
	/**
	 * Returns true iff @a u is a choice set vertex.
	 *
	 * @param u vertex to decide whether it is a choice set vertex
	 * @return true iff @a u is a choice set vertex
	 */
	inline bool isChoiceSet(vertex_descriptor u) {
		return P1 == graph[u].vertexType;
	}
	/**
	 * Returns true iff @a u is a distribution vertex.
	 *
	 * @param u vertex to decide whether it is a distribution vertex
	 * @return true iff @a u is a state vertex
	 */
	inline bool isDistribution(vertex_descriptor u) {
		return P2 == graph[u].vertexType;
	}
	/**
	 * Sets a decision depending on whether P1 should min or max.
	 *
	 * @param u state/P1 decision of which decision shall be set
	 * @param e decision to be set
	 * @param P1min true iff decision is for minimizing P1
	 */
	inline void setChoice(vertex_descriptor u, edge_descriptor e, bool lower_bound) {
		if(lower_bound)
			graph[u].P1min_P_choice = e;
		else
			graph[u].P1max_P_choice = e;
	}
	/**
	 * Gets a decision depending on whether P1 should min or max.
	 *
	 * @param u state/P1 decision to get decision of
	 * @param P1min true iff decision should be for minimizing P1
	 * @return decision taken
	 */
	inline edge_descriptor getChoice(vertex_descriptor u, bool lower_bound) {
		return lower_bound ? graph[u].P1min_P_choice : graph[u].P1max_P_choice;
	}
	/**
	 * Returns the chosen distribution for state @a u.
	 * The results depends on the decisions of player 1 and 2.
	 *
	 * @param u state to get chosen distribution of
	 * @param P1min whether player 1 should minimize
	 * @return distribution chosen for @a u
	 */
	inline vertex_descriptor getChosenDistribution(State u, bool lower_bound) {
		assert( isState(u));
		assert(0 != out_degree(u, graph));
		edge_descriptor e1 = getChoice(u, lower_bound);
		vertex_descriptor v = target(e1, graph);
		edge_descriptor e2 = getChoice(v, lower_bound);
		vertex_descriptor w = target(e2, graph);
		return w;
	}
	/**
	 * Sets a probability depending on whether P1 should min or max.
	 *
	 * @param u state of which probability shall be set
	 * @param prob probability
	 * @param P1min true iff probability is for minimizing P1
	 */
	inline void setProbability(vertex_descriptor u, double prob, bool P1min) {
		if (P1min) {
			graph[u].stateProp.setMinProb(prob);
		} else {
			graph[u].stateProp.setMaxProb(prob);
		}
	}
	inline double getProbability(vertex_descriptor u, bool P1min) const {
		if (P1min) {
			return graph[u].stateProp.getMinProb();
		} else {
			return graph[u].stateProp.getMaxProb();
		}
	}
	void setChosenEdges(vertex_descriptor, edge_descriptor, edge_descriptor,
			bool);
	void toGameSparse(bool);

	void mapSparseToGameGraph(bool, std::vector<int>&, std::vector<int>&);
	void extendYesVec(bool, bool);
	void extendNoVec(bool, bool);
	bool subSet(StateSet &, StateSet &);
	void createTHEBadState();
	void removeTHEBadState();

	typedef std::list<vertex_descriptor> States;

	std::list<vertex_descriptor> states;

	StateSet loop_decision;

	Graph graph;

	/* the state to be refined */
	State pivotState;
	Distribution pivotDistr;

	State bad;

	BackMap backMap;
	GameSparse sparse;

	/* lower and upper probability
	 * from sparse matrix */
	std::vector<double> lower;
	std::vector<double> upper;
	/* for many purposes, we just need one bad state;
	 * in this case, bad_state is used. Usually, for each
	 * "regular" bad state one adds an edge to the bad_state and
	 * that's it */
	State bad_state;
	/*
	 * Also, for similar reasons we often only need one initial state
	 */
	State init_state;

	std::unordered_map<State,std::string> annotation;

	bool doExtendYesVector;
	bool doExtendNoVector;
	/**
	 * Sets whether to do yes vector extension.
	 * If @a __doYesVectorExtension is true, precalculations will be
	 * done which will extend the set of bad states by those who will
	 * reach the set of bad states with probability 1.0. For some models,
	 * this will speed up the analysis quite a lot, because it is much
	 * faster than value iteration. Notice that is is not valid for any
	 * kind of bounded until, next, etc!
	 * This option is activated by default.
	 *
	 * TODO: If later on bounded or even nested formulas become possible,
	 * allow a third value to automatically decide whether to extend
	 * yes vector.
	 *
	 * @param __doExtendYesVector true iff yes vector is to be extended
	 */
	inline void setExtendYesVector(bool __doExtendYesVector) {
		doExtendYesVector = __doExtendYesVector;
	}
	/**
	 * Sets whether to do no vector extension.
	 * If @a __doNoVectorExtension is true, precalculations will be
	 * done which will calculate the set of state that have no change
	 * of ever reaching the set of bad states (according to
	 * players given stategies) For some models, this will speed
	 * up the analysis quite a lot, because it is faster than
	 * value iteration. This option is valid for bounded until properties.
	 * This option is activated by default.
	 *
	 * TODO: If later on bounded or even nested formulas become possible,
	 * allow a third value to automatically decide whether to extend
	 * no vector.
	 *
	 * @param __doExtendNoVector true iff no vector is to be extended
	 */
	inline void setExtendNoVector(bool __doExtendNoVector) {
		doExtendNoVector = __doExtendNoVector;
	}
	std::map<vertex_descriptor,unsigned> vertexMap;

	/*! number of strongly-connected components in the graph */
	int nrOfSCCs;

	std::set<vertex_descriptor> usedVertices;

	std::vector<StrongComponent> sccs;
};
}

#endif
