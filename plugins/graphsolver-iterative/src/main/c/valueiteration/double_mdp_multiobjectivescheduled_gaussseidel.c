#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "epmc_error.h"
#include "epmc_util.h"

__attribute__ ((visibility("default")))
epmc_error_t double_mdp_multiobjectivescheduled_gaussseidel(int relative, double precision,
        int numStates, int *stateBounds, int *nondetBounds, int *targets,
        double *weights, double *stopRewards, double *transRewards,
        double *values, int *scheduler) {
    double optInitValue = -INFINITY;
    for (int state = 0; state < numStates; state++) {
        values[state] = 0.0;
    }
    double maxDiff;
    do {
        maxDiff = 0.0;
        for (int state = 0; state < numStates; state++) {
            double presStateProb = values[state];
            int nondetNr = scheduler[state];
            double nextStateProb = 0.0;
            if (nondetNr == -1) {
                nextStateProb = stopRewards[state];
            } else {
                nextStateProb = transRewards[nondetNr];
                int nondetFrom = nondetBounds[nondetNr];
                int nondetTo = nondetBounds[nondetNr + 1];
                for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    double weight = weights[stateSucc];
                    int succState = targets[stateSucc];
                    double succStateProb = values[succState];
                    double weighted = weight * succStateProb;
                    nextStateProb += weighted;
                }
            }
            double diff = fabs(nextStateProb - values[state]);
            if (relative && values[state] != 0.0) {
                diff /= values[state];
            }
            maxDiff = diff > maxDiff ? diff : maxDiff;
            values[state] = nextStateProb;
        }
    } while (maxDiff > precision / 2);
    return SUCCESS;
}
