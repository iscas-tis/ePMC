package epmc.jani.type.mdp;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in MDP part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANIMDP {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_MDP = "ProblemsJANIMDP";
	/** Edges of MDP models must not have rate specifications. */
	public static final Problem JANI_MDP_EDGE_FORBIDS_RATE = newProblem("jani-mdp-edge-forbids-rate");
	/** Time progress conditions are disallowed in MDPs. */
	public static final Problem JANI_MDP_DISALLOWED_TIME_PROGRESSES = newProblem("jani-mdp-disallowed-time-progresses");

	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_JANI_MDP, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANIMDP() {
    }
}
