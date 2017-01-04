package epmc.expression.standard.evaluatorexplicit;

import epmc.value.TypeInteger;
import epmc.value.ValueInteger;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionLiteral;
import epmc.value.Value;

public final class EvaluatorExplicitIntegerLiteral implements EvaluatorExplicitInteger {
    public final static class Builder implements EvaluatorExplicit.Builder {
        private Expression[] variables;
        private Expression expression;
		private ExpressionToType expressionToType;

        @Override
        public String getIdentifier() {
            return IDENTIFIER;
        }

        @Override
        public Builder setVariables(Expression[] variables) {
            this.variables = variables;
            return this;
        }

        private Expression[] getVariables() {
            return variables;
        }
        
        @Override
        public Builder setExpression(Expression expression) {
            this.expression = expression;
            return this;
        }

        private Expression getExpression() {
            return expression;
        }
        
        @Override
        public boolean canHandle() throws EPMCException {
            assert expression != null;
            assert variables != null;
            if (!(expression instanceof ExpressionLiteral)) {
                return false;
            }
            if (expression.getType(expressionToType) == null) {
                return false;
            }
            if (!TypeInteger.isInteger(expression.getType(expressionToType))) {
                return false;
            }
            return true;
        }

        @Override
        public EvaluatorExplicit build() throws EPMCException {
            return new EvaluatorExplicitIntegerLiteral(this);
        }

		@Override
		public EvaluatorExplicit.Builder setExpressionToType(
				ExpressionToType expressionToType) {
			this.expressionToType = expressionToType;
			return this;
		}
    }
    
    public final static String IDENTIFIER = "integer-literal";
    private final Expression[] variables;
    private final Expression expression;
    private final Value value;
    private final int valueInteger;
    
    private EvaluatorExplicitIntegerLiteral(Builder builder) {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = builder.getExpression();
        variables = builder.getVariables();
        value = getValue(expression);
        if (ValueInteger.isInteger(value)) {
            valueInteger = ValueInteger.asInteger(value).getInt();
        } else {
            valueInteger = -1;
        }
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }
    
    @Override
    public Value evaluate(Value... values) throws EPMCException {
        assert expression != null;
        assert variables != null;
        return value;
    }
    
    @Override
    public int evaluateInteger(Value... values) throws EPMCException {
        return valueInteger;
    }
    
    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }

    @Override
    public Value getResultValue() {
        return value;
    }
}
