package epmc.sexpression;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsSExpression {
	public final static String ERROR_S_EXPRESSION = "ErrorSExpression";
    public final static Problem SEXPRESSION_UNEXPECTED_END_OF_INPUT = newProblem("sexpression-unexpected-end-of-input");
    public final static Problem SEXPRESSION_UNEXPECTED_CLOSING_BRACE = newProblem("sexpression-unexpected-closing-brace");
    public final static Problem SEXPRESSION_END_OF_INPUT_EXPECTED = newProblem("sexpression-end-of-input-expected");

    private static Problem newProblem(String name) {
        return UtilError.newProblem(ERROR_S_EXPRESSION, name);
    }
	
	private ProblemsSExpression() {
	}
}
