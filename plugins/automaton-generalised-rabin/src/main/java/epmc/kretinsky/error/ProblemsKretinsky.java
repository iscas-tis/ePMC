package epmc.kretinsky.error;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsKretinsky {
    private final static String ERROR_KRETINSKY = "ErrorKretinsky";

    public final static Problem KRETINSKY_PO_OPT_CMD_LINE_SET_MULTIPLE = newProblem("rddl-po-opt-cmd-line-set-multiple");
    public final static Problem KRETINSKY_PO_INV_PGR_OPT_VALUE = newProblem("kretinsky-po-inv-prg-opt-value");

    private static Problem newProblem(String name) {
    	assert name != null;
    	return UtilError.newProblem(ERROR_KRETINSKY, ProblemsKretinsky.class, name);
    }
    
    private ProblemsKretinsky() {
    }
}
