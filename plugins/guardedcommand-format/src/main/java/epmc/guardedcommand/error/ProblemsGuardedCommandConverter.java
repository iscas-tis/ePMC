package epmc.guardedcommand.error;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsGuardedCommandConverter {
    private final static String PROBLEMS_GUARDEDCOMMAND_CONVERTER = "ProblemsGuardedCommandConverter";
    public final static Problem GUARDEDCOMMAND_CONVERTER_MULTIPLE_INIT = newProblem("guardedcommand-converter-multiple-init");
    public final static Problem GUARDEDCOMMAND_CONVERTER_VARIABLE_TYPE = newProblem("guardedcommand-converter-variable-type");

    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(PROBLEMS_GUARDEDCOMMAND_CONVERTER, name);
    }

    private ProblemsGuardedCommandConverter() {
    }
}
