package epmc.jani.type.sta;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in STA part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANISTA {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_STA = "ProblemsJANISTA";

	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_JANI_STA, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANISTA() {
    }
}
