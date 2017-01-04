#include "util/Util.h"
#include "util/Error.h"
#include "util/Database.h"
#include "util/Error.h"
#include <iostream>
#include <cmath>
#include "ActionGraph.h"
#include <algorithm>

#include "bdd/BDD.h"
#include "bdd/ODD.h"
#include "MDPSparse.h"
#include <limits.h>

using namespace std;

namespace model_checker {

LabeledTransition::LabeledTransition()
  { prob = 0; precise = false; state = -1;}
LabeledTransition::LabeledTransition(const LabeledTransition& lt) { *this = lt; }
LabeledTransition& LabeledTransition::operator=(const LabeledTransition& lt) {
	prob = lt.prob;
	prob_choices = lt.prob_choices;
	state = lt.state;
	source = lt.source;
	label = lt.label;
	target = lt.target;
	precise = lt.precise;
	diff = lt.diff;
	w = lt.w;
	return *this;
}

LabeledTransition::LabeledTransition(unsigned __source, int __label, unsigned __target)
: source(__source), label(__label), prob(0), target(__target), precise(false), state(-1) {}
void LabeledTransition::Add(double __prob, int __label) {
	if(__prob == 0.0) return;
	prob += __prob;
	prob_choices.push_back(__label);
}


LabeledTransition& LabeledTransition::operator+=(const LabeledTransition& lt) {
	assert(source == lt.source);
	assert(target == lt.target);
	assert(state == lt.state);
	prob += lt.prob;
	prob_choices.insert(prob_choices.end(),lt.prob_choices.begin(),lt.prob_choices.end());
	precise &= lt.precise;
	w.insert(w.end(),lt.w.begin(),lt.w.end());
	return *this;
}


std::string LabeledTransition::toString() const {
	std::string result = "  " + util::intToString(source) + "->"
				+ util::intToString(target) + "[" +
				+ (isPrecise() ? "" : "style=\"dashed\", ")
				+ "label=\""
                                + util::intToString(label)+"{";
	result += "B"+util::intToString(state)+ " ";
	for(unsigned i=0; i<prob_choices.size();++i) {
		if(i>0) result +=",";
		result += util::intToString(prob_choices[i]);
	}
	result += "}, p: ";
	result += util::floatToString(prob);
	result += "\"];";
	return result;
}

  /**
   * Write action graph to MRMC model.
   * The files "debug.*" where ".*" = ".lab", ".input" and ".tra" are
   * created. Run MRMC by
   *
   * $PATH_TO_MRMC/mrmc dtmc debug.tra debug.lab < debug.input
   */
void ActionGraph::printMRMC() {
	string labFilename = "debug.lab";
	string inputFilename = "debug.input";
	string traFilename = "debug.tra";

	/* create label file */
	ofstream labFile(labFilename.c_str(), ios::out);
	for (unsigned stateNr = 0; stateNr < num_states; stateNr++) {
		labFile << (stateNr + 1);
		if (stateNr == init) {
			labFile << " init";
		}
		if (stateNr == bad_state) {
			labFile << " bad";
		}
		labFile << endl;
	}

	/* create input formula file */
	ofstream inputFile(inputFilename.c_str(), ios::out);
	inputFile << "P{<=1.0}[tt U bad]" << endl;
	inputFile << "$RESULT[" << (init + 1) << "]" << endl;
	inputFile << "quit" << endl;

	/* create transition file */
	ofstream traFile(traFilename.c_str(), ios::out);
	traFile << "STATES " << num_states << endl;
	unsigned numTrans = weight.size();

	traFile << "TRANSITIONS " << numTrans << endl;

	for(WeightMap::const_iterator it = weight.begin(); it!=weight.end();++it) {
			const Edge& p(it->first);
			unsigned from (p.first);
			unsigned to(p.second);
			double prob(it->second);
		traFile << (from + 1) << " " << (to + 1) << " " << prob << endl;
	}
}

ActionGraph::ActionGraph(unsigned __num_states) {


	num_states = __num_states;
	actions.resize(num_states);
	for(unsigned i = 0; i<num_states; ++i) {
		actions[i] = -1;
	}
	init = UINT_MAX;
	bad_state = UINT_MAX;
}

void ActionGraph::resize(unsigned __num_states) {
	num_states = __num_states;
	actions.clear();
	actions.resize(num_states,-1);
}

void ActionGraph::setInit(unsigned __init) {
	// check if we got a valid state
	assert(validState(__init));
	init = __init;
}

void ActionGraph::setBad(unsigned __bad) {
	// check if we got a valid state
	assert(validState(__bad));
	bad_state = __bad;
	actions[bad_state] = -1;
}

void ActionGraph::addTransition(const LabeledTransition& lt) {
	unsigned from = lt.getFrom();
	unsigned to = lt.getTo();
	double prob = lt.getProb();
	// check if we got a valid state
	if(!validState(from)) {
		MSG(0,"ActionGraph::addTransition: from %d not within [0,%d]\n",from,num_states);
		assert(false);
	}
	assert(validState(to));
	assert(prob>=0);

	Edge e(from,to);
	
	TransitionMap::iterator tit ( transition.find(e) );
	if( tit == transition.end() ) {
		transition[e] = lt;
		weight[e] = lt.getProb();
	}
	else {
		tit->second += lt;
		weight[e] += lt.getProb();
	}
}

void ActionGraph::PrintDOT() {
	cout << toString() ;
}

std::string ActionGraph::toString() const {
	std::string result;
	result += "digraph ActionGraph {\n" ;

	vector<bool> is_init(num_states,false);
	is_init[init] = true;

	for (unsigned state_nr = 0; state_nr < num_states; state_nr++) {
		result += "  " + util::intToString( state_nr ) + "  [";
		StateLabelMap::const_iterator it =label.find(state_nr);
		if( it!=label.end()) {
			result += "label=\" " + it->second.toString() + "\"";
		}
		if (is_init[state_nr]) {
			result += ",shape=box,";
		}
		if (bad_state == state_nr) {
			result += ",style=filled,color=\"0.7 0.3 1.0\"";
		}
		result += "];\n";
	}
	for(TransitionMap::const_iterator it = transition.begin(); it!=transition.end();++it) {
		const LabeledTransition& edge = it->second;
		result += edge.toString()+"\n";
	}
	result += "}\n";
	return result;
}

std::string ActionGraph::toTGF() const {
	std::string result;
	for (unsigned state_nr = 0; state_nr < num_states; state_nr++) {
		result += util::intToString( state_nr ) + " ";
		if (init == state_nr) {
			result += "INIT";
		}
		if (bad_state == state_nr) {
			result += "BAD";
		}
		result += "\n";
	}
	result += "#\n";
	for(TransitionMap::const_iterator it = transition.begin(); it!=transition.end();++it) {
		const LabeledTransition& edge = it->second;
		double p (edge.getProb());
		unsigned from(edge.getFrom());
		unsigned to(edge.getTo());
		bool precise(edge.isPrecise());
		int label (edge.getLabel());
		result += util::intToString(from) + " " + util::intToString(to) + " " +
			util::floatToString(p) + " " + util::intToString(label) + "\n";
	}
	result += "\n";
	return result;
}



/**
* Relaxation procedure for Bellman-Ford shortest path algo.
* Notice that we don't compute the shortest but the most probable
* path.
*/
inline void BFRelax
(unsigned u, unsigned v, double w, model_checker::ShortestPaths &paths) {
	double w2 ( paths.prob[u][0] * w );

	if (paths.back[v][0].first != UINT_MAX) {
		if (paths.prob[v][0] < w2) {
			paths.prob[v][0] = w2;
			paths.back[v][0].first = u;
		}
	} else {
		/* we need this part because we use multiplication instead of
		* addition */
		paths.prob[v][0] = w2;
		paths.back[v][0].first = u;
	}
}

/**
* Computes the most probable path by modified Bellman-Ford algo.
*/
void ActionGraph::BellmanFord(model_checker::ShortestPaths &paths) {
	for (unsigned state_nr = 0; state_nr < num_states; state_nr++) {
		pair<unsigned,unsigned> back_st(UINT_MAX,0);
		vector<pair<unsigned,unsigned> > back;
		back.push_back(back_st);
		paths.back.push_back(back);
		vector<double> length;
		length.push_back(0.0);
		paths.prob.push_back(length);
	}
	paths.prob[init][0] = 1.0;

	for (unsigned i = 0; i < num_states - 1; ++i) {
		for(WeightMap::const_iterator it = weight.begin(); it!=weight.end();++it) {
			const Edge& p(it->first);
			unsigned from (p.first);
			unsigned to(p.second);
			double prob(it->second);
			BFRelax(from, to, prob, paths);
		}
	} /* we don't need the second part of Bellman-Ford algorithm
	* because we don't have negative probabilities :-) */
}

/**
* Computes back set for each state of the graph.
*/
void ActionGraph::ComputeBackSets() {
	back_sets.clear();
	back_sets.resize(num_states);
	for(WeightMap::const_iterator it = weight.begin(); it!=weight.end();++it) {
		const Edge& p(it->first);
		unsigned from (p.first);
		unsigned to(p.second);
		back_sets[to].push_back(from);
	}
}

/**
* Return the (direct) transition probability from @a from to @a to.
* Returns 0.0 if there is no direct transition.
*
* @param from state to go from
* @param to state to go to
* @return transition probability
*/
double ActionGraph::getTransProb(unsigned from, unsigned to)
{
	double result = 0.0;
	Edge e(from,to);
	WeightMap::const_iterator it = weight.find(e);
	if(it!=weight.end()) {
		result = it->second;
	}
	return result;
}

/**
* Computes the @a k st probable path to @a v.
*
* @return the probability of that path
*/
double ActionGraph::NextPath (
	unsigned v,
	unsigned k,
	model_checker::ShortestPaths &paths,
	vector<CandidateSet> &candidates)
{
	/* B.1 */
	if (1 == k) {
		candidates[v].back.clear();
		candidates[v].prob.clear();
		for (unsigned u_nr = 0; u_nr < back_sets[v].size(); u_nr++) {
			unsigned u = back_sets[v][u_nr];
			if (paths.back[v][0].first != u) {
				pair<unsigned,unsigned> back(u, 0);
				double prob = paths.prob[u][0] * getTransProb(u, v);
				candidates[v].back.push_back(back);
				candidates[v].prob.push_back(prob);
			}
		}
	}

	/* !B.2 */
	if (!((v == init) && (1 == k))) {
		/* B.3 */
		unsigned u = paths.back[v][k - 1].first;
		unsigned k_p = paths.back[v][k - 1].second;

		/* B.4 */
		bool exists = false;
		if (paths.back[u].size() <= k_p + 1) {
			if (NextPath(u, k_p + 1, paths, candidates) > 0.0) {
			exists = true;
			}
		} else {
			exists = true;
		}

		/* B.5 */
		if (exists) {
			pair<unsigned,unsigned> back(u, k_p + 1);
			assert(u < paths.prob.size());
			assert(k_p + 1 < paths.prob[u].size());
			double prob = paths.prob[u][k_p + 1] * getTransProb(u, v);
			candidates[v].back.push_back(back);
			candidates[v].prob.push_back(prob);
		} else {
			MSG(1,"ActionGraph::NextPath: B.5 Path does not exist\n");
		}
	}

	/* B.6 */
	double prob = 0.0;
	if (0 != candidates[v].back.size()) {
		unsigned cand = UINT_MAX;
		/* search path with highest probability */
		for (unsigned cand_nr = 0; cand_nr < candidates[v].back.size();
			cand_nr++) {
			if (candidates[v].prob[cand_nr] > prob) {
				cand = cand_nr;
				prob = candidates[v].prob[cand_nr];
			}
		}
		if(cand!=UINT_MAX) {
			pair<unsigned,unsigned> back = candidates[v].back[cand];
			paths.back[v].push_back(back);
			paths.prob[v].push_back(prob);
			candidates[v].back.erase(candidates[v].back.begin() + cand);
			candidates[v].prob.erase(candidates[v].prob.begin() + cand);
		} else {
			MSG(1,"ActionGraph::NextPath: B.6 -within condition- no path found\n");
			assert(false);
		}
	} else {
		MSG(1,"ActionGraph::NextPath: B.6 no path\n");
	}

	return prob;
}

/**
* Computes a set of shortest paths such that their probability is
* larger or equal to prob.
*/
void ActionGraph::initShortestPaths(double prob, ShortestPaths& paths, unsigned num) {

	/* A.1 */
	BellmanFord(paths);

	/* A.2 */
	paths.candidates.resize(num_states);
	paths.bad_state = bad_state;
	paths.init = init;
	paths.action_graph = this;
	paths.available = 1;
	paths.num = num;
}

unsigned ShortestPaths::getNumPaths() const
{
	return num;
}



double ShortestPaths::getPath(unsigned k, vector<LabeledTransition >& result)
{
	assert(action_graph);

	//action_graph->dgraph.PrintOut(std::cout);
// 	CQYKShortestPaths shortest_paths(action_graph->dgraph, init, bad_state, k+1);
// 	MSG(0,"Computing shortest paths: %d %d \n",init,bad_state);
// 	pfade = shortest_paths.getTopKShortestPaths();
// 	if(k >= pfade.size())
// 		return 0.0;
//
// 	MSG(0,"ActionGraph::initShortestPaths: nr of paths %d\n",pfade.size());
//
// 	double cost ( action_graph->translatePath(pfade[k],result) );
//
// 	MSG(0,"ShortestPaths::getPath: k=%d cost %E\n",k,cost);
// 	return cost;



	if(!action_graph->validState(bad_state)) {
		MSG(0,"bad state not initialized\n");
		assert(false);
	}

	if(!action_graph->validState(init)) {
		MSG(0,"initial state not initialized\n");
		assert(false);
	}

	for(unsigned i= available+1; i<=k; ++i) {
				
		double p = action_graph->NextPath(bad_state, i, *this, candidates);
		if(p == 0) {
			MSG(0,"getPath: suspicious path with probability 0\n");		
			return p;
		}
	}

	available = k;

	result.clear();

	if(bad_state == init)
		MSG(0,"suspicious: initial state %d is bad %d \n",init,bad_state);

	double measure (1.0);

	/*
		create the path
	*/
	for(unsigned k_p = k, state = bad_state;
		     state != init ; ) {
		// DEBUGGING
		if(state >= back.size()) {
			return 0;			
			MSG(0,"%d\n",bad_state);
			MSG(0,"%d\n",init);
			MSG(0,"%d %d\n",state, back.size());
			assert(false);
		}
		if(k_p >= back[state].size()) {
			return 0;
			MSG(0,"k_p=%d back[%d].size()=%d\n",k_p,state,back[state].size());
			assert(false);
		}
		assert(back[state][k_p].second < back[state].size());
		// -- DEBUGGING

		unsigned next_state = state;
		unsigned current_state = back[state][k_p].first;
		ActionGraph::Edge e(current_state,next_state);
		result.push_back(action_graph->transition[e]);
		k_p = back[state][k_p].second;
		state = current_state;

		measure *= action_graph->transition[e].getProb();
	}
	reverse(result.begin(),result.end());
	if(result.size() > 0) {
		assert(result[0].getFrom() == init);
		assert(result[result.size()-1].getTo() == bad_state);
	}
	/*
	if(measure != prob[bad_state][k]) {
		MSG(0,"ShortestPaths::getPath: measure difference %E (%E - %E) \n",measure - prob[bad_state][k],measure,prob[bad_state][k]);
	}
	*/
	return measure /* prob[bad_state][k] */;
}

void ActionGraph::addLabel(unsigned state, StateLabel a) { label[state] = a; }

bool ActionGraph::hasLabel(unsigned state) const { return label.find(state)!=label.end(); }

const StateLabel& ActionGraph::getLabel(unsigned state) const {
	StateLabelMap::const_iterator it = label.find(state);
	if(it!= label.end()) {
		return it->second;
	} else {
		throw util::RuntimeError("ActionGraph::getLabel(): state is unlabled");
	}
}

}
