package epmc.expression.standard.evaluatordd;

import java.util.List;
import java.util.Map;

import epmc.value.ValueEnum;
import epmc.value.ValueInteger;
import epmc.value.ValueNumBitsKnown;
import epmc.dd.ContextDD;
import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.value.ContextValue;
import epmc.value.Value;

public class EvaluatorDDLiteral implements EvaluatorDD {
    public final static String IDENTIFIER = "literal";
    
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
        assert variables != null;
    }

    @Override
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public boolean canHandle() {
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        return true;
    }

    @Override
    public void build() throws EPMCException {
        Value value = getValue(expression);
        boolean useVector = false;
//        boolean useVector = options.getBoolean(OptionsExpressionBasic.DD_EXPRESSION_VECTOR);
        ContextDD contextDD = ContextDD.get(context);
        if (useVector && ValueInteger.isInteger(value)) {
            this.vector = contextDD.twoCplFromInt(ValueInteger.asInteger(value).getInt());
        } else if (useVector && ValueEnum.isEnum(value)) {
            int numBits = ValueNumBitsKnown.getNumBits(value);
            int number = ValueEnum.asEnum(value).getEnum().ordinal();
            this.vector = contextDD.twoCplFromInt(number, numBits);
        } else {
            this.dd = contextDD.newConstant(value);
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

    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
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
