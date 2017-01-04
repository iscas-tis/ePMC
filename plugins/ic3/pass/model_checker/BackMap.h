#ifndef BACK_MAP_H
#define BACK_MAP_H

#include "GraphDecl.h"

namespace model_checker {
  class GameGraph;

  /**
   * Class to map states and transitions from sparse matrix to game graph.
   */
  class BackMap {
    friend class GameGraph;
  public:
    void clear();
  private:
    std::vector<vertex_descriptor> state;
    std::vector<vertex_descriptor> choiceSet;
    std::vector<vertex_descriptor> distribution;
    std::vector<edge_descriptor> stateEdge;
    std::vector<edge_descriptor> choiceSetEdge;
    std::vector<edge_descriptor> distributionEdge;
  };
}
#endif
