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
