#include <stdlib.h>
#include <stdio.h>
#include <math.h>
#include "epmc_error.h"
#include "epmc_util.h"

__attribute__ ((visibility("default")))
epmc_error_t double_mdp_multiobjectiveweighted_jacobi(int relative, double precision,
        int numStates, int *stateBounds, int *nondetBounds, int *targets,
        double *weights, double *stopRewards, double *transRewards, double *values, int *scheduler) {
    double optInitValue = -INFINITY;
    double *presValues = values;
    for (int state = 0; state < numStates; state++) {
        presValues[state] = 0.0;
    }
    double *nextValues = malloc(sizeof(double) * numStates);
    if (nextValues == NULL) {
        return OUT_OF_MEMORY;
    }
    double *allocated = nextValues;
    double maxDiff;
    for (int state = 0; state < numStates; state++) {
        scheduler[state] = -1;
    }
    do {
        maxDiff = 0.0;
        for (int state = 0; state < numStates; state++) {
            double presStateProb = presValues[state];
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            double nextStateProb = optInitValue;
            for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                int nondetFrom = nondetBounds[nondetNr];
                int nondetTo = nondetBounds[nondetNr + 1];
                double choiceNextStateProb = transRewards[nondetNr];
                for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    double weight = weights[stateSucc];
                    int succState = targets[stateSucc];
                    double succStateProb = presValues[succState];
                    double weighted = weight * succStateProb;
                    choiceNextStateProb += weighted;
                }
                if (choiceNextStateProb > nextStateProb) {
                    nextStateProb = choiceNextStateProb;
                    if (nextStateProb > presStateProb) {
                        scheduler[state] = nondetNr;
                    }
                }
            }
            if (stopRewards[state] > nextStateProb) {
                nextStateProb = stopRewards[state];
                if (nextStateProb > presStateProb) {
                    scheduler[state] = -1;
                }
            }
            double diff = fabs(nextStateProb - presValues[state]);
            if (relative && presValues[state] != 0.0) {
                diff /= presValues[state];
            }
            maxDiff = diff > maxDiff ? diff : maxDiff;
            nextValues[state] = nextStateProb;
        }
        double *swap = nextValues;
        nextValues = presValues;
        presValues = swap;
    } while (maxDiff > precision / 2);
    for (int state = 0; state < numStates; state++) {
        values[state] = presValues[state];
    }
    free(allocated);
    return SUCCESS;
}
