package epmc.constraintsolver.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Collections of possible problems in constraint solver plugin of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsConstraintsolver {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_CONSTRAINTSOLVER = "ProblemsConstraintsolver";

    /** Native memory allocation shall be performed but not enough memory is available. */
    public final static Problem CONSTRAINTSOLVER_INSUFFICIENT_NATIVE_MEMORY = newProblem("constraintsolver-insufficient-native-memory");

    /**
     * Generate new problem reading descriptions using correct resource bundle.
     * The parameter must not be {@code null}.
     * 
     * @param name identifier of problem
     * @return problem generated
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_CONSTRAINTSOLVER, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsConstraintsolver() {
    }
}
