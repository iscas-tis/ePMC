#include <cmath>
#include "util/Util.h"
#include "util/Database.h"
#include "GameSparse.h"

using namespace std;

namespace model_checker {
  GameSparse::GameSparse(unsigned __term_crit,
    			 double __term_crit_param,
    			 unsigned __max_iters) {
    term_crit = __term_crit;
    term_crit_param = __term_crit_param;
    max_iters = __max_iters;
    frexp(term_crit_param,&precision);
    precision *= 2;
  }

  /**
   * Resets the complete GameSparse object to its initial status.
   */
  void GameSparse::clear() {
    stateStart.clear();
    stateChoiceSet.clear();
    choiceSetStart.clear();
    choiceSetDistribution.clear();
    distributionStart.clear();
    distributionTarget.clear();
    distributionProb.clear();
    badStates.clear();
  }

  void GameSparse::setTermCritMethod(unsigned __term_crit) {
    term_crit = __term_crit;
  }
  
  void GameSparse::setTermCritParam(double __term_crit_param) {
    term_crit_param = __term_crit_param;
  }

  void GameSparse::setMaxIters(unsigned __max_iters) {
    max_iters = __max_iters;
  }

  void GameSparse::print() {
    cout << "stateStart:" << endl;
    for (unsigned i = 0; i < stateStart.size(); i++) {
      cout << "  " << stateStart[i] << endl;
    }
    cout << "stateChoiceSet:" << endl;
    for (unsigned i = 0; i < stateChoiceSet.size(); i++) {
      cout << "  " << stateChoiceSet[i] << endl;
    }
    cout << "choiceSetStart:" << endl;
    for (unsigned i = 0; i < choiceSetStart.size(); i++) {
      cout << "  " << choiceSetStart[i] << endl;
    }
    cout << "choiceSetDistribution:" << endl;
    for (unsigned i = 0; i < choiceSetDistribution.size(); i++) {
      cout << "  " << choiceSetDistribution[i] << endl;
    }
    cout << "distributionStart:" << endl;
    for (unsigned i = 0; i < distributionStart.size(); i++) {
      cout << "  " << distributionStart[i] << endl;
    }
    cout << "distributionTarget:" << endl;
    for (unsigned i = 0; i < distributionTarget.size(); i++) {
      cout << "  " << distributionTarget[i] << endl;
    }
    cout << "distributionProb:" << endl;
    for (unsigned i = 0; i < distributionProb.size(); i++) {
      cout << "  " << distributionProb[i] << endl;
    }
    cout << "badStates:" << endl;
    for (unsigned i = 0; i < badStates.size(); i++) {
      cout << badStates[i] << endl;
    }
  }

  bool GameSparse::approxEquality(double a, double b, int precision) {
    double diff(b-a);
    if (diff == 0.0) return true;
    int exp_diff;
    double mant_diff(frexp(diff,&exp_diff));
    int exp_b;
    double mant_b(frexp(b,&exp_b));
    return (exp_diff - exp_b ) < precision;
  }
  
  bool GameSparse::checkConvergence
  (vector<double> &soln, vector<double> &soln2) {
    bool done = true;
    unsigned numStates = soln.size();
    switch (term_crit) {
    case METHOD_EXPONENT:
    for (unsigned i = 0; i < numStates; ++i) {
	if (!approxEquality(soln[i], soln2[i], precision)) {
	  done = false;
	  break;
	}
      }
    break;

    case METHOD_ABSOLUTE:
      for (unsigned i = 0; i < numStates; ++i) {
	if (fabs(soln2[i] - soln[i]) > term_crit_param) {
	  done = false;
	  break;
	}
      }      
      break;

    case METHOD_RELATIVE:
      for (unsigned i = 0; i < numStates; ++i) {
	if (fabs(soln2[i] - soln[i])/soln2[i] > term_crit_param) {
	  done = false;
	  break;
	}
      }
      break;
    }    
    return done;
  }
}
