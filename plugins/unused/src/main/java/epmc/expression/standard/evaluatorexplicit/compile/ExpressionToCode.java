package epmc.expression.standard.evaluatorexplicit.compile;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.value.Value;

public interface ExpressionToCode {
    default void setVariables(Expression[] variables) {
    }
    
    default void setExpression(Expression expression) {
    }
    
    default void setReturnType(Class<?> returnType) {
    }
    
    default Class<?> getReturnType() {
        return Value.class;
    }
    
    // TODO setCache(...)
    
    default void setID() {
    }
    
    boolean canHandle() throws EPMCException;
    
    default CharSequence generateImports() throws EPMCException {
        return "";
    }
    
    default CharSequence generateFields() throws EPMCException {
        return "";
    }
    
    default CharSequence generateMethods() throws EPMCException {
        return "";
    }
    
    CharSequence generateTree() throws EPMCException;
}
