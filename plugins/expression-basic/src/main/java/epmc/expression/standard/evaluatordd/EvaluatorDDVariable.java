package epmc.expression.standard.evaluatordd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.options.Options;
import epmc.value.ContextValue;

public final class EvaluatorDDVariable implements EvaluatorDD {
    public final static String IDENTIFIER = "variable";
    
    private Map<Expression, VariableDD> variables;
    private Expression expression;
    private DD dd;
    private List<DD> vector;
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
    public boolean canHandle() {
        for (Expression variable : variables.keySet()) {
            if (variable.equals(expression)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void build() throws EPMCException {
        Options options = context.getOptions();
        boolean useVector = options.getBoolean(OptionsExpressionBasic.DD_EXPRESSION_VECTOR);
        ContextDD contextDD = ContextDD.get(context);
        VariableDD variableDD = variables.get(expression);
        
        if (useVector && variableDD.isInteger()) {
            List<DD> origVec = new ArrayList<>(contextDD.clone(variableDD.getDDVariables(0)));
            origVec.add(contextDD.newConstant(false));
            List<DD> add = contextDD.twoCplFromInt(variableDD.getLower());
            vector = contextDD.twoCplAdd(origVec, add);
            contextDD.dispose(add);
            contextDD.dispose(origVec);
        } else {
            dd = variableDD.getValueEncoding(0);
        }
    }

    @Override
    public DD getDD() throws EPMCException {
        dd = UtilEvaluatorDD.getDD(dd, vector, expression);
        assert dd != null;
        return dd;
    }

    @Override
    public List<DD> getVector() {
        return vector;
    }

    @Override
    public void close() {
        closed = UtilEvaluatorDD.close(closed, dd, vector);
    }

	@Override
	public void setContextValue(ContextValue context) {
		this.context = context;
	}
}
