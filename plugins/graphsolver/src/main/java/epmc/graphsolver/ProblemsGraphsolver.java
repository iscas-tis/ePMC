package epmc.graphsolver;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsGraphsolver {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_GRAPHSOLVER = "ProblemsGraphsolver";
    /** No property solver for the given property is available. */
    public final static Problem GRAPHSOLVER_NO_SOLVER_AVAILABLE = newProblem("graphsolver-no-solver-available");

    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_GRAPHSOLVER, name);
    }

	private ProblemsGraphsolver() {
	}
}
