package epmc.main.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Collections of possible problems in main part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsEPMC {
    /** Base name of file containing problem descriptors. */
    private final static String PROBLEMS_EPMC = "ProblemsEPMC";
    /** File to read properties from does not exist. */
    public static final Problem FILE_NOT_EXISTS = newProblem("file-not-exists");

    /**
     * Generate new problem reading descriptions from EPMC property bundle.
     * The parameter must not be {@code null}.
     * 
     * @param name identifier of problem
     * @return problem generated
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_EPMC, name);
    }
    
    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsEPMC() {
    }
}
