package epmc.graphsolver.iterative.natives;

import com.sun.jna.Memory;

import epmc.util.JNATools;

final class IterationNative {
    native static int double_dtmc_bounded(int bound, int numStates,
            int[] stateBounds, int[] targets, double[] weights,
            double[] values,
            Memory numIterations);

    native static int double_dtmc_bounded_cumulative(int bound, int numStates,
            int[] stateBounds, int[] targets, double[] weights,
            double[] values, double[] cumul,
            Memory numIterations);

    native static int double_dtmc_bounded_cumulative_discounted(int bound, double discount, int numStates,
            int[] stateBounds, int[] targets, double[] weights,
            double[] values, double[] cumul,
            Memory numIterations);

    native static int double_dtmc_unbounded_jacobi(int relative,
            double precision, int numStates, int[] stateBounds,
            int[] targets, double[] weights, double[] values,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_dtmc_unbounded_gaussseidel(int relative,
            double precision, int numStates, int[] stateBounds,
            int[] targets, double[] weights, double[] values,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_dtmc_unbounded_cumulative_jacobi(int relative,
            double precision, int numStates, int[] stateBounds,
            int[] targets, double[] weights, double[] values, double[] cumul,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_dtmc_unbounded_cumulative_jacobi_span(int relative,
            double precision, int numStates, int[] stateBounds,
            int[] targets, double[] weights, double[] values, double[] cumul,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_dtmc_unbounded_cumulative_gaussseidel(int relative,
            double precision, int numStates, int[] stateBounds,
            int[] targets, double[] weights, double[] values, double[] cumul,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_ctmc_bounded(double[] fg, int left, int right,
            int numStates, int[] stateBounds, int[] targets,
            double[] weights, double[] values,
            Memory numIterations);

    /*
    native static int double_ctmc_bounded_cumulative(double[] fg, int left, int right,
            int numStates, int[] stateBounds, int[] targets,
            double[] weights, double[] values, double[] cumul);
     */

    native static int double_mdp_unbounded_jacobi(int relative,
            double precision, int numStates,
            int[] stateBounds, int[] nondetBounds, int[] targets,
            double[] weights, int min, double[] values,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_mdp_unbounded_gaussseidel(int relative,
            double precision, int numStates,
            int[] stateBounds, int[] nondetBounds, int[] targets,
            double[] weights, int min, double[] values,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_mdp_unbounded_cumulative_jacobi(int relative,
            double precision, int numStates,
            int[] stateBounds, int[] nondetBounds, int[] targets,
            double[] weights, int min, double[] values, double[] cumul,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_mdp_unbounded_cumulative_jacobi_span(int relative,
            double precision, int numStates,
            int[] stateBounds, int[] nondetBounds, int[] targets,
            double[] weights, int min, double[] values, double[] cumul,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_mdp_unbounded_cumulative_gaussseidel(int relative,
            double precision, int numStates,
            int[] stateBounds, int[] nondetBounds, int[] targets,
            double[] weights, int min, double[] values, double[] cumul,
            int[] iterationsResult,
            Memory numIterations, Memory difference);

    native static int double_mdp_bounded(int bound, int numStates,
            int[] stateBounds, int[] nondetBounds, int[] targets,
            double[] weights, int min, double[] values,
            Memory numIterations);

    native static int double_mdp_bounded_cumulative(int bound, int numStates,
            int[] stateBounds, int[] nondetBounds, int[] targets,
            double[] weights, int min, double[] values, double[] cumul,
            Memory numIterations);

    native static int double_mdp_bounded_cumulative_discounted(int bound, double discount, int numStates,
            int[] stateBounds, int[] nondetBounds, int[] targets,
            double[] weights, int min, double[] values, double[] cumul,
            Memory numIterations);

    private final static boolean loaded =
            JNATools.registerLibrary(IterationNative.class, "valueiteration");

    final static int EPMC_ERROR_SUCCESS = 0;
    final static int EPMC_ERROR_OUT_OF_MEMORY = 1;
}
