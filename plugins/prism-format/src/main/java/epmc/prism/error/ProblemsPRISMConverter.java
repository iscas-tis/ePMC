package epmc.prism.error;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsPRISMConverter {
    private final static String PROBLEMS_PRISM_CONVERTER = "ProblemsPRISMConverter";
    public final static Problem PRISM_CONVERTER_MULTIPLE_INIT = newProblem("prism-converter-multiple-init");
    public final static Problem PRISM_CONVERTER_VARIABLE_TYPE = newProblem("prism-converter-variable-type");

    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_PRISM_CONVERTER, name);
    }

    private ProblemsPRISMConverter() {
    }
}
