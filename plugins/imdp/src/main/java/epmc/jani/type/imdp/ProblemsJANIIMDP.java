package epmc.jani.type.imdp;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in IMDP part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANIIMDP {
    /** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_MDP = "ProblemsJANIMDP";
    /** Edges of IMDP models must not have rate specifications. */
    public static final Problem JANI_IMDP_EDGE_FORBIDS_RATE = newProblem("jani-imdp-edge-forbids-rate");
    /** Time progress conditions are disallowed in IMDPs. */
    public static final Problem JANI_IMDP_DISALLOWED_TIME_PROGRESSES = newProblem("jani-imdp-disallowed-time-progresses");

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
    private ProblemsJANIIMDP() {
    }
}
