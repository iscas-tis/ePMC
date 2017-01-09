#include <stdlib.h>
#include <math.h>
#include "epmc_error.h"

__attribute__ ((visibility("default")))
epmc_error_t double_tpg_unbounded_jacobi(int relative, double precision,
        int maxEnd, int minEnd, int *stateBounds, int *nondetBounds,
        int *targets, double *weights, double *values) {
    double *presValues = values;
    double *nextValues = malloc(sizeof(double) * minEnd);
    if (nextValues == NULL) {
        return OUT_OF_MEMORY;
    }
    double *allocated = nextValues;
    double maxDiff;
    do {
        maxDiff = 0.0;
        for (int state = 0; state < maxEnd; state++) {
            double presStateProb = presValues[state];
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            double nextStateProb = -INFINITY;
            for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                int nondetFrom = nondetBounds[nondetNr];
                int nondetTo = nondetBounds[nondetNr + 1];
                double choiceNextStateProb = 0.0;
                for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    double weight = weights[stateSucc];
                    int succState = targets[stateSucc];
                    double succStateProb = presValues[succState];
                    double weighted = weight * succStateProb;
                    choiceNextStateProb += weighted;
                }
                nextStateProb = fmax(nextStateProb, choiceNextStateProb);
            }
            double diff = fabs(nextStateProb - presValues[state]);
            if (relative && presValues[state] != 0.0) {
                diff /= presValues[state];
            }
            maxDiff = diff > maxDiff ? diff : maxDiff;
            nextValues[state] = nextStateProb;
        }
        for (int state = maxEnd; state < minEnd; state++) {
            double presStateProb = presValues[state];
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            double nextStateProb = INFINITY;
            for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                int nondetFrom = nondetBounds[nondetNr];
                int nondetTo = nondetBounds[nondetNr + 1];
                double choiceNextStateProb = 0.0;
                for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    double weight = weights[stateSucc];
                    int succState = targets[stateSucc];
                    double succStateProb = presValues[succState];
                    double weighted = weight * succStateProb;
                    choiceNextStateProb += weighted;
                }
                nextStateProb = fmin(nextStateProb, choiceNextStateProb);
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
    for (int state = 0; state < minEnd; state++) {
        values[state] = presValues[state];
    }
    free(allocated);
    return SUCCESS;
}
