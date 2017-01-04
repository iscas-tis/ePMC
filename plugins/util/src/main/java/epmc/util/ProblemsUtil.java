package epmc.util;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsUtil {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_UTIL = "ProblemsUtil";
    /** Native memory allocation shall be performed but not enough memory is available. */
    public final static Problem INSUFFICIENT_NATIVE_MEMORY = newProblem("insufficient-native-memory");

    /**
     * Generate new problem reading descriptions from plugin property bundle.
     * The parameter must not be {@code null}.
     * 
     * @param name identifier of problem
     * @return problem generated
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_UTIL, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
	private ProblemsUtil() {
	}
}
