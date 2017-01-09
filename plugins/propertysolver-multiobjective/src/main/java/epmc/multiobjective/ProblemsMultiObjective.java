package epmc.multiobjective;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsMultiObjective {
	public final static String ERROR_MULTI_OBJECTIVE = "ErrorMultiObjective";
    public final static Problem MULTI_OBJECTIVE_UNEXPECTED_INFEASIBLE = newProblem("multi-objective-unexpected-infeasible");
    public final static Problem MULTI_OBJECTIVE_INITIAL_NOT_SINGLETON = newProblem("multi-objective-initial-not-singleton");

    private static Problem newProblem(String name) {
    	assert name != null;
        return UtilError.newProblem(ERROR_MULTI_OBJECTIVE, name);
    }
    
	/**
	 * Private constructor to prevent instantiation of this class.
	 */
	private ProblemsMultiObjective() {
	}
}
