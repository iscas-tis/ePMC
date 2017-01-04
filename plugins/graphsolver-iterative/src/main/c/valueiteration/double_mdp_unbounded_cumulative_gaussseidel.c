#include <stdlib.h>
#include "epmc_error.h"
#include "epmc_util.h"

__attribute__ ((visibility("default")))
epmc_error_t double_mdp_unbounded_cumulative_gaussseidel(int relative,
        double precision, int numStates, int *stateBounds, int *nondetBounds,
        int *targets, double *weights, int min, double *values, double *cumul) {
    double maxDiff;
    double optInitValue = min ? INFINITY : -INFINITY;
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
    } while (maxDiff > precision / 2);
    return SUCCESS;
}
