package epmc.expression.standard.evaluatordd;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.value.ContextValue;

public interface EvaluatorDD extends Closeable {
    String getIdentifier();
    
    void setVariables(Map<Expression,VariableDD> variables);
    
    void setExpression(Expression expression);

    void setContextValue(ContextValue context);
    
    boolean canHandle() throws EPMCException;
    
    void build() throws EPMCException;

    DD getDD() throws EPMCException;
    
    List<DD> getVector();
    
    @Override
    void close();
}
