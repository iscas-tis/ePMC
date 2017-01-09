package epmc.filter.error;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in filter plugin.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsFilter {
	/** Base name of resource file containing plugin problem descriptions. */
	public final static String PROBLEMS_FILTER = "ProblemsFilter";
	/** In a filter of type "state", more than one state fulfils the filter. */
    public final static Problem FILTER_STATE_MORE_THAN_ONE = newProblem("filter-state-more-than-one");

	/**
	 * Create new problem object using plugin resource file.
	 * The name parameter must not be {@code null}.
	 * 
	 * @param name problem identifier String
	 * @return newly created problem identifier
	 */
    private static Problem newProblem(String name) {
    	assert name != null;
        return UtilError.newProblem(PROBLEMS_FILTER, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsFilter() {
    }
}
