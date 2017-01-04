#include <fstream>
#include <iostream>
#include "util/Util.h"
#include "util/Database.h"
#include "util/Statistics.h"
#include "GameGraph.h"
#include "GameSparse.h"
#include "ActionGraph.h"
#include "AiSeePrinter.h"

#include <boost/graph/dijkstra_shortest_paths.hpp>
#include <boost/graph/strong_components.hpp>
#include <boost/graph/depth_first_search.hpp>
#include <boost/graph/graph_utility.hpp>

using namespace model_checker;
using namespace std;
using namespace boost;

namespace model_checker {

GameGraph::GameGraph() {
	pivotState = 0;
	pivotDistr = 0;

	doExtendYesVector = util::Database::extend_yes;
	// leave this "false" until it actually works!
	doExtendNoVector = false;
}

State GameGraph::addState() {
	VertexProp prop(intermediate);
	State result = add_vertex(prop, graph);
	states.insert(states.begin(), result);

	return result;
}

void GameGraph::setAnnotation(State s, const std::string & str) {
	annotation[s] = str;
}

void GameGraph::addInit(State __init) {
	graph[__init].stateProp.setInit();
	/* TODO: actually this should be a set of initial states */
	init_state = __init;
}

/**
 * Marks state @a __bad as being a bad state.
 * Also, min and max probabilities are set to 1 for this state.
 *
 * @param __bad state to be marked as bad
 */
void GameGraph::addGoal(State __bad) {
	bad = __bad;
	graph[__bad].stateProp.setGoal();
}

/**
 * Creates a new choice set.
 *
 * @return new choice set
 */
ChoiceSet GameGraph::createChoiceSet() {
	VertexProp prop(true);
	ChoiceSet result = add_vertex(prop, graph);
	return result;
}

/**
 * Adds choice set to set of choice sets of a state.
 *
 * Remember that for each state s there is a set of choice sets
 * C = {{P_s_{11}, ..., P_s_{1m_1}}, ..., {P_s_{n1}, ..., P_s_{nm_n}}}
 * where each element of C describes a possible choice of player 1
 * whereas each element of an element of C describes a choice of player 2.
 *
 * @param state state to add choice set to
 * @param choice choice set to be added to state
 */
void GameGraph::addChoiceSet(State state, ChoiceSet choice) {
	add_edge(state, choice, graph);

}

/**
 * Creates a new distribution.
 *
 * @param label when label is taken, this leads to resulting distribution
 * @return new distribution
 */
Distribution GameGraph::createDistribution(Action label) {
	VertexProp prop(label);
	Distribution result = add_vertex(prop, graph);

	return result;
}

/**
 * Adds a distribution to player 2's choices.
 *
 * @param c set of player 2 choices to add distribution to
 * @param d distribution to be added to set
 */
void GameGraph::addDistribution(ChoiceSet c, Distribution d) {
	add_edge(c, d, graph);
}

void GameGraph::removeDistribution(State state, ChoiceSet c, Distribution d) {

	MSG(0,"GameGraph::removeDistribution\n")
	checkConsistency();
	// check if "state" is the only one referring to this choice set */
	if (in_degree(c, graph) == 1) {
		MSG(0,"GameGraph::removeDistribution: case in_degree(c,graph) == 1\n")
		remove_edge(c, d, graph);
		if (out_degree(c, graph) == 0) {
			clear_vertex(c, graph);
			remove_vertex(c, graph);
		}
		if (in_degree(d, graph) == 0) {
			clear_vertex(d,graph);
			remove_vertex(d, graph);
		}
	} else {
		remove_edge(state, c, graph);
		MSG(0,"GameGraph::removeDistribution: case in_degree(c,graph) != 1\n")
		// check if c has more than one distr
		if (out_degree(c, graph) > 1) {

			ChoiceSet freshc = createChoiceSet();
			addChoiceSet(state, freshc);
			/* link fresh state with all out-going choice sets of pivot state except pivot.c */
			for (out_edge_iter_pair it(out_edges(c, graph)); it.first
					!= it.second; ++it.first) {
				Distribution dold(target(*it.first, graph));
				if (d != dold)
					addDistribution(freshc, dold);
			}
		}
	}
	checkConsistency();
}

/**
 * Adds probabilistic choice to a distribution @a c.
 *
 * @param c distribution to add choice to
 * @param target target state of choice
 * @param prob probability of choice
 */
void GameGraph::addProbChoice(Distribution c, State target, double prob) {
	pair<Graph::edge_descriptor, bool> e = add_edge(c, target, graph);
	graph[e.first] = prob;
}

State GameGraph::doSplit(const Pivot& pivot) {

	MSG(0,"GameGraph::doSplit\n");

	/*
	 Effect of refinement:
	 pivot state -> cannot perform the chosen transition anymore
	 fresh state -> can do only that a-transition (and no other a-transition)
	 */

	/* create  fresh state */
	State fresh = addState();

	/* add the pivot distribution to the fresh state */
	ChoiceSet atrans(createChoiceSet());
	add_edge(fresh,atrans,graph);
	add_edge(atrans, pivot.d,graph);

	/* link fresh state with all out-going choice sets of pivot state except pivot.c */
	for (out_edge_iter_pair it(out_edges(pivot.state, graph)); it.first
			!= it.second; ++it.first) {
		ChoiceSet c(target(*it.first, graph));
		if (c != pivot.c)
			add_edge(fresh, c, graph);
	}

	/* add incoming distributions to fresh state */
	for (in_edge_iter_pair it1(in_edges(pivot.state, graph)); it1.first
			!= it1.second; ++it1.first) {
		Distribution d(source(*it1.first, graph));

		Distribution freshd(createDistribution(graph[d].p2Prop.action));

		for (out_edge_iter_pair it2(out_edges(d, graph)); it2.first
				!= it2.second; ++it2.first) {
			State succ(target(*it2.first, graph));
			double prob(graph[*it2.first]);
			addProbChoice(freshd, succ == pivot.state ? fresh : succ, prob);
		}

		/* add the distribution to the choiceset */
		for (in_edge_iter_pair it2(in_edges(d, graph)); it2.first != it2.second; ++it2.first) {
			ChoiceSet c(source(*it2.first, graph));
			add_edge(c, freshd,graph);
		}
	}
	/* remove the pivot distribution from pivot choice set */
		removeDistribution(pivot.state, pivot.c, pivot.d);

	return fresh;
}

void GameGraph::getSuccessors(Distribution u,
		std::vector < vertex_descriptor> &succ) {
	edge_descriptor e;
	for (out_edge_iter_pair it(out_edges(u, graph)); it.first != it.second; ++it.first) {
		e = *it.first;
		succ.push_back(target(e, graph));
	}
}

void GameGraph::getSuccessorStates(vertex_descriptor u,
		std::vector < vertex_descriptor> &succ) {
	edge_descriptor e, f, g;
	for (out_edge_iter_pair it(out_edges(u, graph)); it.first != it.second; ++it.first) {
		e = *it.first;
		vertex_descriptor v(target(e, graph));
		for (out_edge_iter_pair it2(out_edges(v, graph)); it2.first
				!= it2.second; ++it2.first) {
			f = *it2.first;
			vertex_descriptor w(target(f, graph));
			for (out_edge_iter_pair it3(out_edges(w, graph)); it3.first
					!= it3.second; ++it3.first) {
				g = *it3.first;
				succ.push_back(target(g, graph));
			}
		}
	}
}

void GameGraph::getSuccessorStates(vertex_descriptor u, StateSet &succ) {
	edge_descriptor e, f, g;
	for (out_edge_iter_pair it(out_edges(u, graph)); it.first != it.second; ++it.first) {
		e = *it.first;
		vertex_descriptor v(target(e, graph));
		for (out_edge_iter_pair it2(out_edges(v, graph)); it2.first
				!= it2.second; ++it2.first) {
			f = *it2.first;
			vertex_descriptor w(target(f, graph));
			for (out_edge_iter_pair it3(out_edges(w, graph)); it3.first
					!= it3.second; ++it3.first) {
				g = *it3.first;
				succ.insert(target(g, graph));
			}
		}
	}
}

void GameGraph::getChoiceSets(State state, std::vector < ChoiceSet> &choice_sets) {
	assert (isState (state));
	getSuccessors(state, choice_sets);
}

void GameGraph::getDistributions(ChoiceSet choice_set,
		std::vector < Distribution> &distrs) {
	assert (isChoiceSet (choice_set));
	getSuccessors(choice_set, distrs);
}

Action GameGraph::getAction(Distribution distr) {
	assert (isDistribution (distr));
	return graph[distr].p2Prop.action;
}

void GameGraph::getActionSets(State state,
		std::vector < std::vector < int> > &vec) {
	assert (isState (state));
	std::vector < ChoiceSet> choice_sets;
	getChoiceSets(state, choice_sets);
	std::vector < Distribution> distrs;
	for (std::vector<ChoiceSet>::const_iterator it = choice_sets.begin(); it
			!= choice_sets.end(); ++it) {
		distrs.clear();
		getDistributions(*it, distrs);
		std::vector < int> actions;
		for (std::vector<Distribution>::const_iterator dit = distrs.begin(); dit
				!= distrs.end(); ++dit) {
			actions.push_back(getAction(*dit));
		}
		vec.push_back(actions);
	}
}

void GameGraph::getProbChoices(Distribution distr,
		std::vector < std::pair < State,
		double> > &vec) {
	assert (isDistribution (distr));

	for (out_edge_iter_pair oIt(out_edges(distr, graph)); oIt.first
			!= oIt.second; oIt.first++) {
		edge_descriptor e = *oIt.first;
		vertex_descriptor v = target(e, graph);
		double prob = graph[e];
		vec.push_back(std::pair < State, double>(v, prob));
	}

}

void GameGraph::getActionSet(edge_descriptor choice, std::vector < int>&vec) {
	// target of a choice is a player 2 state
	vertex_descriptor u = target(choice, graph);
	std::vector < Distribution> distrs;
	getDistributions(u, distrs);
	for (std::vector<Distribution>::const_iterator dit = distrs.begin(); dit
			!= distrs.end(); ++dit) {
		vec.push_back(getAction(*dit));
	}
}

void GameGraph::getHyperTransition(State state, edge_descriptor choice,
		HyperTransition & ht) {
	State s = source(choice, graph);
	assert (s == state);
	vertex_descriptor v = target(choice, graph);
	std::vector < Distribution> distrs;
	getDistributions(v, distrs);
	ht.transitions.resize(distrs.size());
	for (unsigned i = 0; i < distrs.size(); ++i) {
		getTransition(s, distrs[i], ht.transitions[i]);
	}
}

void GameGraph::getTransition(State s, Distribution distr, Transition & t) {
	t.command_nr = graph[distr].p2Prop.action;
	t.states.push_back(graph[s].stateProp.state_nr);
	for (out_edge_iter_pair oIt(out_edges(distr, graph)); oIt.first
			!= oIt.second; oIt.first++) {
		edge_descriptor e = *oIt.first;
		vertex_descriptor v = target(e, graph);
		t.states.push_back(graph[v].stateProp.state_nr);
	}
}

void GameGraph::printAiSee() const {
	printAiSee(cout);
}

void GameGraph::printAiSee(string filename) const {
	ofstream file(filename.c_str(), ios::out);
	printAiSee(file);
	file.close();
}

/**
 * Prints out game graph in aiSee format to given stream @a stream.
 *
 * @param stream output stream to print GraphViz output to
 */
void GameGraph::printAiSee(ostream & stream) const {
	AiSeePrinter aiSeePrinter(*this, stream);
}



bool GameGraph::checkConsistency() {
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		vertex_descriptor u = *vp.first;
		switch (graph[u].vertexType) {
			case P2:
				assert (out_degree (u, graph)> 0);
				break;

			case P1:
				assert (out_degree (u, graph)> 0);
				break;

			case S:
				break;


		}
	}
	return true;
}


/**
 * Converts game graph to sparse matrix for for model checking.
 * Notice that now the matrix has to be build once for each of the two
 * iterations. However, in both cases the 0-1 precomputation can be taken
 * into account, so overall performance should be better.
 *
 * @param P1min build for minimizing player 1
 */
void GameGraph::toGameSparse(bool P1min) {

	sparse.clear();
	backMap.clear();
	usedVertices.clear();

	/* find out which vertices are to be included in sparse matrix
	 taking information about pre-computations into account
	 */
	foreach(vertex_descriptor u, states) {

		if ((!doExtendYesVector || (1.0 != getProbability(u, P1min)))
				&& (!doExtendNoVector || (0.0 != getProbability(u, P1min)))) {
			usedVertices.insert(u);
			for (out_edge_iter_pair soIt = out_edges(u, graph); soIt.first
					!= soIt.second; soIt.first++) {
				vertex_descriptor v = target(*soIt.first, graph);
				usedVertices.insert(v);
				for (out_edge_iter_pair poIt = out_edges(v, graph); poIt.first
						!= poIt.second; poIt.first++) {
					vertex_descriptor w = target(*poIt.first, graph);
					usedVertices.insert(w);
				}
			}
		}
	}

	/* enumerate distributions, choices and states,
	 * and create bad states vector
	 */
	unsigned distributionNr = 0;
	unsigned choiceSetNr = 0;
	unsigned stateNr = 0;
	vertexMap.clear();
	foreach(State u, usedVertices) {
		switch (graph[u].vertexType) {
		case P2: {
			vertexMap[u] = distributionNr;
			distributionNr++;
			break;
		}
		case P1: {
			vertexMap[u] = choiceSetNr;
			choiceSetNr++;
			break;
		}
		default: {
			vertexMap[u] = stateNr;
			sparse.badStates.push_back(graph[u].stateProp.isBad());
			stateNr++;
			break;
		}
		}
	}

	unsigned sinkStateNr(0);
	unsigned badStateNr(0);
	if (doExtendNoVector) {
		sinkStateNr = stateNr;
		sparse.badStates.push_back(false);
		stateNr++;
	}
	if (doExtendYesVector) {
		badStateNr = stateNr;
		sparse.badStates.push_back(true);
		stateNr++;
	}

	/* fill structures,
	 * create mapping from sparse(states, transitions) to game graph */
	unsigned distributionStart = 0;
	unsigned choiceSetStart = 0;
	unsigned stateStart = 0;

	foreach(vertex_descriptor u, usedVertices) {
		switch (graph[u].vertexType) {
		case P2: {
			backMap.distribution.push_back(u);
			sparse.distributionStart.push_back(distributionStart);
			for (out_edge_iter_pair oIt(out_edges(u, graph)); oIt.first
					!= oIt.second; oIt.first++) {
				edge_descriptor e = *oIt.first;
				vertex_descriptor v = target(e, graph);
				unsigned target;
				if ((!doExtendYesVector || (1.0 != getProbability(v, P1min)))
						&& (!doExtendNoVector || (0.0 != getProbability(v,
								P1min)))) {
					target = vertexMap[v];
				} else {
					target = 0.0 == getProbability(v, P1min) ? sinkStateNr
							: badStateNr;
				}
				backMap.distributionEdge.push_back(e);
				double prob = graph[e];
				sparse.distributionTarget.push_back(target);
				sparse.distributionProb.push_back(prob);
				distributionStart++;
			}
			break;
		}
		case P1: {
			assert (out_degree (u, graph)> 0);
			backMap.choiceSet.push_back(u);
			sparse.choiceSetStart.push_back(choiceSetStart);
			for (out_edge_iter_pair oIt(out_edges(u, graph)); oIt.first
					!= oIt.second; oIt.first++) {
				edge_descriptor e = *oIt.first;
				backMap.choiceSetEdge.push_back(e);
				vertex_descriptor v = target(e, graph);
				unsigned distr = vertexMap[v];
				sparse.choiceSetDistribution.push_back(distr);
				choiceSetStart++;
			}
			break;
		}
		case S: {
			backMap.state.push_back(u);
			sparse.stateStart.push_back(stateStart);
			for (out_edge_iter_pair oIt(out_edges(u, graph)); oIt.first
					!= oIt.second; oIt.first++) {
				edge_descriptor e = *oIt.first;
				backMap.stateEdge.push_back(e);
				vertex_descriptor v = target(e, graph);
				unsigned choice = vertexMap[v];
				sparse.stateChoiceSet.push_back(choice);
				stateStart++;
			}
			break;
		}
		}
	}
	/* add end of last state/choice set/distribution */
	sparse.distributionStart.push_back(distributionStart);
	sparse.choiceSetStart.push_back(choiceSetStart);
	sparse.stateStart.push_back(stateStart);
	if (doExtendNoVector) {
		sparse.stateStart.push_back(stateStart);
	}
	if (doExtendYesVector) {
		sparse.stateStart.push_back(stateStart);
	}
}

// TODO finally, we should be able to work without the action graph as this
//      would fit better into the overall architecture: It's a bit stupid
//      if we try to build only partial sparse matrices and then have to
//      build the action graph for the whole system again.
//      It would also be possible to only generate the action graph for these
//      partial sparse matrices, but this would lead to some complications,
//      because then we would have to append paths generated in these action
//      graphs with other paths to get complete paths from an initial state
//      to a final state. So, i think the other solution is possible better.
void GameGraph::computeActionGraph(ActionGraph & g, bool P1min) {
	map<vertex_descriptor, unsigned> stateMap;
	vector<vertex_descriptor> stateList;

	/* find some (!) initial state */
	State init;
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		vertex_descriptor u = *vp.first;
		if (isState(u) && graph[u].stateProp.isInit()) {
			init = u;
			break;
		}
	}

	/* get set of states reachable from that initial state */
	StateSet reach;
	StateSet toDo;
	reach.insert(init);
	toDo.insert(init);
	while (!toDo.empty()) {
		StateSet::iterator toDoIt = toDo.begin();
		vertex_descriptor u = *toDoIt;
		toDo.erase(toDoIt);
		if (out_degree(u, graph) > 0) {
			vertex_descriptor distr = getChosenDistribution(u, P1min);
			for (out_edge_iter_pair oIt(out_edges(distr, graph)); oIt.first
					!= oIt.second; oIt.first++) {
				edge_descriptor e = *oIt.first;
				vertex_descriptor v = target(e, graph);
				if (reach.find(v) == reach.end()) {
					reach.insert(v);
					toDo.insert(v);
				}
			}
		}
	}

	/* enumerate states, set initial and bad states of action graph */
	g.resize(reach.size());
	unsigned stateNr = 0;
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		vertex_descriptor u = *vp.first;
		if (reach.find(u) != reach.end()) {
			StateProp & stateProp = graph[u].stateProp;

			/** \note annotate "imprecise" states with action sets for refinement */
			if (0 != out_degree(u, graph)) {
				edge_descriptor choice = getChoice(u, P1min);
				edge_descriptor other_choice = getChoice(u, !P1min);

				bool precise = choice == other_choice || getProbability(u,
						false) == getProbability(u, true);
				StateLabel stateLabel(precise);
				if (false && !precise) {
					//                      getHyperTransition(choice,stateLabel.h1);
					//                      getHyperTransition(choice,stateLabel.h2);

					std::vector < int> action_set1;
					std::vector < int> action_set2;
					getActionSet(choice, action_set1);
					getActionSet(other_choice, action_set2);
					stateLabel.action_sets.push_back(action_set1);
					stateLabel.action_sets.push_back(action_set2);
					g.addLabel(stateNr, stateLabel);
				}
			}

			stateMap[u] = stateNr;

			std::unordered_map<State, std::string>::const_iterator ann_it(
					annotation.find(u));
			if (ann_it != annotation.end()) {
				g.setAnnotation(stateNr, ann_it->second);
			}

			stateList.push_back(u);
			if (stateProp.isInit()) {
				g.setInit(stateNr);
			}
			if (stateProp.isBad()) {
				g.setBad(stateNr);
			}
			stateNr++;
		}
	}

	/* create action graph */
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		vertex_descriptor u = *vp.first;
		if (reach.find(u) != reach.end()) {
			if (0 != out_degree(u, graph)) {
				vertex_descriptor distr = getChosenDistribution(u, P1min);
				bool precise = getChoice(u, P1min) == getChoice(u, !P1min);

				unsigned sourceNr = stateMap[u];
				Action action = graph[distr].p2Prop.action;
				/* add probabilisitic decisions */
				unsigned probNr = 0;
				map<unsigned, LabeledTransition> labelMap; // map target states to label
				for (out_edge_iter_pair oIt(out_edges(distr, graph)); oIt.first
						!= oIt.second; oIt.first++) {
					edge_descriptor e3 = *oIt.first;
					vertex_descriptor x = target(e3, graph);
					unsigned targetNr = stateMap[x];
					if (labelMap.find(targetNr) == labelMap.end()) {
						/* if this is the first time this target state is taken, insert
						 * new labeled transition for it.
						 */
						LabeledTransition lt(sourceNr, action, targetNr);
						labelMap.insert(make_pair(targetNr, lt));
					}
					LabeledTransition & lt = labelMap[targetNr];
					lt.setPrecise(precise);
					double prob = graph[e3];
					lt.setDiff(getProbability(x, false) - getProbability(x,
							true));
					//double prob = diff == 0.0 ? 1E-20 : diff;
					lt.Add(prob, probNr);
					probNr++;
				}
				/* insert all the labeled transitions into action graph */
				for (map<unsigned, LabeledTransition>::iterator it =
						labelMap.begin(); it != labelMap.end(); it++) {
					g.addTransition(it->second);
				}
			}
		}
	}

	g.ComputeBackSets();

	if (util::Database::DEBUG) {
		g.printMRMC();
	}
}

void GameGraph::computeConflictingHyperTransitions(std::vector <
std::pair <
HyperTransition,
HyperTransition> >&c) {
	map<vertex_descriptor, unsigned> stateMap;
	vector<vertex_descriptor> stateList;

	/* get set of states reachable from that initial state */
	StateSet reach;
	StateSet toDo;

	/* start with initial states */
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		vertex_descriptor u = *vp.first;
		if (isState(u) && graph[u].stateProp.isInit()) {
			reach.insert(u);
			toDo.insert(u);
			break;
		}
	}

	/* compute the set of states reachable from both(!) schedulers */
	while (!toDo.empty()) {
		StateSet::iterator toDoIt = toDo.begin();
		vertex_descriptor u = *toDoIt;
		toDo.erase(toDoIt);
		if (out_degree(u, graph) > 0) {
			vertex_descriptor distr = getChosenDistribution(u, true);
			for (out_edge_iter_pair oIt(out_edges(distr, graph)); oIt.first
					!= oIt.second; oIt.first++) {
				edge_descriptor e = *oIt.first;
				vertex_descriptor v = target(e, graph);
				if (reach.find(v) == reach.end()) {
					reach.insert(v);
					toDo.insert(v);
				}
			}
			distr = getChosenDistribution(u, false);
			for (out_edge_iter_pair oIt(out_edges(distr, graph)); oIt.first
					!= oIt.second; oIt.first++) {
				edge_descriptor e = *oIt.first;
				vertex_descriptor v = target(e, graph);
				if (reach.find(v) == reach.end()) {
					reach.insert(v);
					toDo.insert(v);
				}
			}
		}
	}

	/* get the places where max and min decision deviate */
	for (std::list<vertex_descriptor>::const_iterator it = states.begin(); it
			!= states.end(); ++it) {
		vertex_descriptor u = *it;
		//if (reach.find(u) != reach.end()) {

		/** \note find places where scheduler decisions deviate */
		if (0 != out_degree(u, graph)) {
			edge_descriptor choice = getChoice(u, true);
			assert (source (choice, graph) == u);
			edge_descriptor other_choice = getChoice(u, false);
			assert (source (other_choice, graph) == u);
			bool precise = choice == other_choice || getProbability(u, false)
					== getProbability(u, true);
			if (!precise) {
				c.resize(c.size() + 1);
				HyperTransition & h1(c.back().first);
				HyperTransition & h2(c.back().second);
				getHyperTransition(u, choice, h1);
				getHyperTransition(u, other_choice, h2);
			}
		}
		//}
	}
}

/* */
void GameGraph::exploreSCC(State root, StrongComponent& sc) {
	int scc_index = sc.scc_index;
	assert(isState(root));
	sc.root = root;

	assert(graph[root].scc_index == scc_index);

	/* get set of states reachable from that initial state */
	StateSet reach;
	StateSet toDo;

	/* start with root state */
	toDo.insert(root);

	while (!toDo.empty()) {
		StateSet::iterator toDoIt = toDo.begin();
		vertex_descriptor u = *toDoIt;
		toDo.erase(toDoIt);
		assert(reach.count(u) == 0);
		assert(graph[u].scc_index == scc_index);
		assert(isState(u));
		reach.insert(u);

		/* carry on exploring the SCC */
		std::vector<vertex_descriptor> succ;
		getSuccessorStates(u, succ);
		bool exit_state(false);

		for (std::vector<vertex_descriptor>::const_iterator i = succ.begin(); i
				!= succ.end(); ++i) {
			vertex_descriptor v(*i);

			if (graph[v].scc_index != scc_index) {
				exit_state = true;
				sc.exit.insert(u);
				sc.target.insert(v);
			} else if (reach.count(v) == 0) {
				toDo.insert(v);
			}
		}

		if (exit_state) {
			edge_descriptor e, f, g;
			vertex_descriptor v, w, x;
			for (out_edge_iter_pair it(out_edges(u, graph)); it.first
					!= it.second; ++it.first) {
				e = *it.first;
				/*
				 * back_into_scc is true if all of its branches lead back into the SCC
				 */
				bool back_into_scc(true);
				//player 1 decision
				v = target(e, graph);
				for (out_edge_iter_pair it2(out_edges(v, graph)); it2.first
						!= it2.second; ++it2.first) {
					//player 2 decision
					f = *it2.first;
					w = target(f, graph);
					for (out_edge_iter_pair it3(out_edges(w, graph)); it3.first
							!= it3.second; ++it3.first) {
						//probabilistic decision
						g = *it3.first;
						x = target(g, graph);
						back_into_scc = back_into_scc && graph[x].scc_index
								== scc_index;
					}
				}
				if (back_into_scc)
					loop_decision.insert(v);
			}
		}
	}
	sc.constituents = reach;
	MSG(0,"GameGraph::exploreSCC: # states of %d\n",reach.size());
}

int GameGraph::computeSCCs() {
	nrOfSCCs = strong_components(graph, get(&VertexProp::scc_index, graph),
			root_map(get(&VertexProp::root, graph)). discover_time_map(get(
					&VertexProp::time, graph)). color_map(get(
					&VertexProp::color, graph)). vertex_index_map(get(
					&VertexProp::index, graph)));

	/* create some statistics

	 * number of SCCs
	 * numer of non-trivial (=more than one node) SCCs
	 */
	/* count the number of nodes per SCC */
	std::map < int, int> SCC_size_counter;
	std::map< int, State> root;

	unsigned non_trivial = 0;

	/* go through the states */
	for (std::list<vertex_descriptor>::const_iterator i = states.begin(); i
			!= states.end(); ++i) {
		vertex_descriptor v = *i;
		++SCC_size_counter[graph[v].scc_index];
		root[graph[v].scc_index] = graph[v].root;
		StateSet succ;
		getSuccessorStates(v, succ);
		// self-loops should be included in non-trivial SCCs
		if (succ.count(v) > 0)
			++SCC_size_counter[graph[v].scc_index];
	}

	for (std::map<int, int>::const_iterator it = SCC_size_counter.begin(); it
			!= SCC_size_counter.end(); ++it) {
		int scc_index = it->first;
		int size = it->second;

		if (size > 1) {
			MSG (0, "scc %d #>1 %d\n", scc_index, size);
			++non_trivial;
			sccs.resize(sccs.size() + 1, scc_index);
			StrongComponent& sc(sccs.back());
			exploreSCC(root[scc_index], sc);
		}
	}

	MSG (0, "SCCs in game %d thereof non-trivial %d \n", nrOfSCCs, non_trivial);

	return nrOfSCCs;

}

void GameGraph::removeCycles() {
	StateSet death_list;
	for (StateSet::iterator i = loop_decision.begin(); i != loop_decision.end(); ++i) {
		State p1(*i);
		StateSet death_list;
		std::vector<vertex_descriptor> succ;
		getSuccessors(p1, succ);
		clear_vertex(p1, graph);
		remove_vertex(p1, graph);
		// TODO: thorough clean up

	}

	for (StateSet::iterator i = death_list.begin(); i != death_list.end(); ++i) {

	}

}

/**
 * Maps actions to states.
 * The parameters @a chosenP1 and @a chosenP2 represent choices of player
 * 1 and 2 respectively. These are usually computed by the Until of the
 * GameSparse datastructure. Parameter @a P1min decides whether
 * player 1 tries to minimize the probability to reach set of target
 * states. For player 2, this parameter is not given, because it is
 * not needed: For one reachability formula, P2 either constantly
 * minimizes or maximizes.
 *
 * These decisions are then saved for each state, for example, for
 * later calculating the respective longest paths.
 * Notice that @a chosenP1 and @a chosenP2 will be modified by this
 * method.
 *
 * @param P1min true iff player 1 tries to minimize
 * @param chosenP1 choices of player 1
 * @param chosenP2 choices of player 2
 */
void GameGraph::mapSparseToGameGraph(bool lower_bound,
									 vector<int>& chosenP1,
								     vector<int>& chosenP2) {
	/* map state probabilities */
	for (unsigned i = 0; i < backMap.state.size(); i++) {
		vertex_descriptor u = backMap.state[i];
		setProbability(u, lower_bound ? lower[i] : upper[i], lower_bound);
	}

	/* map player 1  decisions */
	for (unsigned i = 0; i < backMap.state.size(); i++) {
		vertex_descriptor u = backMap.state[i];
		if (out_degree(u, graph) > 0) {
			int choice = chosenP1[i];
			assert (-1 != choice);
			edge_descriptor e = backMap.stateEdge[choice];
			setChoice(u, e, lower_bound);
			assert (u == source (e, graph));
		}
	}

	/* map player 2 decisions */
	for (unsigned i = 0; i < backMap.choiceSet.size(); i++) {
		vertex_descriptor u = backMap.choiceSet[i];
		int choice = chosenP2[i];
		if (-1 != choice) {
			edge_descriptor e = backMap.choiceSetEdge[choice];
			setChoice(u, e, lower_bound);
			assert (u == source (e, graph));
		} else if (!graph[u].stateProp.isBad() && (out_degree(u, graph) != 0)) {
			cerr << "WARNING: Non-Sink P1 without chosen decision!" << endl;
		}
	}
}

void GameGraph::createTHEBadState() {
	/* add THE bad state */
	bad_state = addState();

	vector<State> bad_states;

	/* add edge from any bad state to THE bad state */
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		Graph::vertex_descriptor v = *vp.first;
		if (v != bad_state) {
			if ((graph[v].vertexType == S) && (graph[v].stateProp.isBad())) {
				bad_states.push_back(v);
			}
		}
	}
	for (unsigned i = 0; i < bad_states.size(); i++) {
		State v = bad_states[i];
		/* remove self-loops etc. if any */
		clear_out_edges(v, graph);
		/* add transition to THE bad state */
		ChoiceSet c1 = createChoiceSet();
		addChoiceSet(v, c1);
		ChoiceSet c2 = createDistribution(-1);
		addDistribution(c1, c2);
		addProbChoice(c2, bad_state, 1.0);
		/* set choices (for path algo) */

		edge_descriptor e = edge(v, c1, graph).first;
		graph[v].P1min_P_choice = edge(v, c1, graph).first;;
		graph[v].P1max_P_choice = edge(v, c1, graph).first;
		graph[c1].P1min_P_choice = edge(c1, c2, graph).first;
		graph[c1].P1max_P_choice = edge(c1, c2, graph).first;
	}

}

void GameGraph::removeTHEBadState() {
	vector<Graph::vertex_descriptor> delete_v;
	for (in_edge_iter_pair it(in_edges(bad_state, graph)); it.first
			!= it.second; ++it.first) {
		edge_descriptor e = *it.first;
		vertex_descriptor u = source(e, graph);
		delete_v.push_back(u);
	}

	for (vertex_iter_pair vp = vertices(graph); vp.first != vp.second; ++vp.first) {
		vertex_descriptor v = *vp.first;
		if (v != bad_state) {
			if (isState(v) && (graph[v].stateProp.isBad())) {
				pair<out_edge_iterator, out_edge_iterator> it = out_edges(v,
						graph);
				edge_descriptor e = *it.first;
				vertex_descriptor u = target(e, graph);
				delete_v.push_back(u);
			}
		}
	}
	delete_v.push_back(bad_state);

	for (unsigned i = 0; i < delete_v.size(); i++) {
		Graph::vertex_descriptor v = delete_v[i];
		clear_vertex(v, graph);
		remove_vertex(v, graph);
	}

}

/**
 * Clears all results
 */
void GameGraph::clearResults() {
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		Graph::vertex_descriptor u = *vp.first;
		if (isState(u)) {
			StateProp & prop = graph[u].stateProp;
			prop.setMinProb(prop.isBad() ? 1.0 : -1.0);
			prop.setMaxProb(prop.getMinProb());
		}
	}
	usedVertices.clear();
}

/**
 * Unbounded until.
 */
void GameGraph::until(bool min) {
	clearResults();
	if (doExtendYesVector) {
		extendYesVec(min,true);
		extendYesVec(min,false);
	}
	if (doExtendNoVector) {
		extendNoVec(min,true);
		extendNoVec(min,false);
	}

	toGameSparse(true);

	lower.resize(sparse.stateStart.size() - 1, 0.0);
	upper.resize(sparse.stateStart.size() - 1, 0.0);

	vector<int> chosenP1(sparse.stateStart.size(), -1);
	vector<int> chosenP2(sparse.choiceSetStart.size(), -1);

	sparse.until(min, true, lower, chosenP1, chosenP2);
	mapSparseToGameGraph(true, chosenP1, chosenP2);

	toGameSparse(false);
	chosenP1.clear();
	chosenP2.clear();
	chosenP1.resize(sparse.stateStart.size(), -1);
	chosenP2.resize(sparse.choiceSetStart.size(), -1);

	sparse.until(min, false, upper, chosenP1, chosenP2);
	mapSparseToGameGraph(false, chosenP1, chosenP2);

	util::Statistics::upper_bound = getMaxResult();
	util::Statistics::lower_bound = getMinResult();

	MSG(0,"#######################\n");
	MSG(0,"game result [%E,%E]\n", util::Statistics::lower_bound, util::Statistics::upper_bound);
	MSG(0,"#######################\n");
}

double GameGraph::getMinResult() {
	double result = 2.0;
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		Graph::vertex_descriptor v = *vp.first;
		if (isState(v) && (graph[v].stateProp.isInit())
				&& (graph[v].stateProp.getMinProb() < result)) {
			result = graph[v].stateProp.getMinProb();
		}
	}

	return result;
}

double GameGraph::getMaxResult() {
	double result = -1.0;
	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		Graph::vertex_descriptor v = *vp.first;
		if (isState(v) && (graph[v].stateProp.isInit())
				&& (graph[v].stateProp.getMaxProb() > result)) {
			result = graph[v].stateProp.getMaxProb();
		}
	}

	return result;
}

/**
 * Check whether @a sub is a subset of @a sup.
 *
 * @param sub check whether subset of @a sup
 * @param sup check whether superset of @a sub
 * @return true iff sub subset of sup
 */
bool GameGraph::subSet(StateSet & sub, StateSet & sup) {
	bool isSub = true;

	for (StateSet::iterator subIt = sub.begin(); subIt != sub.end(); subIt++) {
		vertex_descriptor state = *subIt;
		if (sup.find(state) == sup.end()) {
			isSub = false;
			break;
		}
	}

	return isSub;
}

/**
 * Extends the yes vector.
 * That is, do a precalculation to label all states with "1" which will
 * finally reach the set of bad states with probability 1 and set the
 * decisions for player 1 and 2 respectively.
 *
 * @param P1min whether player 1 shall try to minimize
 * @param P2min whether player 2 shall try to minimize
 */
void GameGraph::extendYesVec(bool P1min, bool P2min) {
	/* collect all bad states and all distributions of a state,
	 * fill stateset u
	 */
	/* Also:
	 * calculate back set of all states, that is all states which somehow
	 * can reach the respective state in one step by some P1 and P2 decision
	 * plus a probabilistic choice */
	StateSet badStates;
	map<vertex_descriptor, StateSet> stateDistr;
	map<vertex_descriptor, StateSet> backMap;
	StateSet vSet;
	StateSet uSet;
	for (vertex_iter_pair vp = vertices(graph); vp.first != vp.second; ++vp.first) {
		vertex_descriptor u = *vp.first;
		/* just select some (!) edge */
		if ((0 != out_degree(u, graph)) && !isDistribution(u)) {
			edge_descriptor e = *out_edges(u, graph).first;
			setChoice(u, e, true);
			setChoice(u, e, false);
		}

		if (isState(u)) {
			uSet.insert(u);
			if (graph[u].stateProp.isBad()) {
				badStates.insert(u);
			}
			VertexSet distrAlready; // distr we already have had
			for (out_edge_iter_pair P1It = out_edges(u, graph); P1It.first
					!= P1It.second; P1It.first++) {
				vertex_descriptor v = target(*P1It.first, graph);
				for (out_edge_iter_pair P2It = out_edges(v, graph); P2It.first
						!= P2It.second; P2It.first++) {
					vertex_descriptor w = target(*P2It.first, graph);
					if (distrAlready.find(w) == distrAlready.end()) {
						for (out_edge_iter_pair distrIt = out_edges(w, graph); distrIt.first
								!= distrIt.second; distrIt.first++) {
							vertex_descriptor x = target(*distrIt.first, graph);
							backMap[x].insert(u);
						}
					}
				}
			}
		} else if (isDistribution(u)) {
			for (in_edge_iter_pair it = in_edges(u, graph); it.first
					!= it.second; it.first++) {
				vertex_descriptor v = source(*it.first, graph);
				for (in_edge_iter_pair it2 = in_edges(v, graph); it2.first
						!= it2.second; it2.first++) {
					vertex_descriptor w = source(*it2.first, graph);
					stateDistr[w].insert(u);
				}
			}
		}
	}

	/* now find yes vector extension */
	bool uDone = false;
	while (!uDone) {
		/* TODO: Is clearing the sets neccessary? Consider a closer look at mu
		 * calculus fix point calculation techniques. Originally, in
		 * PRISM the clearing was done, but that's not a good argument.
		 */
		vSet.clear();
		bool vDone = false;
		while (!vDone) {
			StateSet tmp = badStates;
			for (StateSet::iterator vIt = vSet.begin(); vIt != vSet.end(); vIt++) {
				State state = *vIt;
				StateSet & backSet = backMap[state];
				for (StateSet::iterator bIt = backSet.begin(); bIt
						!= backSet.end(); bIt++) {
					State from = *bIt;
					if (tmp.find(from) == tmp.end()) {
						/* forall distributions of the state */
						VertexSet & distrs = stateDistr[from];
						map<vertex_descriptor, bool> distrOK;
						for (StateSet::iterator dIt = distrs.begin(); dIt
								!= distrs.end(); dIt++) {
							vertex_descriptor distr = *dIt;
							bool leftU = false;
							bool foundState = false;
							/* check distribution for whether
							 *
							 * 1.) it stays in set u
							 * 2.) reaches "state"
							 *
							 * if yes, it is an "OK" distribution
							 */
							for (out_edge_iter_pair oIt = out_edges(distr,
									graph); oIt.first != oIt.second; oIt.first++) {
								vertex_descriptor t = target(*oIt.first, graph);
								if (uSet.find(t) == uSet.end()) {
									leftU = true;
									break;
								} else if (vSet.find(t) != vSet.end()) {
									foundState = true;
								}
							}
							distrOK[distr] = !leftU && foundState;
						}

						/* now for P1/P2 decisions */
						bool P1OK = P1min;
						for (out_edge_iter_pair P2It(out_edges(from, graph)); P2It.first
								!= P2It.second; P2It.first++) {
							bool P2OK = P2min;
							edge_descriptor e = *P2It.first;
							vertex_descriptor decision = target(e, graph);
							for (out_edge_iter_pair distIt = out_edges(
									decision, graph); distIt.first
									!= distIt.second; distIt.first++) {
								edge_descriptor eDistr = *distIt.first;
								vertex_descriptor distr = target(eDistr, graph);
								if ((P2min && !distrOK[distr]) || (!P2min
										&& distrOK[distr])) {
									P2OK = !P2min;
									setChoice(decision, eDistr, P1min);
									break;
								}
							}
							if ((P1min && !P2OK) || (!P1min & P2OK)) {
								P1OK = !P1min;
								setChoice(from, e, P1min);
								break;
							}
						}
						if (P1OK) {
							tmp.insert(from);
						}
					}
				}
			}
			vDone = subSet(tmp, vSet);
			vSet.swap(tmp);
		}
		uDone = subSet(uSet, vSet);
		uSet.swap(vSet);
	}

	/* now extend the vector */
	for (StateSet::iterator it = uSet.begin(); it != uSet.end(); it++) {
		setProbability(*it, 1.0, P1min);
	}
	cout << "YES_VECTOR SIZE: " << uSet.size() << endl;
}
}

/**
 * Extends the no vector.
 * That is, do a precalculation to label all states with "0" which will
 * never reach the set of bad states and set the decisions for
 * player 1 and 2 respectively.
 *
 * @param P1min whether player 1 shall try to minimize
 * @param P2min whether player 2 shall try to minimize
 */
void GameGraph::extendNoVec(bool P1min, bool P2min) {
	/* collect all bad states and all distributions of a state,
	 * fill stateset u
	 */
	/* Also:
	 * calculate back set of all states, that is all states which somehow
	 * can reach the respective state in one step by some P1 and P2 decision
	 * plus a probabilistic choice */
	StateSet badStates;
	map<vertex_descriptor, StateSet> stateDistr;
	map<vertex_descriptor, StateSet> backMap;
	StateSet vSet;
	StateSet uSet;
	for (vertex_iter_pair vp = vertices(graph); vp.first != vp.second; ++vp.first) {
		vertex_descriptor u = *vp.first;
		if (isState(u)) {
			uSet.insert(u);
			if (graph[u].stateProp.isBad()) {
				badStates.insert(u);
			}
			VertexSet distrAlready; // distr we already have had
			for (out_edge_iter_pair P1It = out_edges(u, graph); P1It.first
					!= P1It.second; P1It.first++) {
				vertex_descriptor v = target(*P1It.first, graph);
				for (out_edge_iter_pair P2It = out_edges(v, graph); P2It.first
						!= P2It.second; P2It.first++) {
					vertex_descriptor w = target(*P2It.first, graph);
					if (distrAlready.find(w) == distrAlready.end()) {
						for (out_edge_iter_pair distrIt = out_edges(w, graph); distrIt.first
								!= distrIt.second; distrIt.first++) {
							vertex_descriptor x = target(*distrIt.first, graph);
							backMap[x].insert(u);
						}
					}
				}
			}
		} else if (P2 == graph[u].vertexType) {
			for (in_edge_iter_pair it = in_edges(u, graph); it.first
					!= it.second; it.first++) {
				vertex_descriptor v = source(*it.first, graph);
				for (in_edge_iter_pair it2 = in_edges(v, graph); it2.first
						!= it2.second; it2.first++) {
					vertex_descriptor w = source(*it2.first, graph);
					stateDistr[w].insert(u);
				}
			}
		}
	}

	/* now find states which can reach bad states with nonzero
	 * probability */
	StateSet reach = badStates;
	bool done = false;
	while (!done) {
		StateSet tmp = reach;
		for (StateSet::iterator reachIt = reach.begin(); reachIt != reach.end(); reachIt++) {
			State state = *reachIt;
			StateSet & backSet = backMap[state];
			for (StateSet::iterator backIt = backSet.begin(); backIt
					!= backSet.end(); backIt++) {
				State from = *backIt;
				if (tmp.find(from) == tmp.end()) {
					/* forall distributions of the state */
					StateSet & distrs = stateDistr[from];
					map<vertex_descriptor, bool> distrOK;
					for (StateSet::iterator dIt = distrs.begin(); dIt
							!= distrs.end(); dIt++) {
						vertex_descriptor distr = *dIt;
						bool found = false;
						/* check distribution for whether reaching a state in
						 * "reach" is possible
						 * if yes, it is an "OK" distribution
						 */
						for (out_edge_iter_pair oIt = out_edges(distr, graph); oIt.first
								!= oIt.second; oIt.first++) {
							vertex_descriptor t = target(*oIt.first, graph);
							if (reach.find(t) != reach.end()) {
								found = true;
								break;
							}
						}
						distrOK[distr] = found;
						if (found) {
							cout << "ADSF" << endl;
						}
					}

					/* now for P1/P2 decisions */
					bool P1OK = P1min;
					for (out_edge_iter_pair P2It(out_edges(from, graph)); P2It.first
							!= P2It.second; P2It.first++) {
						bool P2OK = P2min;
						edge_descriptor e = *P2It.first;
						vertex_descriptor decision = target(e, graph);
						for (out_edge_iter_pair distIt = out_edges(decision,
								graph); distIt.first != distIt.second; distIt.first++) {
							edge_descriptor eDistr = *distIt.first;
							vertex_descriptor distr = target(eDistr, graph);
							if ((P2min && !distrOK[distr]) || (!P2min
									&& distrOK[distr])) {
								P2OK = !P2min;
								// no set choice here, we don't need it
								break;
							}
						}
						if ((P1min && !P2OK) || (!P1min & P2OK)) {
							P1OK = !P1min;
							// no set choice here, we don't need it
							break;
						}
					}
					if (P1OK) {
						tmp.insert(from);
					}
				}
			}
		}

		if (tmp == reach) {
			done = true;
		}
		reach.swap(tmp);
	}

	/* now mark all states NOT in reach */
	for (vertex_iter_pair vp = vertices(graph); vp.first != vp.second; ++vp.first) {
		State state = *vp.first;
		if (isState(state)) {
			if (reach.find(state) == reach.end()) {
				setProbability(state, 0.0, P1min);
			}
		}
	}
}

inline void printGMLNode(ostream & stream, int id, const std::string & label,
		int width, int height, const std::string & shape,
		const std::string & fill) {
	stream << "node [\n " << "  id " << id << "\n" << "  graphics [\n"
			<< "     w " << width << "\n" << "     h " << height << "\n"
			<< "     shape \"" << shape << "\"\n" << "     fill \"" << fill
			<< "\"\n" << "  ]\n" << "  LabelGraphics \n" << "  [ text \""
			<< label << "\"\n" << "    fill \"#FFFFCC\" ]\n" << "]\n";
}

inline void printGMLNode(ostream & stream, int id, const std::string & label,
		const std::string & infobox, int width, int height,
		const std::string & shape, const std::string & fill) {
	stream << "node [\n " << "  id " << id << "\n" << "  graphics [\n"
			<< "     w " << width << "\n" << "     h " << height << "\n"
			<< "     shape \"" << shape << "\"\n" << "     fill \"" << fill
			<< "\"\n" << "  ]\n" << "  LabelGraphics \n" << "  [ text \""
			<< label << "\"\n" << "    fill \"#FFFFCC\" ]\n" << "  graph [\n"
			<< "    node [\n" << "    label \"subgraph\"\n"
			<< "            LabelGraphics [\n" << "              text \""
			<< infobox << "\"\n" << "            ]\n" << "    ]\n" << "  ]\n"
			<< "]\n";
}

inline void printGMLEdge(ostream & stream, int source, int target,
		const std::string & label, int width,
		// "line", "dashed", "dotted"
		const std::string & style,
		//"delta", "standard", "diamond", "short", "white_delta", "white_diamond", or "none"
		const std::string & sourceArrow,
		//"delta", "standard", "diamond", "short", "white_delta", "white_diamond", or "none"
		const std::string & targetArrow, const std::string & fill) {
	stream << "edge [\n " << "  source " << source << "\n" << "  target "
			<< target << "\n" << "  graphics [\n" << "     width " << width
			<< "\n" << "     style \"" << style << "\"\n"
			<< "     sourceArrow \"" << sourceArrow << "\"\n"
			<< "     targetArrow \"" << targetArrow << "\"\n" << "     fill \""
			<< fill << "\"\n" << "  ]\n" << "  LabelGraphics \n"
			<< "  [ text \"" << label << "\" ]\n" << "]\n";
}

char gradient[] = { '0', '4', '8', 'A', 'C', 'D', 'F' };

std::string getGMLColor(vertex_descriptor v, const VertexProp & prop) {

	std::string color = "#FFFFFF";
	// if this is a single-component SCC we just color it white
	if (prop.root == v) {
		color += "FFFFFF";
	} else {
		color[0] = gradient[prop.scc_index % 7];
		color[1] = gradient[(2 * prop.scc_index) % 7];
		color[2] = gradient[(3 * prop.scc_index) % 7];
		color[3] = gradient[(5 * prop.scc_index) % 7];
		color[4] = gradient[(11 * prop.scc_index) % 7];
		color[5] = gradient[(13 * prop.scc_index) % 7];
	}
	return color;
}

/**
 * Prints out game graph in GML format to given stream @a stream.
 *
 * @param stream output stream to print GML output to
 */
void GameGraph::printGML(ostream & stream) {
	stream << " Creator \"PASS\"\n" << "graph [\n" << "  hierarchic 1\n"
			<< "  directed   1\n";

	for (vertex_iter_pair vp(vertices(graph)); vp.first != vp.second; ++vp.first) {
		vertex_descriptor v = *vp.first;
		switch (graph[v].vertexType) {
		case S: {

			bool init(graph[v].stateProp.isInit());
			bool bad(graph[v].stateProp.isBad());
			double max(getProbability(v, false)), min(getProbability(v, true));
			std::string label;
			int width = 40;
			int height = 40;
			std::string color(getGMLColor(v, graph[v]));
			std::string shape("rectangle");
			if (init) {
				width = 80;
				height = 80;
				label += "init ";
				color = "#99FF00";
				shape = "hexagon";
			} else if (bad) {
				width = 80;
				height = 80;
				label += "bad ";
				color = "#FF0000";
			}

			if (min == max) {
				label += util::floatToString(min);
			} else {
				label += "[" + util::floatToString(min) + ","
						+ util::floatToString(max) + "]";
			}

			std::unordered_map<State, std::string>::const_iterator it(
					annotation.find(v));
			if (it != annotation.end()) {
				printGMLNode(stream, (int) v, label, it->second, label.size()
						* 10, height, shape, color);

			} else {
				printGMLNode(stream, (int) v, label, label.size() * 10, height,
						shape, color);
			}

		}
			break;
		case P1: {
			printGMLNode(stream, (int) v, "", 10, 10, "diamond", "#000000");
		}
			break;
		default: {
			int action(graph[v].p2Prop.action);
			std::string label(util::intToString(action));
			printGMLNode(stream, (int) v, label, label.size() * 10, 10,
					"triangle", "#FFFFCC");
		}
		}
	}

	/* output edges of graph, mark choices of graph */
	for (vertex_iter_pair vp = vertices(graph); vp.first != vp.second; ++vp.first) {
		vertex_descriptor v = *vp.first;
		vertex_descriptor u;
		edge_descriptor e;
		int edge_nr = 0;
		for (out_edge_iter_pair it(out_edges(v, graph)); it.first != it.second; ++it.first) {
			e = *it.first;

			u = target(e, graph);
			/* output "from" state */

			std::string label;
			int width;
			std::string style;
			std::string sourceArrow;
			std::string targetArrow;
			std::string fill;

			switch (graph[v].vertexType) {
			case S:
			case P1: {
				edge_descriptor min_choice;
				edge_descriptor max_choice;
				min_choice = graph[v].P1min_P_choice;
				max_choice = graph[v].P1max_P_choice;

				bool is_min = (e == min_choice);
				bool is_max = (e == max_choice);

				sourceArrow = targetArrow = "none";

				if (is_min && is_max) {
					fill = "#800080"; // purple
					width = 3;
				} else if (is_min) {
					fill = "#0000FF"; // blue
					width = 2;
				} else if (is_max) {
					fill = "#FF0000"; // red
					width = 2;
				} else {
					fill = "#000000"; // black
					width = 1;
				}
			}
				break;
			case P2:
				sourceArrow = "none";
				targetArrow = "standard";
				label = util::floatToString(graph[e]);
				width = 1;
				fill = "#000000";
				break;
			}

			/* end of TODO */

			printGMLEdge(stream, (int) v, (int) u, label, width, style,
					sourceArrow, targetArrow, fill);

			edge_nr++;
		}
	}

	stream << "]\n";
}
