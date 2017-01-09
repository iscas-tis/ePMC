package epmc.expression.standard.evaluatorexplicit;

import java.util.Map;

import epmc.value.OperatorAddInverse;
import epmc.value.OperatorMultiplyInverse;
import epmc.value.TypeInteger;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit.EvaluatorCacheEntry;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public final class EvaluatorExplicitOperatorUnaryIntegerToInteger implements EvaluatorExplicitInteger {
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
            String opName = expressionOperator.getOperator().getIdentifier();
            for (Expression variable : variables) {
                if (expression.equals(variable)) {
                    return false;
                }
            }
            if (!opName.equals(OperatorAddInverse.IDENTIFIER)
                    && !opName.equals(OperatorMultiplyInverse.IDENTIFIER)) {
                return false;
            }
            for (Expression child : expressionOperator.getOperands()) {
                if (child.getType(expressionToType) == null
                        || !TypeInteger.isInteger(child.getType(expressionToType))) {
                    return false;
                }
            }
            for (Expression operand : expressionOperator.getOperands()) {
                EvaluatorExplicit op = UtilEvaluatorExplicit.newEvaluator(null, operand, variables, cache, expressionToType);
                if (!(op instanceof EvaluatorExplicitInteger)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public EvaluatorExplicit build() throws EPMCException {
            return new EvaluatorExplicitOperatorUnaryIntegerToInteger(this);
        }

		@Override
		public EvaluatorExplicit.Builder setExpressionToType(
				ExpressionToType expressionToType) {
			this.expressionToType = expressionToType;
			return this;
		}
		
		public ExpressionToType getExpressionToType() {
			return expressionToType;
		}
    }

    @FunctionalInterface
    private static interface UnaryIntegerToInteger {
        int call(int a);
    }

    public final static String IDENTIFIER = "operator-unary-integer-to-integer";
    
    private final Expression[] variables;
    private final ExpressionOperator expression;
    private final EvaluatorExplicitInteger[] operands;
    private final Value[] operandValues;
    private final Value result;
    private final UnaryIntegerToInteger unaryIntegerToInteger;

    private final Operator operator;

    
    private EvaluatorExplicitOperatorUnaryIntegerToInteger(Builder builder) throws EPMCException {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = (ExpressionOperator) builder.getExpression();
        variables = builder.getVariables();
        Operator operator = expression.getOperator();
        operands = new EvaluatorExplicitInteger[expression.getOperands().size()];
        operandValues = new Value[expression.getOperands().size()];
        Type[] types = new Type[expression.getOperands().size()];
        int opNr = 0;
        for (Expression operand : expression.getOperands()) {
            operands[opNr] = (EvaluatorExplicitInteger) UtilEvaluatorExplicit.newEvaluator(null, operand, variables, builder.getCache(), builder.getExpressionToType());
            operandValues[opNr] = operands[opNr].getResultValue();
            types[opNr] = operands[opNr].getResultValue().getType();
            opNr++;
        }
        result = operator.resultType(types).newValue();
        switch (operator.getIdentifier()) {
        case OperatorAddInverse.IDENTIFIER:
            unaryIntegerToInteger = a -> -a;
            break;
        case OperatorMultiplyInverse.IDENTIFIER:
            unaryIntegerToInteger = a -> 1/a;
            break;
        default:
            unaryIntegerToInteger = null;
            break;
        }
        this.operator = operator;
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
        for (EvaluatorExplicit operand : operands) {
            operand.evaluate(values);
        }
        operator.apply(result, operandValues);
        return result;
    }
    
    @Override
    public Value getResultValue() {
        return result;
    }

    @Override
    public int evaluateInteger(Value... values) throws EPMCException {
        return unaryIntegerToInteger.call(operands[0].evaluateInteger(values));
    }
}
