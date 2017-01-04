#ifndef AI_SEE_PRINTER_H
#define AI_SEE_PRINTER_H

#include <iostream>
#include <fstream>

namespace model_checker {

  class GameGraph;

  class AiSeePrinter {
    friend class GameGraph;
    const GameGraph &graph;

  public:
    AiSeePrinter(const GameGraph &, std::ostream &);
    void printRegions(std::ostream &stream);
  };
}

#endif
