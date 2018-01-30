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
#include <math.h>
#include "epmc_error.h"

__attribute__ ((visibility("default")))
epmc_error_t double_dtmc_unbounded_cumulative_gaussseidel(int relative,
        double precision, int numStates, int *stateBounds, int *targets,
        double *weights, double *values, double *cumul, int *iterationsResult,
        volatile int *numIterationsFeedback, volatile double *differenceFeedback) {
    double maxDiff;
    int iterations = 0;
    do {
        maxDiff = 0.0;
        for (int state = 0; state < numStates; state++) {
            int from = stateBounds[state];
            int to = stateBounds[state + 1];
            double nextStateProb = cumul[state];
            for (int succ = from; succ < to; succ++) {
                double weight = weights[succ];
                int succState = targets[succ];
                double succStateProb = values[succState];
                nextStateProb += weight * succStateProb;
            }
            double diff = fabs(nextStateProb - values[state]);
            if (relative && values[state] != 0.0) {
                diff /= values[state];
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
