#include "BackMap.h"

namespace model_checker {
  void BackMap::clear() {
    state.clear();
    choiceSet.clear();
    distribution.clear();
    stateEdge.clear();
    choiceSetEdge.clear();
    distributionEdge.clear();
  }
}
