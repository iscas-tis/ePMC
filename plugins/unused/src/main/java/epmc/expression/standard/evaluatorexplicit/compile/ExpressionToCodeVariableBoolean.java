package epmc.expression.standard.evaluatorexplicit.compile;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.value.TypeInteger;

public final class ExpressionToCodeVariableBoolean implements ExpressionToCode {
    public final static String IDENTIFIER = "variable-boolean";
    
    private Expression expression;
    private Class<?> returnType;
    private Expression[] variables;

    @Override
    public void setExpression(Expression expression) {
        this.expression = expression;
    }
    
    @Override
    public void setVariables(Expression[] variables) {
        this.variables = variables;
    }

    @Override
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }
    
    @Override
    public boolean canHandle() throws EPMCException {
        if (!(expression instanceof ExpressionIdentifier)) {
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
        int index = 0;
        for (Expression checkVariable : variables) {
            if (expression.equals(checkVariable)) {
                break;
            }
            index++;
        }
        assert index < variables.length;
        StringBuilder builder = new StringBuilder();
        builder.append(EvaluatorExplicitCompile.VARIABLES_PARAMETER);
        builder.append("[");
        builder.append(index);
        builder.append("].getBoolean()");
        
        return builder;
    }
}
