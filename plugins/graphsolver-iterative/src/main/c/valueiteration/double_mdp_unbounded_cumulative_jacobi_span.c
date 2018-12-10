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
epmc_error_t double_mdp_unbounded_cumulative_jacobi_span(int relative,
        double precision, int numStates, int *stateBounds, int *nondetBounds,
        int *targets, double *weights, int min, double *values, double *cumul,
        int *iterationsResult,
        volatile int *numIterationsFeedback, volatile double *differenceFeedback) {
    double optInitValue = min ? INFINITY : -INFINITY;
    double *presValues = values;
    double *nextValues = malloc(sizeof(double) * numStates);
    if (nextValues == NULL) {
        return OUT_OF_MEMORY;
    }
    double *allocated = nextValues;
    for (int state = 0; state < numStates; state++) {
        nextValues[state] = 0.0;
    }
    double span;
    int iterations = 0;
    do {
        double lower = INFINITY;
        double upper = -INFINITY;
        for (int state = 0; state < numStates; state++) {
            double presStateProb = presValues[state];
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
                    double succStateProb = presValues[succState];
                    double weighted = weight * succStateProb;
                    choiceNextStateProb += weighted;
                }
                nextStateProb = fopt(min, nextStateProb, choiceNextStateProb);
            }
            double diff = fabs(nextStateProb - presValues[state]);
            if (relative && presValues[state] != 0.0) {
                diff /= presValues[state];
            }
            lower = fmin(lower, diff);
            upper = fmax(upper, diff);
            nextValues[state] = nextStateProb;
        }
        double *swap = nextValues;
        nextValues = presValues;
        presValues = swap;
        *numIterationsFeedback = iterations;
        *differenceFeedback = span;
        iterations++;
    } while (span > precision / 2);
    for (int state = 0; state < numStates; state++) {
        values[state] = presValues[state];
    }
    free(allocated);
    iterationsResult[0] = iterations;
    return SUCCESS;
}
