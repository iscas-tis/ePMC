/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

#include <stdlib.h>
#include "epmc_error.h"
#include "epmc_util.h"

__attribute__ ((visibility("default")))
epmc_error_t double_mdp_unbounded_cumulative_gaussseidel(int relative,
        double precision, int numStates, int *stateBounds, int *nondetBounds,
        int *targets, double *weights, int min, double *values, double *cumul,
        int *iterationsResult,
        volatile int *numIterationsFeedback, volatile double *differenceFeedback) {
    double maxDiff;
    double optInitValue = min ? INFINITY : -INFINITY;
    int iterations = 0;
    do {
        maxDiff = 0.0;
        for (int state = 0; state < numStates; state++) {
            double presStateProb = values[state];
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            double nextStateProb = optInitValue;
            for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                int nondetFrom = nondetBounds[nondetNr];
                int nondetTo = nondetBounds[nondetNr + 1];
                double choiceNextStateProb = cumul[nondetNr];
                for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    double weight = weights[stateSucc];
                    int succState = targets[stateSucc];
                    double succStateProb = values[succState];
                    double weighted = weight * succStateProb;
                    choiceNextStateProb +=  weighted;
                }
                nextStateProb = fopt(min, nextStateProb, choiceNextStateProb);
            }
            double diff = fabs(nextStateProb - values[state]);
            if (relative && presStateProb != 0.0) {
                diff /= presStateProb;
            }
            maxDiff = diff > maxDiff ? diff : maxDiff;
            values[state] = nextStateProb;
        }
        *numIterationsFeedback = iterations;
        *differenceFeedback = maxDiff;
        iterations++;
    } while (maxDiff > precision / 2);
    iterationsResult[0] = iterations;
    return SUCCESS;
}
