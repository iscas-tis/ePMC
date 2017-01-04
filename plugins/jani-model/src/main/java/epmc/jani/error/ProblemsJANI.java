package epmc.jani.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANI {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI = "ProblemsJANI";
    
    /** More than one model input file was given. */
	public static final Problem JANI_ONE_MODEL_FILE = newProblem("jani-one-input-file");
	public static final Problem JANI_UNSUPPORTED_ENGINE = newProblem("jani-unsupported-engine");
	
	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_JANI, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANI() {
    }
}
