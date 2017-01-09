package epmc.jani.dd;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in DD part of JANI plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsJANIDD {
	/** Base name of resource file containing plugin problem descriptions. */
    private final static String PROBLEMS_JANI_DD = "ProblemsJANIDD";
    
	/** Number of action bits insufficient. */
	public static final Problem JANI_DD_ACTION_BITS_INSUFFICIENT = newProblem("jani-dd-action-bits-insufficient");
	/** Deadlock state reached during symbolic state space exploration. */
	public static final Problem JANI_DD_DEADLOCK = newProblem("jani-dd-deadlock");
	/** Global variable was written multiple times in DD-based engine. */
	public static final Problem JANI_DD_GLOBAL_MULTIPLE = newProblem("jani-dd-global-multiple");
	/** Invalid assignment to variable during initial states computation. */
	public static final Problem JANI_DD_TRANSITION_INVALID_INITIAL_STATE = newProblem("jani-dd-transition-invalid-initial-state");
	/** Invalid assignment to variable during transition. */
	public static final Problem JANI_DD_TRANSITION_INVALID_ASSIGNMENT = newProblem("jani-dd-transition-invalid-assignment");
	/** Unsupported engine. */
	
	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_JANI_DD, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsJANIDD() {
    }
}
