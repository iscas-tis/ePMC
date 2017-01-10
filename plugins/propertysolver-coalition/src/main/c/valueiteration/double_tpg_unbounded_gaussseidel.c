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
epmc_error_t double_tpg_unbounded_gaussseidel(int relative, double precision,
        int maxEnd, int minEnd, int *stateBounds, int *nondetBounds,
        int *targets, double *weights, double *values) {
    double maxDiff;
    do {
        maxDiff = 0.0;
        for (int state = 0; state < maxEnd; state++) {
            double presStateProb = values[state];
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            double nextStateProb = -INFINITY;
            for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                int nondetFrom = nondetBounds[nondetNr];
                int nondetTo = nondetBounds[nondetNr + 1];
                double choiceNextStateProb = 0.0;;
                for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    double weight = weights[stateSucc];
                    int succState = targets[stateSucc];
                    double succStateProb = values[succState];
                    double weighted = weight * succStateProb;
                    choiceNextStateProb +=  weighted;
                }
                nextStateProb = fmax(nextStateProb, choiceNextStateProb);
            }
            double diff = fabs(nextStateProb - values[state]);
            if (relative && presStateProb != 0.0) {
                diff /= presStateProb;
            }
            maxDiff = diff > maxDiff ? diff : maxDiff;
            values[state] = nextStateProb;
        }
        for (int state = maxEnd; state < minEnd; state++) {
            double presStateProb = values[state];
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            double nextStateProb = INFINITY;
            for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                int nondetFrom = nondetBounds[nondetNr];
                int nondetTo = nondetBounds[nondetNr + 1];
                double choiceNextStateProb = 0.0;;
                for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    double weight = weights[stateSucc];
                    int succState = targets[stateSucc];
                    double succStateProb = values[succState];
                    double weighted = weight * succStateProb;
                    choiceNextStateProb += weighted;
                }
                nextStateProb = fmin(nextStateProb, choiceNextStateProb);
            }
            double diff = fabs(nextStateProb - values[state]);
            if (relative && presStateProb != 0.0) {
                diff /= presStateProb;
            }
            maxDiff = diff > maxDiff ? diff : maxDiff;
            values[state] = nextStateProb;
        }
    } while (maxDiff > precision / 2);
    return SUCCESS;
}
