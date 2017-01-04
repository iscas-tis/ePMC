#ifndef MODEL_CHECKER_PATHS_H
#define MODEL_CHECKER_PATHS_H

#include <vector>
#include <boost/graph/graph_traits.hpp>
#include <boost/graph/adjacency_list.hpp>
#include <boost/graph/dijkstra_shortest_paths.hpp>
#include "Path.h"

/*
 * To really understand what's going on here, you should probably read:
 *
 * @inproceedings{720768,
 * author = {V\'{\i}ctor M. Jim\'{e}nez and Andr\'{e}s Marzal},
 * title = {Computing the K Shortest Paths: A New Algorithm and an Experimental Comparison},
 * booktitle = {WAE '99: Proceedings of the 3rd International Workshop on Algorithm Engineering},
 * year = {1999},
 * isbn = {3-540-66427-0},
 * pages = {15--29},
 * publisher = {Springer-Verlag},
 * address = {London, UK},
 * }
 */

namespace model_checker {
  /* we can't include GameGraph.h, because GameGraph.h includes
   * Paths.h, so we have to defined stuff needed here manually */
  class GameGraph;

  /* path node for candidate set */
  class PathNode {
    friend class Paths;
    /* state describing path */
    void *state;
    /* probability of path */
    double prob;
    /* predecessor state of last state of path */
    PathNode *predecessor;
    /* stochastic decision done in predecessor to reach this state */    
    unsigned stochasticdecision;
    /* player 1 decision done in predecessor to reach this state */        
    // get from graph!
    /* player 2 decision done in predecessor to reach this state */    
    // get from graph
  };

  class Paths {
    friend class GameGraph;
  private:
    Paths(GameGraph *);
    bool hasMorePaths();
    Path getNextPath();
    void preparePathDS();
    void bellmanFord();
    void restart();
    void* getStochasticDecisions(void*);
    void bfRelax(void*, void*);
    double getTransitionProb(void*, void*);
    double getTransitionProb(void*, void*, unsigned &);
    GameGraph &gameGraph;
    unsigned k;
    std::map<void*, std::vector<PathNode> > paths;
    bool P1min;
  };
}
#endif
