package epmc.jani.type.lts;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in LTS part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANILTS {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_LTS = "ProblemsJANILTS";
    
	public static final Problem JANI_LTS_ONLY_ONE_DESTINATIONS = newProblem("jani-lts-only-one-destination");
	public static final Problem JANI_LTS_NO_PROBABILITIES = newProblem("jani-lts-no-probabilities");
	/** Time progress conditions are disallowed in DTMCs. */
	public static final Problem JANI_LTS_DISALLOWED_TIME_PROGRESSES = newProblem("jani-lts-disallowed-time-progresses");

	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_JANI_LTS, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANILTS() {
    }
}
