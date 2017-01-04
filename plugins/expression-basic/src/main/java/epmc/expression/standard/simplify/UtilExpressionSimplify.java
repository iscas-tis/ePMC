package epmc.expression.standard.simplify;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.Type;

public final class UtilExpressionSimplify {
    public static Expression simplify(ExpressionToType expressionToType, Expression expression, Type preferredType) throws EPMCException {
        ContextExpressionSimplifier context = new ContextExpressionSimplifier(expressionToType.getContextValue());
        return context.simplify(expressionToType, expression);
    }
    
    public static Expression simplify(ExpressionToType expressionToType, Expression expression) throws EPMCException {
        return simplify(expressionToType, expression, null);
    }
    
    private UtilExpressionSimplify() {
    }
}
