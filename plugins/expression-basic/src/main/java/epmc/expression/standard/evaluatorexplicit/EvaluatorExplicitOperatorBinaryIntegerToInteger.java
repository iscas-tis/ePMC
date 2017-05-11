/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

*****************************************************************************/

package epmc.expression.standard.evaluatorexplicit;

import java.util.Map;

import epmc.value.OperatorAdd;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMod;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorPow;
import epmc.value.OperatorSubtract;
import epmc.value.TypeInteger;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit.EvaluatorCacheEntry;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public final class EvaluatorExplicitOperatorBinaryIntegerToInteger implements EvaluatorExplicitInteger {
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
            String opName = expressionOperator.getOperator();
            for (Expression variable : variables) {
                if (expression.equals(variable)) {
                    return false;
                }
            }
            if (!opName.equals(OperatorAdd.IDENTIFIER)
                    && !opName.equals(OperatorMax.IDENTIFIER)
                    && !opName.equals(OperatorMin.IDENTIFIER)
                    && !opName.equals(OperatorMod.IDENTIFIER)
                    && !opName.equals(OperatorMultiply.IDENTIFIER)
                    && !opName.equals(OperatorPow.IDENTIFIER)
                    && !opName.equals(OperatorSubtract.IDENTIFIER)) {
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
            return new EvaluatorExplicitOperatorBinaryIntegerToInteger(this);
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
    
    @FunctionalInterface
    private static interface BinaryIntegerToInteger {
        int call(int a, int b);
    }

    public final static String IDENTIFIER = "operator-binary-integer-to-integer";
    
    private final Expression[] variables;
    private final ExpressionOperator expression;
    private final EvaluatorExplicitInteger[] operands;
    private final Value[] operandValues;
    private final Value result;
    private final BinaryIntegerToInteger binaryIntegerToInteger;

    private Operator operator;

    
    private EvaluatorExplicitOperatorBinaryIntegerToInteger(Builder builder) throws EPMCException {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = (ExpressionOperator) builder.getExpression();
        variables = builder.getVariables();
        Operator operator = ContextValue.get().getOperator(expression.getOperator());
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
        switch (expression.getOperator()) {
        case OperatorAdd.IDENTIFIER:
            binaryIntegerToInteger = (a,b) -> a+b;
            break;
        case OperatorMax.IDENTIFIER:
            binaryIntegerToInteger = (a,b) -> Math.max(a, b);
            break;
        case OperatorMin.IDENTIFIER:
            binaryIntegerToInteger = (a,b) -> Math.min(a, b);
            break;
        case OperatorMod.IDENTIFIER:
            binaryIntegerToInteger = (a,b) -> a % b;
            break;
        case OperatorMultiply.IDENTIFIER:
            binaryIntegerToInteger = (a,b) -> a * b;
            break;
        case OperatorPow.IDENTIFIER:
            binaryIntegerToInteger = (a,b) -> (int) Math.pow(a, b);
            break;
        case OperatorSubtract.IDENTIFIER:
            binaryIntegerToInteger = (a,b) -> a - b;
            break;
        default:
            binaryIntegerToInteger = null;
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
        return binaryIntegerToInteger.call(operands[0].evaluateInteger(values),
                operands[1].evaluateInteger(values));
    }
}
