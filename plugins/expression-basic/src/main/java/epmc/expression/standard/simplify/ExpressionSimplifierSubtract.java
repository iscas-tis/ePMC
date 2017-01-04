package epmc.expression.standard.simplify;

import epmc.value.OperatorSubtract;
import epmc.value.TypeAlgebra;
import epmc.value.ValueAlgebra;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;


public final class ExpressionSimplifierSubtract implements ExpressionSimplifier {
    public final static String IDENTIFIER = "subtract";

    @Override
    public Expression simplify(ExpressionToType expressionToType, Expression expression) throws EPMCException {
        assert expression != null;
        if (!isSubtract(expression)) {
            return null;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        if (isZero(expressionOperator.getOperand1())) {
            return UtilExpressionStandard.opAddInverse(expressionToType.getContextValue(), expressionOperator.getOperand2());
        }
        if (isZero(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1();
        }
        if (expressionOperator.getOperand1().equals(expressionOperator.getOperand2())) {
            return new ExpressionLiteral.Builder()
                    .setValue(TypeAlgebra.asAlgebra(expressionOperator.getType(expressionToType)).getZero())
                    .build();
        }
        return null;
    }
    
    private static boolean isSubtract(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .getIdentifier()
                .equals(OperatorSubtract.IDENTIFIER);
    }

    private boolean isZero(Expression expression) {
        assert expression != null;
        return expression instanceof ExpressionLiteral
                && ValueAlgebra.asAlgebra(((ExpressionLiteral) expression).getValue()).isZero();
    }
}
