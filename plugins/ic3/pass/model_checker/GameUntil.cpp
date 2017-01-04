#include <math.h>
#include "util/Util.h"
#include "util/Database.h"
#include <iostream>
#include "GameSparse.h"

using namespace std;
using namespace model_checker;

/**
 * Calculates the unbounded until.
 */
void GameSparse::until(bool P1min, bool P2min, vector<double>& soln,
		std::vector<int> &chosenP1, std::vector<int> &chosenP2) {
	bool done = false;
	unsigned numStates = stateStart.size() - 1;

	/* set appropriate values for bad/non-bad states*/
	for (unsigned stateNr = 0; stateNr < numStates; stateNr++) {
		if (badStates[stateNr]) {
			soln[stateNr] = 1.0;
		}
	}

	/* set initial choice */
	//   for (unsigned stateNr = 0; stateNr < numStates; stateNr++) {
	//     chosenP1[stateNr] = stateStart[stateNr];
	//   }

	vector<double> soln2(numStates, 0.0);
	vector<double> distrVal(distributionStart.size() - 1);
	vector<double> p2Val(choiceSetStart.size() - 1, -100.0);

	unsigned iters;
	/* iterate till precision is reached or max number of iterations done */
	for (iters = 0; !done && iters < max_iters; ++iters) {
		/* calculate values of distributions */
		for (unsigned distrNr = 0; distrNr < distributionStart.size() - 1; distrNr++) {
			distrVal[distrNr] = 0.0;
			for (unsigned i = distributionStart[distrNr]; i < distributionStart[distrNr + 1]; i++) {
				distrVal[distrNr] += distributionProb[i] * soln[distributionTarget[i]];
			}
		}

		/* calculate optimal values for player 2, remember choice made */
		for (unsigned p2Choice = 0; p2Choice < choiceSetStart.size() - 1; p2Choice++) {
			double optVal = P2min ? 2.0 : -1.0;
			unsigned decision = -1;
			for (unsigned i = choiceSetStart[p2Choice]; i < choiceSetStart[p2Choice + 1]; ++i) {
				double val = distrVal[choiceSetDistribution[i]];
				if (P2min ? (val < optVal) : (val > optVal)) {
					optVal = val;
					decision = i;
				}
			}
			//if (!approxEquality(p2Val[p2Choice], optVal, precision)) {
				chosenP2[p2Choice] = decision;
			//}

			p2Val[p2Choice] = optVal;
		}

		/* calculate optimal value for player 1 */
		for (unsigned s = 0; s < numStates; ++s) {
			double optVal = badStates[s] ? 1.0 : (P1min ? 2.0 : -1.0);
			int choiceP1 = -1;
			for (unsigned i = stateStart[s]; i < stateStart[s + 1]; ++i) {
				double val = p2Val[stateChoiceSet[i]];
				/* if either no choice was done or we get a value that is really
				 * better, take this choice
				 */
				if (P1min ? (val < optVal) : (val > optVal)) {
					optVal = val;
					choiceP1 = i;
				}
			}

			/*
			 Modify the strategy
			 */
			if (chosenP1[s] == -1 || !approxEquality(p2Val[stateChoiceSet[chosenP1[s]]], optVal, precision)) // value iteration destroyed optimality
			{
				chosenP1[s] = choiceP1;
			}

			if ((2.0 == optVal) || (-1.0 == optVal)) {
				optVal = 0.0;
			}
			soln2[s] = optVal;
		}

		done = checkConvergence(soln, soln2);
		soln.swap(soln2);
	}

	MSG(0,"iterations: %d \n",iters);
}

