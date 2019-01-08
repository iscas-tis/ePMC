package epmc.jani.type.qmc;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in QMC part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANIQMC {
    /** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_QMC = "ProblemsJANIQMC";
    /** Edges of QMC models must not have rate specifications. */
    public static final Problem JANI_QMC_EDGE_FORBIDS_RATE = newProblem("jani-qmc-edge-forbids-rate");
    /** Time progress conditions are disallowed in QMCs. */
    public static final Problem JANI_QMC_DISALLOWED_TIME_PROGRESSES = newProblem("jani-qmc-disallowed-time-progresses");

    /**
     * Create new problem object using plugin resource file.
     * The name parameter must not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_JANI_QMC, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANIQMC() {
    }
}
