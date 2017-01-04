#include "util/Util.h"
#include "util/Database.h"
#include "util/Error.h"
#include "util/Timer.h"
#include "util/Statistics.h"
#include "util/Cube.h"
#include <fstream>
#include "lang/Node.h"
#include "lang/ExprManager.h"
#include "lang/SymbolTable.h"
#include "lang/Property.h"
#include "pred/Predicate.h"
#include "pred/PredSet.h"
#include "lang/Model.h"
#include "bdd/BDD.h"
#include "bdd/ODD.h"
#include "dp/SMT.h"

#include "GameGraph.h"
#include "pred/EncodingManager.h"
#include "pred/AbsModel.h"
#include "pred/AbsModelImpl.h"

using namespace model_checker;
using namespace std;
using namespace util;

std::string model_name;
lang::Model model;
pred::AbsModel* abs_model;

int main(int argc, char *argv[]) {
  cout << "Test for GameGraph class and friends." << endl << endl;

  cout << "Generating graph from paper [?]" << endl;
  GameGraph graph;
  State s0 = graph.addState();
  graph.addInit(s0);
  State s1 = graph.addState();
  State s2 = graph.addState();
  State s3 = graph.addState();
  graph.addBad(s2);
  ChoiceSet c1;
  Distribution c2;
  c1 = graph.createChoiceSet();
  graph.addChoiceSet(s0, c1);
  c2 = graph.createDistribution(1);
  graph.addDistribution(c1, c2);
  graph.addProbChoice(c2, s1, 1.0);
  c1 = graph.createChoiceSet();
  graph.addChoiceSet(s1, c1);
  c2 = graph.createDistribution(2);
  graph.addDistribution(c1, c2);
  graph.addProbChoice(c2, s3, 0.3);
  graph.addProbChoice(c2, s2, 0.7);
  c2 = graph.createDistribution(4);
  graph.addDistribution(c1, c2);
  graph.addProbChoice(c2, s2, 1.0);
  c1 = graph.createChoiceSet();
  graph.addChoiceSet(s1, c1);
  c2 = graph.createDistribution(2);
  graph.addDistribution(c1, c2);
  graph.addProbChoice(c2, s2, 0.8);
  graph.addProbChoice(c2, s3, 0.1);
  graph.addProbChoice(c2, s0, 0.1);
  graph.printDOT();
  cout << "DONE" << endl;
  cout << "Unbounded Until with minimizing player 2" << endl;
  graph.until(true);
  cout << "Result:  [" << graph.getMinResult() << ", " << graph.getMaxResult() << "]" << endl;
  cout << "Result decisions:" << endl;
  graph.printDOT();
  graph.printInducedMarkovChain(true);
  graph.printInducedMarkovChain(false);
  cout << "Unbounded Until with maximizing player 2" << endl;
  graph.until(false);
  cout << "Result:  [" << graph.getMinResult() << ", " << graph.getMaxResult() << "]" << endl;
  cout << "Result decisions:" << endl;
  graph.printDOT();
  graph.printInducedMarkovChain(true);
  graph.printInducedMarkovChain(false);

  cout << "Another example graph:" << endl;
  GameGraph graph2;
  s0 = graph2.addState();
  s1 = graph2.addState();
  s2 = graph2.addState();
  graph2.addInit(s0);
  graph2.addBad(s1);
  c1 = graph2.createChoiceSet();
  graph2.addChoiceSet(s0, c1);
  c2 = graph2.createDistribution(1);
  graph2.addDistribution(c1, c2);
  graph2.addProbChoice(c2, s1, 0.1);
  graph2.addProbChoice(c2, s2, 0.9);
  c2 = graph2.createDistribution(2);
  graph2.addDistribution(c1, c2);
  graph2.addProbChoice(c2, s1, 0.2);
  graph2.addProbChoice(c2, s2, 0.8);
  c1 = graph2.createChoiceSet();
  graph2.addChoiceSet(s0, c1);
  c2 = graph2.createDistribution(3);
  graph2.addDistribution(c1, c2);
  graph2.addProbChoice(c2, s1, 0.3);
  graph2.addProbChoice(c2, s2, 0.7);
  c2 = graph2.createDistribution(2);
  graph2.addDistribution(c1, c2);
  graph2.addProbChoice(c2, s1, 0.4);
  graph2.addProbChoice(c2, s2, 0.6);
  graph2.printDOT();

  cout << "Unbounded Until with minimizing player 2" << endl;
  graph2.until(true);
  cout << "Result:  [" << graph2.getMinResult() << ", " << graph2.getMaxResult() << "]" << endl;
  cout << "Result decisions:" << endl;
  graph2.printDOT();
  cout << "Unbounded Until with maximizing player 2" << endl;
  graph2.until(false);
  cout << "Result:  [" << graph2.getMinResult() << ", " << graph2.getMaxResult() << "]" << endl;
  cout << "Result decisions:" << endl;
  graph2.printDOT();
  graph2.printInducedMarkovChain(true);
  graph2.printInducedMarkovChain(false);

  cout << "Now a GameGraph with multiple transitions into one state" << endl;
  GameGraph graph3;
  s1 = graph3.addState();
  s2 = graph3.addState();
  s3 = graph3.addState();
  graph3.addInit(s1);
  graph3.addBad(s3);
  c1 = graph3.createChoiceSet();
  graph3.addChoiceSet(s1, c1);
  c2 = graph3.createDistribution(0);
  graph3.addDistribution(c1, c2);
  graph3.addProbChoice(c2, s2, 0.1);
  graph3.addProbChoice(c2, s2, 0.1);
  graph3.addProbChoice(c2, s3, 0.8);
  graph3.until(false);
  graph3.printDOT();

  exit(0);
}
