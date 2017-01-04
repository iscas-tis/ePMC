package epmc.expression.standard.simplify;

import epmc.value.OperatorAnd;
import epmc.value.OperatorNot;
import epmc.value.ValueBoolean;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.value.ContextValue;

public final class ExpressionSimplifierAnd implements ExpressionSimplifier {
    public final static String IDENTIFIER = "and";

    @Override
    public Expression simplify(ExpressionToType expressionToType, Expression expression) {
        assert expression != null;
        if (!isAnd(expression)) {
            return null;
        }
        ContextValue context = expressionToType.getContextValue();
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        if (isFalse(expressionOperator.getOperand1())) {
            return ExpressionLiteral.getFalse(context);
        }
        if (isFalse(expressionOperator.getOperand2())) {
            return ExpressionLiteral.getFalse(context);
        }
        if (isTrue(expressionOperator.getOperand1())) {
            return expressionOperator.getOperand2();
        }
        if (isTrue(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1();
        }
        if (expressionOperator.getOperand1()
                .equals(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1();
        }
        if (isNot(expressionOperator.getOperand1())
                && ((ExpressionOperator) expressionOperator.getOperand1())
                .getOperand1()
                .equals(expressionOperator.getOperand2())) {
            return ExpressionLiteral.getFalse(context);
        }
        if (isNot(expressionOperator.getOperand2())
                && ((ExpressionOperator) expressionOperator.getOperand2()).getOperand1()
                .equals(expressionOperator.getOperand1())) {
            return ExpressionLiteral.getFalse(context);
        }
        Expression left = simplify(expressionToType, expressionOperator.getOperand1());
        Expression right = simplify(expressionToType, expressionOperator.getOperand2());
        if (left != null && right != null) {
            return UtilExpressionStandard.opAnd(expressionToType.getContextValue(), left, right);
        }
        if (left != null) {
            return UtilExpressionStandard.opAnd(expressionToType.getContextValue(), left, expressionOperator.getOperand2());
        }
        if (right != null) {
            return UtilExpressionStandard.opAnd(expressionToType.getContextValue(), expressionOperator.getOperand1(), right);
        }
        return null;
    }
    
    private static boolean isNot(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .getIdentifier()
                .equals(OperatorNot.IDENTIFIER);
    }
    
    private static boolean isAnd(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .getIdentifier()
                .equals(OperatorAnd.IDENTIFIER);
    }
    
    private static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isFalse(expressionLiteral.getValue());
    }
    
    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return ValueBoolean.isTrue(expressionLiteral.getValue());
    }
}
