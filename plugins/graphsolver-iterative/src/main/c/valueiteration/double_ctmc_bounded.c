#include <stdlib.h>
#include "epmc_error.h"

__attribute__ ((visibility("default")))
epmc_error_t double_ctmc_bounded(double *fg, int left, int right,
        int numStates, int *stateBounds, int *targets, double *weights,
        double *values) {
    double *presValues = malloc(sizeof(double) * numStates);
    if (presValues == NULL) {
        return OUT_OF_MEMORY;
    }
    double *nextValues = malloc(sizeof(double) * numStates);
    if (nextValues == NULL) {
        free(presValues);
        return OUT_OF_MEMORY;
    }
    for (int state = 0; state < numStates; state++) {
        presValues[state] = 0.0;
        nextValues[state] = 0.0;
    }
    for (int i = right - left; i >= 0; i--) {
        double fgWeight = fg[i];
        for (int state = 0; state < numStates; state++) {
            double value = values[state];
            int from = stateBounds[state];
            int to = stateBounds[state + 1];
            double nextStateProb = fgWeight * value;
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
    for (int i = left - 1; i >= 0; i--) {
        for (int state = 0; state < numStates; state++) {
            int from = stateBounds[state];
            int to = stateBounds[state + 1];
            double nextStateProb = 0.0;
            for (int succ = from; succ < to; succ++) {
                double weight = weights[succ];
                int succState = targets[succ];
                double succStateProb = presValues[succState];
                nextStateProb += succStateProb * weight;
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
    free(presValues);
    free(nextValues);
    return SUCCESS;
}
