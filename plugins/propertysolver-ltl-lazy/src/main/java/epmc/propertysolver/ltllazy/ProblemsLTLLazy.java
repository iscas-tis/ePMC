package epmc.propertysolver.ltllazy;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsLTLLazy {
	public final static String ERROR_LTL_LAZY = "ErrorLTLLazy";
    public final static Problem LTL_LAZY_COULDNT_DECIDE = newProblem("ltl-lazy-couldnt-decide");

    private static Problem newProblem(String name) {
        return UtilError.newProblem(ERROR_LTL_LAZY, name);
    }
    
	private ProblemsLTLLazy() {
	}
}
