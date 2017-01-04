package epmc.expression.standard.evaluatordd;

import java.util.List;
import java.util.Map;

import epmc.value.OperatorLe;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.value.ContextValue;

public final class EvaluatorDDOperatorVectorLe implements EvaluatorDD {
    public final static String IDENTIFIER = "operator-vector-le";
    
    private Map<Expression, VariableDD> variables;
    private Expression expression;
    private DD dd;
    private boolean closed;

	private ContextValue context;
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setVariables(Map<Expression, VariableDD> variables) {
        this.variables = variables;
    }

    @Override
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean canHandle() throws EPMCException {
        return UtilEvaluatorDD.canIntegerVectorOperator(context, expression, OperatorLe.IDENTIFIER, variables);
    }

    @Override
    public void build() throws EPMCException {
        dd = UtilEvaluatorDD.applyVector(context, expression, variables, getContextDD()::twoCplLe);
    }

    @Override
    public DD getDD() throws EPMCException {
        assert dd != null;
        return dd;
    }

    @Override
    public List<DD> getVector() {
        return null;
    }

    @Override
    public void close() {
        closed = UtilEvaluatorDD.close(closed, dd, null);
    }

    private ContextDD getContextDD() throws EPMCException {
        return ContextDD.get(context);
    }

	@Override
	public void setContextValue(ContextValue context) {
		this.context = context;
	}
}
