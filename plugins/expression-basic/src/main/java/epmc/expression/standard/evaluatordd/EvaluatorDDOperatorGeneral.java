package epmc.expression.standard.evaluatordd;

import java.util.List;
import java.util.Map;

import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.value.ContextValue;
import epmc.value.Operator;

public final class EvaluatorDDOperatorGeneral implements EvaluatorDD {
    public final static String IDENTIFIER = "operator-general";
    
    private Map<Expression, VariableDD> variables;
    private Expression expression;
    private ExpressionOperator expressionOperator;
    private DD dd;
    private boolean closed;

	private ContextValue context;
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setVariables(Map<Expression, VariableDD> variables) {
        assert this.variables == null;
        this.variables = variables;
    }

    @Override
    public void setExpression(Expression expression) {
        assert this.expression == null;
        this.expression = expression;
        if (expression instanceof ExpressionOperator) {
            expressionOperator = (ExpressionOperator) expression;
        }
    }

    @Override
    public boolean canHandle() {
        assert expression != null;
        assert variables != null;
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        if (variables.containsKey(expression)) {
            return false;
        }
        return true;
    }

    @Override
    public void build() throws EPMCException {
        Operator operator = expressionOperator.getOperator();
        DD[] dds = new DD[expressionOperator.getOperands().size()];
        for (int i = 0; i < expressionOperator.getOperands().size(); i++) {
            Expression operand = expressionOperator.getOperands().get(i);
            EvaluatorDD evaluator = UtilEvaluatorDD.newEvaluator(context, operand, variables);
            dds[i] = evaluator.getDD();
            assert dds[i] != null : expressionOperator.getOperands().get(i) + " " + i + " " + expression;
        }
        this.dd = getContextDD().apply(operator, dds);
    }

    @Override
    public DD getDD() {
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
