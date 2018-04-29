package epmc.imdp.graphsolver;

import epmc.graph.CommonProperties;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.GraphExplicitSparseAlternate.EdgePropertySparseNondetOnlyNondet;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.options.Options;
import epmc.value.ValueContentDoubleArray;

final class UtilIMDPGraphSolver {

    static Diff getDiff() {
        IterationStopCriterion stopCriterion =
                Options.get().getEnum(OptionsGraphSolverIterative
                        .GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        switch (stopCriterion) {
        case ABSOLUTE:
            return (a,b) -> Math.abs(a - b);
        case RELATIVE:
            return (a,b) -> Math.abs(a - b) / a;
        default:
            break;
        }
        return null;
    }

    static void normalise(GraphExplicitSparseAlternate graph) {
        int[] stateBounds = graph.getStateBoundsJava();
        int numStates = graph.computeNumStates();
        int[] nondetBounds = graph.getNondetBoundsJava();
        EdgePropertySparseNondetOnlyNondet weightProp = (EdgePropertySparseNondetOnlyNondet) graph.getEdgeProperty(CommonProperties.WEIGHT);
        double[] weights = ValueContentDoubleArray.getContent(weightProp.getContent());
        for (int state = 0; state < numStates; state++) {
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                int nondetFrom = nondetBounds[nondet];
                int nondetTo = nondetBounds[nondet + 1];
                normalise(weights, nondetFrom, nondetTo);
            }
        }
    }

    private static void normalise(double[] intervals, int from, int to) {
        double lower = 0.0;
        double upper = 0.0;
        for (int index = from; index < to; index++) {
            lower += intervals[index * 2];
            upper += intervals[index * 2 + 1];
        }
        lower = 1 - lower;
        upper = 1 - upper;
        for (int index = from; index < to; index++) {
            double entryLower = lower + intervals[index * 2];
            double entryUpper = upper + intervals[index * 2 + 1];
            intervals[index * 2] = Math.max(intervals[index * 2], entryUpper);
            intervals[index * 2 + 1] = Math.min(intervals[index * 2 + 1], entryLower);
        }
    }

    private UtilIMDPGraphSolver() {
    }
}
