#ifndef CFGDEF_H_
#define CFGDEF_H_

#include <boost/graph/graph_traits.hpp>
#include <boost/graph/adjacency_list.hpp>

namespace pred {

class CFGNodeProp;

typedef boost::adjacency_list<boost::vecS, boost::vecS,boost::bidirectionalS, CFGNodeProp, unsigned> Graph;

/** convenience typedefs for graph traversal */
typedef boost::graph_traits<Graph> traits;
typedef traits::vertex_descriptor vertex_descriptor;
typedef traits::out_edge_iterator out_edge_iterator;
typedef traits::in_edge_iterator in_edge_iterator;
typedef traits::vertex_iterator vertex_iterator;
typedef traits::adjacency_iterator adjacency_iterator;

typedef vertex_descriptor Location;
typedef vertex_descriptor Action;
typedef vertex_descriptor Distribution;

typedef std::pair<out_edge_iterator,out_edge_iterator> out_edge_iter_pair;
typedef std::pair<in_edge_iterator,in_edge_iterator> in_edge_iter_pair;
typedef std::pair<vertex_iterator,vertex_iterator> vertex_iter_pair;
typedef std::pair<adjacency_iterator,adjacency_iterator> adjacency_iter_pair;
typedef traits::edge_descriptor edge_descriptor;


/* value iteration */
typedef std::vector<double> Valuation;
typedef std::vector<int> Strategy;

/** \brief valuations and scheduler decisions
    \note Valuations and decisions both are functions from blocks to floating point numbers/indices
          The vector is indexed by the unique index of the respective block (given by the ODD at the CFGLocation)
  */
struct ValIterResult {
	/** \brief function from blocks to valuations */
	Valuation val;
	/** \brief function from blocks to scheduler decisions
	  * \note  the meaning of the index depends on the kind of CFG node
	  *        - CFGLocation : index refers to a CFGAction
	  *        - CFGAction   : index refers to an abstract distribution
	  *        - CFGDistr    : no meaning
	  */
	Strategy  str;
};

/** \brief contents of a CFG node */
class CFGNodeProp {
public:


	enum Kind {
		Default,
		Location,
		Action,
		Distr
	} kind;



	CFGNodeProp() { kind = Default; id = 0; }
	CFGNodeProp(Kind k, unsigned i) : kind (k), id (i) { }
	CFGNodeProp& operator=(const CFGNodeProp& p) {
		kind = p.kind;
		id = p.id;
		lb.str = p.lb.str;
		ub.str = p.lb.str;
		lb.val = p.lb.val;
		ub.val = p.ub.val;

		return *this; }
	CFGNodeProp(const CFGNodeProp& p) { *this = p; }

	bool isDefault () const { return kind == Default; }
	bool isLocation() const { return kind == Location; }
	bool isAction  () const { return kind == Action; }
	bool isDistr   () const { return kind == Distr;  }

	ValIterResult lb, ub;
	ValIterResult cache;

	inline void resizeValIter(ValIterResult& v, unsigned n, double default_val = 0.0) {
		v.val.clear();
		v.val.resize(n,default_val);
		v.str.clear();
		v.str.resize(n,-1);
	}

	inline void resizeValIterResult(unsigned n, double default_val = 0.0) {
		resizeValIter(lb,n,default_val);
		resizeValIter(ub,n,default_val);
		resizeValIter(cache,n,default_val);
	}

	unsigned id;

	int bfs_time;

	/* SCC */
    int scc_index;
    vertex_descriptor root;
    int time;
    boost::default_color_type color;
};




struct DFSOrder {
	bool operator()(vertex_descriptor x, vertex_descriptor y) const {
		return graph[x].time < graph[y].time;
	}

	DFSOrder(const Graph& g) : graph(g) {}

	const Graph& graph;

};

struct DFSReverseOrder {
	bool operator()(vertex_descriptor x, vertex_descriptor y) const {
		return graph[x].time > graph[y].time;
	}

	DFSReverseOrder(const Graph& g) : graph(g) {}

	const Graph& graph;

};




class Blocks {
public:
	/** local set of predicates */
	PredSet preds;

	/** encoding symbolic <=> explicit-state */
	bdd::ODD2 odd;

	/** set of reachable blocks */
	bdd::BDD reach;
	bdd::BDD init;
	std::vector<bool> init_vec;

	/** set of reachable blocks */
	bdd::BDD reach_prime;


	/** Boolean variables corresponding to predicates */
	std::vector<int> variables;
};

class CFGLocation {
public:
	/** characteristic function */
	std::vector<CVC3::Expr> e;
	Blocks b;

	bool getDiff(unsigned block_nr1, unsigned block_nr2, unsigned& position) const;
	bool getDiff(const CFGLocation& loc, unsigned& position) const;
};

struct LocationHasher {
	size_t operator()(const Location& loc) const {
		MSG(0,"LocationHasher %d\n",(size_t) loc)
		return (size_t)loc;
	}
};

struct Block {
	Location loc;
	unsigned block_nr;

	bool operator==(const Block& b) const { return loc == b.loc && block_nr == b.block_nr; }
	bool operator!=(const Block& b) const { return loc != b.loc || block_nr != b.block_nr; }
	Block(const Block& b) { *this = b; }
	Block& operator=(const Block& b) { loc = b.loc; block_nr = b.block_nr; return *this; }
	Block() {}
	Block(Location l, unsigned b) : loc(l), block_nr(b) {}
};


struct Transition {

	Transition() {}

	Transition(const Transition& t) { *this = t; }

	Transition(Block __block, Action __action, Distribution __distr, unsigned __prob_choice) 
		: block(__block), action(__action), distr(__distr), prob_choice(__prob_choice), terminal(false) {}

	Transition(Block __block) : block(__block), terminal(true) {}

	bool operator== (const Transition& t) const {
		return terminal == t.terminal 
		    && block == t.block 
		    && (terminal ? true : action == t.action && distr == t.distr && prob_choice == t.prob_choice);
	}


	Transition& operator=(const Transition& t) {
		block       = t.block;
		action      = t.action;
		distr       = t.distr;
		prob_choice = t.prob_choice;
		terminal    = t.terminal;
		return *this;
	}

	Block getBlock() const { return block; }
	Action getAction() const { assert(!terminal); return action; }
	Distribution getDistribution() const { assert(!terminal); return distr; }
	unsigned getChoice() const { assert(!terminal); return prob_choice; }
	bool isTerminal() const { return terminal; }
private:
	Block block;
	Action action;         // => Command is known
	Distribution distr;    
	unsigned prob_choice;  // => probabilistic alternative is known

	bool terminal;

};


typedef std::vector<Transition> CFGPath;





struct Splitter {
	Block b;
	Distribution d1, d2;
	std::vector<Block> v1,v2;
};





}

namespace std {
	namespace tr1 {
	template<> struct hash< pred::Block >
		{
			size_t operator()( const pred::Block& b ) const
			{
				size_t result = 0;
				result = b.loc + (result << 6) + (result << 16) - result;
				result = b.block_nr + (result << 6) + (result << 16) - result;
				return result;
			}
		};
	}
}


#endif
