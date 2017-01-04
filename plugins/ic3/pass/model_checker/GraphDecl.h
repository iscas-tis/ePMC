#ifndef GRAPH_DECL_H
#define GRAPH_DECL_H

#include <boost/graph/graph_traits.hpp>
#include <boost/graph/adjacency_list.hpp>
#include <boost/graph/dijkstra_shortest_paths.hpp>

namespace model_checker {
  typedef int Action;
  struct VertexProp;

  /** \brief Tripartite dynamic graph */
  typedef boost::adjacency_list<boost::listS, boost::listS, boost::bidirectionalS, VertexProp, double> Graph;

  typedef boost::graph_traits<Graph> traits;

  typedef traits::vertex_descriptor vertex_descriptor;
  typedef vertex_descriptor State;
  typedef vertex_descriptor ChoiceSet;
  typedef vertex_descriptor Distribution;

  typedef boost::graph_traits<Graph>::edge_descriptor edge_descriptor;
  typedef boost::graph_traits<Graph>::out_edge_iterator out_edge_iterator;
  typedef boost::graph_traits<Graph>::in_edge_iterator in_edge_iterator;
  typedef boost::graph_traits<Graph>::vertex_iterator vertex_iterator;

  typedef std::set<vertex_descriptor> StateSet;
  typedef std::set<vertex_descriptor> VertexSet;
  typedef std::pair<out_edge_iterator,out_edge_iterator> out_edge_iter_pair;
  typedef std::pair<in_edge_iterator,in_edge_iterator> in_edge_iter_pair;
  typedef std::pair<vertex_iterator,vertex_iterator> vertex_iter_pair;

enum StateKind {
	init,
	intermediate,
	goal,
        sink
};

  /* properties attached to states of the model */
  struct StateProp {
    /* getters and setters */
    inline StateKind getKind() const { return kind; }
    inline void setKind(StateKind k) { kind = k; }
    inline bool isBad() const        { return kind == goal; }
    inline void setGoal()            { kind = goal; min_prob = max_prob = 1.0; }
    inline bool isInit() const       { return kind == init; }
    inline void setInit()            { kind = init; }
    inline double getMinProb() const { return min_prob;}
    inline void setMinProb(double p) { min_prob = p;}
    inline double getMaxProb() const { return max_prob;}
    inline void setMaxProb(double p) { max_prob = p;}

    int state_nr;

    protected:
    StateKind     kind;
    double min_prob; // Min. probability of reaching a bad state
    double max_prob; // Min. probability of reaching a bad state

  };

  enum VertexType {S, P1, P2};

  struct P1Prop { };

  struct P2Prop {
    Action action;
  };

  struct VertexProp {
    VertexType vertexType;

    VertexProp(StateKind kind) : vertexType(S) {
	stateProp.setKind(kind);
	stateProp.setMinProb(2.0);
    	stateProp.setMaxProb(-1.0);
    }

    VertexProp(bool) : vertexType(P1) {}

    VertexProp(Action a) : vertexType(P2) {
	p2Prop.action = a;
    }


    /*
     * P1min_P_choice choice player i does if player 1 minimizes
     * P1max_P_choice choice player i does if player 1 maximized
     * i = 1 if vertexType = S, i = 2 if vertexType = P1
     *
     * Notice that it would be nicer to have this placed into the
     * different StateProp, P1Prop, P2Prop union parts. This is
     * actually not possible, because C++ forbids it.
     */
    edge_descriptor P1min_P_choice;
    edge_descriptor P1max_P_choice;

    union {
      StateProp stateProp;
      P1Prop p1prop;
      P2Prop p2Prop;
    };
    /* properties for SCC computation */
    int scc_index;
    State root;
    int time;
    boost::default_color_type color;
    int index;

  };

class StrongComponent {

public:
	/** \brief ID of the SCC assigned by Tarjan's algorithm */
	int scc_index;

	StrongComponent(int index) : scc_index (index) {}
	StrongComponent() {}

	State root;

	/** \brief elements of the strongly-connected component */
	StateSet constituents;

	/** \brief component constituents with an incoming edge from outside the component. */
	StateSet entry;

	/** \brief compomenent constituents with out-going edge to a state outside the component. */
	StateSet exit;

	/** \brief states outside the compoment and have an incoming edge from a component constituent. */
	StateSet target;
};

}






struct Transition {
	int command_nr;
	// The command with number "command_nr" has k probabilistic choices.
	// The vector "states" has k+1 entries.
	std::vector<int> states;

	bool operator==(const Transition& t) const {
		if(t.command_nr != command_nr || t.states.size()!=states.size()) return false;
		for(unsigned i=0;i<states.size();++i)
			if(states[i]!=t.states[i]) return false;
		return true;
	}
};

struct HyperTransition {
	std::vector<Transition> transitions;

	int check() const {
		int val = -1;
		for(std::vector<Transition>::const_iterator i=transitions.begin();i!=transitions.end();++i) {
			if(i==transitions.begin()) val = (*i).states[0];
			else assert( (*i).states[0] == val);
		}
		assert(val !=-1);
		return val;
	}
};

#endif
