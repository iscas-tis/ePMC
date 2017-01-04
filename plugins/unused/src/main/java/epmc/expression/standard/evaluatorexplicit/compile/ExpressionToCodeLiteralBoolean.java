package epmc.expression.standard.evaluatorexplicit.compile;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class ExpressionToCodeLiteralBoolean implements ExpressionToCode {
    public final static String IDENTIFIER = "literal-boolean";
    
    private final static String TRUE = "true";
    private final static String FALSE = "false";

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
        if (!TypeBoolean.isBoolean(expression.getType(null))) {
            return false;
        }
        if (returnType != null && returnType != boolean.class) {
            return false;
        }
        return true;
    }
    
    @Override
    public Class<?> getReturnType() {
        return boolean.class;
    }

    @Override
    public CharSequence generateTree() throws EPMCException {
        return ValueBoolean.asBoolean(getValue(expression)).getBoolean() ? TRUE : FALSE;
    }
    
    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }

}
