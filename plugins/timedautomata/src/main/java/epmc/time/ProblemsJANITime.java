package epmc.time;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting potential problems of timed automata of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANITime {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_TIME = "ProblemsJANITime";
    /** Model cannot be translated because it contains an unknown component type. */
	public static final Problem JANI_TIME_UNKNOWN_COMPONENT = newProblem("jani-time-unknown-component");
	/** Clock is compared to an expression which does not evaluate to integer. */
	public static final Problem JANI_TIME_NON_INTEGER_CLOCK_COMPARISON = newProblem("jani-time-non-integer-clock-comparison");

	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_JANI_TIME, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANITime() {
    }
}
