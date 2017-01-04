#include <vector>
#include <boost/graph/graph_traits.hpp>
#include <boost/graph/adjacency_list.hpp>
#include <boost/graph/dijkstra_shortest_paths.hpp>
#include "util/Util.h"
#include "GameGraph.h"
#include "Paths.h"
#include "Path.h"

using namespace std;
using namespace boost;
using namespace model_checker;

namespace model_checker {
  Paths::Paths(GameGraph *__graph) : gameGraph(*__graph) {
    P1min = false;
  }

  bool Paths::hasMorePaths() {
    return true;
  }

  Path Paths::getNextPath() {
    Path result;
    if (0 == k) {
      
    } else {
    }

    vertex_descriptor bad_state = gameGraph.bad_state;
    vertex_descriptor init_state = gameGraph.init_state;

    PathNode &path = paths[bad_state][k];
    unsigned length = 0;
    PathNode &p = p;
    while (p.state != init_state) {
      p = *p.predecessor;      
      length++;
    }
    result.actions.resize(length);
    result.stochasticdecisions.resize(length);
    result.player1decisions.resize(length);
    result.player2decisions.resize(length);

    Graph &graph = gameGraph.graph;
    unsigned index = length - 1;
    p = path;
    // TODO actually do this function
#if 0
    while (p.state != init_state) {
      vertex_descriptor v = getStochasticDecisions(p.state);
      result.actions[index] = graph[v].p2Prop.action;
      StateProp &stateProp = graph[v].stateProp;
      result.player1decisions[index] = P1min ? stateProp.P1min_P1_choice
	: stateProp.P1max_P1_choice;
      index--;
      p = *p.predecessor;      
    }    
#endif

    k++;
    return result;
  }

  /**
   * Get vertex_descriptor for stochastic decisions of @a v.
   * This depends on the choices for player 1 and 2 made before.
   *
   * @param v state to get stochastic decisions for
   * @return player 2 vertex of stochastic decisions
   */
  vertex_descriptor Paths::getStochasticDecisions(State v) {
    //  TODO actually implement stuff
#if 0
    Graph &graph = gameGraph.graph;
    pair<out_edge_iterator, out_edge_iterator> it;
    unsigned P1choice = P1min ? graph[v].stateProp.P1min_P1_choice
      : graph[v].stateProp.P1max_P1_choice;
    unsigned p1_dec_nr = 0;
    for (it = out_edges(v, graph); it.first != it.second; ++it.first) {
      if (p1_dec_nr == P1choice) {
	edge_descriptor e = *it.first;
	vertex_descriptor u = target(e, graph);
	unsigned P2choice = P1min ? graph[u].p1prop.P1min_P2_choice
	  : graph[v].p1prop.P1max_P2_choice;
	pair<out_edge_iterator, out_edge_iterator> it2;	
	unsigned p2_dec_nr = 0;
	for (it2 = out_edges(u, graph);
	     it2.first != it2.second; ++it2.first) {
	  if (p2_dec_nr == P2choice) {
	    edge_descriptor e2 = *it2.first;	    
	    return target(e2, graph);
	  }
	  p2_dec_nr++;
	}	
      }
      p1_dec_nr++;
    }
#endif
  }

  /**
   * Gives probability to move from @a u to @a v in one step.
   * The probability will be returned in dependence of the scheduler
   * calculated before.
   *
   * @param u from state
   * @param v to state
   * @return probability to move from @a u to @a v in one step
   */
  double Paths::getTransitionProb(vertex_descriptor u, vertex_descriptor v) {
    Graph &graph = gameGraph.graph;

    vertex_descriptor w = getStochasticDecisions(u);
    pair<out_edge_iterator, out_edge_iterator> it;
    for (it = out_edges(w, graph); it.first != it.second; ++it.first) {
      edge_descriptor e = *it.first;
      State x = target(e, graph);
      if (x == v) {
	return graph[e];
      }
    }
  }

  /**
   * Gives probability to move from @a u to @a v in one step.
   * The probability will be returned in dependence of the scheduler
   * calculated before.
   *
   * @param u from state
   * @param v to state
   * @param trans_nr number of stochastic transition
   * @return probability to move from @a u to @a v in one step
   */
  double Paths::getTransitionProb(vertex_descriptor u, vertex_descriptor v,
				  unsigned &trans_nr) {
    Graph &graph = gameGraph.graph;

    vertex_descriptor w = getStochasticDecisions(u);
    pair<out_edge_iterator, out_edge_iterator> it;
    unsigned nr = 0;
    for (it = out_edges(w, graph); it.first != it.second; ++it.first) {
      edge_descriptor e = *it.first;
      State x = target(e, graph);
      if (x == v) {
	trans_nr = nr;
	return graph[e];
      }
      nr++;
    }
  }

  /**
   * Relax function from Bellman-Ford algorithm.
   */
  void Paths::bfRelax(vertex_descriptor u, vertex_descriptor v) {
    unsigned nr;
    double prob_u_v = getTransitionProb(u, v, nr);
    if (paths[v][0].prob <
	paths[u][0].prob * prob_u_v) {
      paths[v][0].prob = paths[u][0].prob * prob_u_v;
      paths[v][0].predecessor = &paths[u][0];
      paths[v][0].stochasticdecision = nr;
    }
  }

  void Paths::preparePathDS() {
    paths.clear();
    Graph &graph = gameGraph.graph;
    vertex_descriptor bad_state = gameGraph.bad_state;

    for (vertex_iter_pair vp = vertices(graph); vp.first != vp.second;
	 ++vp.first) {
      Graph::vertex_descriptor v =  *vp.first;
      if (S == graph[v].vertexType) {
	PathNode node;
	node.state = v;
	node.prob = (v == bad_state) ? 1.0 : 0.0;
	node.predecessor = NULL;
	node.stochasticdecision = UINT_MAX;
	vector<PathNode> vec;
	vec.push_back(node);
	paths[v] = vec;
      }
    }    
  }
  
  void Paths::bellmanFord() {
    Graph &graph = gameGraph.graph;

    for (vertex_iter_pair vp = vertices(graph); vp.first != vp.second;
	 ++vp.first) {
      Graph::vertex_descriptor v =  *vp.first;
      if (S == graph[v].vertexType) {
	vertex_descriptor u = getStochasticDecisions(v);
	pair<out_edge_iterator, out_edge_iterator> it;
	for (it = out_edges(u, graph); it.first != it.second; ++it.first) {
	  edge_descriptor e = *it.first;
	  vertex_descriptor w = target(e, graph);
	  bfRelax(v, w);
	}
      }
    }
  }

  /**
   * Initializes paths data structures if graph has changed.
   */
  void Paths::restart() {
    preparePathDS();
    bellmanFord();
    k = 0;
  }
}
