package epmc.expression.standard.simplify;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;

/**
 * Expression simplifier interface.
 * 
 * @author Ernst Moritz Hahn
 */
public interface ExpressionSimplifier {
    /**
     * Simplifies the given expression.
     * If possible, the simplifier will return an expression which is equivalent
     * but shorter than its input expression.
     * If this simplifier cannot simplify this expression (further), it will
     * return {@code null}.
     * The expression parameter must not be {@code null}.
     * 
     * @param expressionToType expression types
     * @param expression expression to simplify
     * @return simplified expression, or {@code null}
     * @throws EPMCException thrown in case of problems
     */
    Expression simplify(ExpressionToType expressionToType, Expression expression) throws EPMCException;
}
