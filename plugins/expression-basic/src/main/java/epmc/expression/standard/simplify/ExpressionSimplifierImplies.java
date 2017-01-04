package epmc.expression.standard.simplify;

import epmc.value.OperatorImplies;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;

public final class ExpressionSimplifierImplies implements ExpressionSimplifier {
    public final static String IDENTIFIER = "implies";

    @Override
    public Expression simplify(ExpressionToType expressionToType, Expression expression) {
        assert expression != null;
        if (!isImplies(expression)) {
            return null;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return UtilExpressionStandard.opOr(expressionToType.getContextValue(), UtilExpressionStandard.opNot(expressionToType.getContextValue(), expressionOperator.getOperand1()), expressionOperator.getOperand2());
    }
    
    private static boolean isImplies(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .getIdentifier()
                .equals(OperatorImplies.IDENTIFIER);
    }
}
