package epmc.expression.standard.evaluatorexplicit;

import java.util.Map;

import epmc.value.OperatorIte;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit.EvaluatorCacheEntry;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public final class EvaluatorExplicitOperatorShortcutIfThenElse implements EvaluatorExplicit {
    public final static class Builder implements EvaluatorExplicit.Builder {
        private Expression[] variables;
        private Expression expression;
        private Map<EvaluatorCacheEntry, EvaluatorExplicit> cache;
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
        public Builder setCache(Map<EvaluatorCacheEntry, EvaluatorExplicit> cache) {
            this.cache = cache;
            return this;
        }
        
        private Map<EvaluatorCacheEntry, EvaluatorExplicit> getCache() {
            return cache;
        }

        @Override
        public boolean canHandle() throws EPMCException {
            assert expression != null;
            if (!(expression instanceof ExpressionOperator)) {
                return false;
            }
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            if (!expressionOperator.getOperator().getIdentifier().equals(OperatorIte.IDENTIFIER)) {
                return false;
            }
            for (Expression variable : variables) {
                if (expression.equals(variable)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public EvaluatorExplicit build() throws EPMCException {
            return new EvaluatorExplicitOperatorShortcutIfThenElse(this);
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
    
    public final static String IDENTIFIER = "operator-shortcut-if-then-else";
    
    private final Expression[] variables;
    private final ExpressionOperator expression;
    private final EvaluatorExplicit[] operands;
    private final Value[] operandValues;
    private final Value result;

    private EvaluatorExplicitOperatorShortcutIfThenElse(Builder builder) throws EPMCException {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = (ExpressionOperator) builder.getExpression();
        variables = builder.getVariables();
        Operator operator = expression.getOperator();
        operands = new EvaluatorExplicit[expression.getOperands().size()];
        operandValues = new Value[expression.getOperands().size()];
        Type[] types = new Type[expression.getOperands().size()];
        int opNr = 0;
        for (Expression operand : expression.getOperands()) {
            operands[opNr] = UtilEvaluatorExplicit.newEvaluator(null, operand, variables, builder.getCache(), builder.getExpressionToType());
            operandValues[opNr] = operands[opNr].getResultValue();
            types[opNr] = operands[opNr].getResultValue().getType();
            opNr++;
        }
        result = operator.resultType(types).newValue();
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
        for (Value variable : values) {
            assert variable != null;
        }
        if (((EvaluatorExplicitBoolean) operands[0]).evaluateBoolean(values)) {
            EvaluatorExplicit thenOp = operands[1];
            thenOp.evaluate(values);
            result.set(thenOp.getResultValue());
        } else {
            EvaluatorExplicit elseOp = operands[2];
            elseOp.evaluate(values);
            result.set(elseOp.getResultValue());
        }
        return result;
    }
    
    @Override
    public Value getResultValue() {
        return result;
    }
}
