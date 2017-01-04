package epmc.rddl.error;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsRDDL {
    private final static String ERROR_RDDL = "ErrorRDDL";
    public final static Problem RDDL_PARSER_FAILED = newProblem("rddl-parser-failed");
    public final static Problem RDDL_PO_OPT_CMD_LINE_SET_MULTIPLE = newProblem("rddl-po-opt-cmd-line-set-multiple");
    public final static Problem RDDL_OPTION_BOOLEAN = newProblem("rddl-option-boolean");

    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(ERROR_RDDL, name);
    }
    
    private ProblemsRDDL() {
    }
}
