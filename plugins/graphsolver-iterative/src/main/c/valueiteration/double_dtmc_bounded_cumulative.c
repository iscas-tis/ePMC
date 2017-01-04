#include <stdlib.h>
#include "epmc_error.h"

__attribute__ ((visibility("default")))
epmc_error_t double_dtmc_bounded_cumulative(int bound, int numStates,
        int *stateBounds, int *targets, double *weights, double *values,
        double *cumul) {
    double *presValues = values;
    double *nextValues = malloc(sizeof(double) * numStates);
    if (nextValues == NULL) {
        return OUT_OF_MEMORY;
    }
    double *allocated = nextValues;
    for (int state = 0; state < numStates; state++) {
        presValues[state] = 0.0;
        nextValues[state] = 0.0;
    }
    double nextStateProb;
    for (int i = 0; i < bound; i++) {
        for (int state = 0; state < numStates; state++) {
            double value = values[state];
            int from = stateBounds[state];
            int to = stateBounds[state + 1];
            double nextStateProb = cumul[state];
            for (int succ = from; succ < to; succ++) {
                double weight = weights[succ];
                int succState = targets[succ];
                double succStateProb = presValues[succState];
                nextStateProb += weight * succStateProb;
            }
            nextValues[state] = nextStateProb;
        }
        double *swap = presValues;
        presValues = nextValues;
        nextValues = swap;
    }
    for (int state = 0; state < numStates; state++) {
        values[state] = presValues[state];
    }
    free(allocated);
    return SUCCESS;
}
