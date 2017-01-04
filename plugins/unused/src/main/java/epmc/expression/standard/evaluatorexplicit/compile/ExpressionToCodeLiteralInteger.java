package epmc.expression.standard.evaluatorexplicit.compile;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;

public final class ExpressionToCodeLiteralInteger implements ExpressionToCode {
    public final static String IDENTIFIER = "literal-integer";
    
    private Expression expression;
    private Class<?> returnType;

    @Override
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }
    
    @Override
    public boolean canHandle() throws EPMCException {
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        // TODO
        if (expression.getType(null) == null) {
            return false;
        }
        // TODO
        if (!TypeInteger.isInteger(expression.getType(null))) {
            return false;
        }
        if (returnType != null && returnType != int.class) {
            return false;
        }
        return true;
    }

    @Override
    public Class<?> getReturnType() {
        return int.class;
    }

    @Override
    public CharSequence generateTree() throws EPMCException {
        StringBuilder builder = new StringBuilder();
        builder.append(ValueInteger.asInteger(getValue(expression)).getInt());
        return builder;
    }
    
    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }
}
