package epmc.expression.standard.evaluatorexplicit;

import epmc.value.ValueBoolean;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.value.Value;

public class EvaluatorExplicitVariable implements EvaluatorExplicit, EvaluatorExplicitBoolean {
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
            for (Expression variable : variables) {
                if (variable.equals(expression)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public EvaluatorExplicit build() throws EPMCException {
            return new EvaluatorExplicitVariable(this);
        }

		@Override
		public EvaluatorExplicit.Builder setExpressionToType(
				ExpressionToType expressionToType) {
			this.expressionToType = expressionToType;
			return this;
		}
		
		private ExpressionToType getExpressionToType() {
			return expressionToType;
		}
    }

    public final static String IDENTIFIER = "variable";
    
    private final Expression[] variables;
    private final Expression expression;
    private final int index;
    private final Value result;

    private EvaluatorExplicitVariable(Builder builder) throws EPMCException {
        assert builder != null;
        assert builder.getVariables() != null;
        assert builder.getExpression() != null;
        variables = builder.getVariables();
        expression = builder.getExpression();
        int index = -1;
        for (int i = 0; i < variables.length; i++) {
            Expression variable = variables[i];
            if (variable.equals(expression)) {
                index = i;
                break;
            }
        }
        this.index = index;
        result = variables[index].getType(builder.getExpressionToType()).newValue();
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
        assert values != null;
        for (Value value : values) {
            assert value != null;
        }
        result.set(values[index]);
        return result;
    }

    @Override
    public boolean evaluateBoolean(Value... values) throws EPMCException {
        assert values != null;
        for (Value value : values) {
            assert value != null;
        }
        return ValueBoolean.asBoolean(values[index]).getBoolean();
    }
    
    @Override
    public Value getResultValue() {
        return result;
    }
}
