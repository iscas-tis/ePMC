#include <stdlib.h>
#include <math.h>
#include "epmc_error.h"

__attribute__ ((visibility("default")))
epmc_error_t double_dtmc_unbounded_gaussseidel(int relative,
        double precision, int numStates, int *stateBounds, int *targets,
        double *weights, double *values) {
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
    } while (maxDiff > precision);
    return SUCCESS;
}
