package epmc.jani.type.dtmc;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in CTMC part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANIDTMC {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_DTMC = "ProblemsJANIDTMC";
	/** Edges of DTMC models must not have rate specifications. */
	public static final Problem JANI_DTMC_EDGE_FORBIDS_RATE = newProblem("jani-dtmc-edge-forbids-rate");
	/** Multi-transition remain even though disallowed. */
	public static final Problem JANI_DTMC_DISALLOWED_MULTI_TRANSITIONS = newProblem("jani-dtmc-disallowed-multi-transitions");
	/** Time progress conditions are disallowed in DTMCs. */
	public static final Problem JANI_DTMC_DISALLOWED_TIME_PROGRESSES = newProblem("jani-dtmc-disallowed-time-progresses");

	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_JANI_DTMC, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANIDTMC() {
    }
}
