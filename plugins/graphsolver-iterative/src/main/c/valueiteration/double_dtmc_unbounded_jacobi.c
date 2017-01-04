#include <stdlib.h>
#include <math.h>
#include "epmc_error.h"

__attribute__ ((visibility("default")))
epmc_error_t double_dtmc_unbounded_jacobi(int relative, double precision,
        int numStates, int *stateBounds, int *targets, double *weights,
        double *values) {
    double *presValues = values;
    double *nextValues = malloc(sizeof(double) * numStates);
    if (nextValues == NULL) {
        return OUT_OF_MEMORY;
    }
    double *allocated = nextValues;
    for (int state = 0; state < numStates; state++) {
        nextValues[state] = 0.0;
    }
    double maxDiff;
    do {
        maxDiff = 0.0;
        for (int state = 0; state < numStates; state++) {
            int from = stateBounds[state];
            int to = stateBounds[state + 1];
            double nextStateProb = 0.0;
            for (int succ = from; succ < to; succ++) {
                double weight = weights[succ];
                int succState = targets[succ];
                double succStateProb = presValues[succState];
                nextStateProb += weight * succStateProb;
            }
            double diff = fabs(nextStateProb - presValues[state]);
            if (relative && presValues[state] != 0.0) {
                diff /= presValues[state];
            }
            maxDiff = diff > maxDiff ? diff : maxDiff;
            nextValues[state] = nextStateProb;
        }
        double *swap = presValues;
        presValues = nextValues;
        nextValues = swap;
    } while (maxDiff > precision / 2);
    for (int state = 0; state < numStates; state++) {
        values[state] = presValues[state];
    }
    free(allocated);
    return SUCCESS;
}
