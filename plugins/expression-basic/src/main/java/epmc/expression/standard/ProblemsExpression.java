package epmc.expression.standard;

import epmc.error.Problem;
import epmc.error.UtilError;

public final class ProblemsExpression {
    private final static String PROBLEMS_EXPRESSION = "ProblemsExpression";
    
    public final static Problem EXPR_INCONSISTENT = newProblem("expr-inconsistent");
    public final static Problem VALUE_INCONSISTENT_INFO = newProblem("value-inconsistent-info");

    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(PROBLEMS_EXPRESSION, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsExpression() {
    }
}
