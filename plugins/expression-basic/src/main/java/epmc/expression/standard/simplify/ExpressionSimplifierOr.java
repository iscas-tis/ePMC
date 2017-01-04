package epmc.expression.standard.simplify;

import epmc.value.OperatorNot;
import epmc.value.OperatorOr;
import epmc.value.ValueBoolean;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.value.ContextValue;

public final class ExpressionSimplifierOr implements ExpressionSimplifier {
    public final static String IDENTIFIER = "or";

    @Override
    public Expression simplify(ExpressionToType expressionToType, Expression expression) {
        assert expression != null;
        ContextValue context = expressionToType.getContextValue();
        if (!isOr(expression)) {
            return null;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        if (isFalse(expressionOperator.getOperand1())) {
            return expressionOperator.getOperand2();
        }
        if (isFalse(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1();
        }
        if (isTrue(expressionOperator.getOperand1())) {
            return ExpressionLiteral.getTrue(context);
        }
        if (isTrue(expressionOperator.getOperand2())) {
            return ExpressionLiteral.getTrue(context);
        }
        if (expressionOperator.getOperand1().equals(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1();
        }
        if (isNot(expressionOperator.getOperand1())
                && ((ExpressionOperator) expressionOperator.getOperand1()).getOperand1()
                .equals(expressionOperator.getOperand2())) {
            return ExpressionLiteral.getTrue(context);
        }
        if (isNot(expressionOperator.getOperand2())
                && ((ExpressionOperator) expressionOperator.getOperand2()).getOperand2()
                .equals(expressionOperator.getOperand1())) {
            return ExpressionLiteral.getTrue(context);
        }
        Expression left = simplify(expressionToType, expressionOperator.getOperand1());
        Expression right = simplify(expressionToType, expressionOperator.getOperand2());
        if (left != null && right != null) {
            return UtilExpressionStandard.opOr(expressionToType.getContextValue(), left, right);
        }
        if (left != null) {
            return UtilExpressionStandard.opOr(expressionToType.getContextValue(), left, expressionOperator.getOperand2());
        }
        if (right != null) {
            return UtilExpressionStandard.opOr(expressionToType.getContextValue(), expressionOperator.getOperand1(), right);
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
    
    private static boolean isOr(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .getIdentifier()
                .equals(OperatorOr.IDENTIFIER);
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
