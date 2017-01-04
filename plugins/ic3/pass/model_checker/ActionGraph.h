#ifndef ACTION_GRAPH_H
#define ACTION_GRAPH_H


#include <vector>

#include "Witness.h"

namespace model_checker {
  class ShortestPaths;
  class CandidateSet;


  class LabeledTransition {
  public:
	LabeledTransition();
	LabeledTransition(const LabeledTransition& lt);
	LabeledTransition& operator=(const LabeledTransition& lt);
	LabeledTransition(unsigned __source, int __label, unsigned __target);


	bool isNull() const { return state == -1; }
	LabeledTransition& operator+=(const LabeledTransition& lt);

	void Add(double __prob, int __label);
	int getLabel() const { return label; }
	int getState() const { return state; }
	void setState(int s) { state = s ; }

	double getProb() const { return prob; }
	unsigned getFrom() const { return source; }
	void setFrom( unsigned s ) { source = s; }
	unsigned getTo()   const { return target; }
	const std::vector<int>& getProbChoices() const { return prob_choices; }
	void clearProbChoices() { prob_choices.clear(); }
	std::string toString() const;


	std::string toGraphML() const;

	void setPrecise(bool b) { precise = b; }
	bool isPrecise() const { return precise; }

	inline void setDiff(double p) { diff = p; }
	inline double getDiff() const { return diff; }

	inline void addWitness(const Witness& wit) { w.push_back(wit); }
	inline const std::vector<Witness>& getWitnesses() const { return w; }


  private:
	friend class MDPSparse;
	int state;
	unsigned source;
	int label;
	double prob;
	double diff;
	std::vector<int> prob_choices;
	unsigned target;
	bool precise;
	std::vector<Witness> w;
  };

  struct StateLabel {

	StateLabel(double __diff) : diff (__diff) { }
	StateLabel() : diff(0.0) {}

	double diff;

	std::string toString() const { return util::floatToString(diff); }

  };




  class ActionGraph {
  public:
	ActionGraph() {}
	ActionGraph(unsigned num_states);

	void PrintDOT();
	void printMRMC();

	/* graph file export */

	std::string toString() const;
	std::string toTGF() const;
	std::string toGraphML() const;

	void initShortestPaths(double prob, ShortestPaths&,unsigned);
	void ComputeBackSets();
	double getTransProb(unsigned from, unsigned to);

	inline unsigned getNumStates() const { return num_states; }

	inline bool validState(unsigned s) const {
		return s>=0 && s<num_states;
	}

	inline int getAction(unsigned state) { return actions[state]; }

	//void setAction(unsigned state, int action);

	void addLabel(unsigned state, StateLabel label);
	bool hasLabel(unsigned state) const;
	const StateLabel& getLabel(unsigned state) const;

	void setAnnotation(unsigned state, const std::string& ann) {
		annotation[state] = ann;
	}
	std::string getAnnotation(unsigned state) {
		return annotation[state];
	}

	void setInit(unsigned init);
	void setBad(unsigned bad);
	void addTransition(const LabeledTransition& lt) ;

	void resize(unsigned num_states);

  private:
	friend class MDPSparse;
	friend class ShortestPaths;
	typedef std::map<unsigned,StateLabel> StateLabelMap;
	StateLabelMap label;

	typedef std::pair<unsigned,unsigned> Edge;
	typedef std::map<Edge,double> WeightMap;
	typedef std::map<Edge,LabeledTransition> TransitionMap;
	WeightMap weight;
	TransitionMap transition;

	typedef std::map<unsigned,std::string> AnnotationMap;
	AnnotationMap annotation;


	/* number of states the graph has */
	/* states go from 0 to num_states - 1 */
	unsigned num_states;
	/* gives the action for each state */
	std::vector<int> actions;
	/* the initial state */
	unsigned init;


	/* the transitions of the graph */
	/* for each state we have a vector of transitions, whereby each
	* transition consists of the target state (unsigned) and the
	* probability to go there. */
	std::vector<std::vector<LabeledTransition > > trans;
	unsigned bad_state;
	std::vector<std::vector<unsigned> > back_sets;

	//void BFRelax(unsigned u, unsigned v, double w, pred::ShortestPaths &paths);
	void BellmanFord(ShortestPaths &paths);
	double NextPath(unsigned v, unsigned k, ShortestPaths &paths,
			std::vector<CandidateSet> &candidates);

    };

  class ShortestPaths {
  public:
    ActionGraph* action_graph;
    std::vector<CandidateSet> candidates;
    unsigned bad_state;
    unsigned init;
    unsigned getNumPaths() const;
    double getPath(unsigned nr, std::vector<LabeledTransition >&);
    /* k starts from 0 here! */
    /* first element of pair is state number, second is k */
    std::vector<std::vector<std::pair<unsigned,unsigned> > > back;
    std::vector<std::vector<double> > prob;
    unsigned num;
    unsigned available;

  };

  class CandidateSet {
  public:
    std::vector<std::pair<unsigned,unsigned> > back;
    std::vector<double> prob;
    // we don't need to save v as it is clear from context
    // TODO: use heap?
  };

}

#endif
