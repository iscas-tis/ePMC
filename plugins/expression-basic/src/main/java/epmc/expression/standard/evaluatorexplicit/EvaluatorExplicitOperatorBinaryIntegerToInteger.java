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

import epmc.value.OperatorEvaluator;
import epmc.value.TypeInteger;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionOperator;
import epmc.expressionevaluator.ExpressionToType;
import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorMod;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorSubtract;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public final class EvaluatorExplicitOperatorBinaryIntegerToInteger implements EvaluatorExplicitInteger {
    public final static class Builder implements EvaluatorExplicit.Builder {
        private Expression[] variables;
        private Expression expression;
        private EvaluatorCache cache;
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
        public Builder setCache(EvaluatorCache cache) {
            this.cache = cache;
            return this;
        }

        private EvaluatorCache getCache() {
            return cache;
        }

        @Override
        public boolean canHandle() {
            assert expression != null;
            if (!(expression instanceof ExpressionOperator)) {
                return false;
            }
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            Operator opName = expressionOperator.getOperator();
            for (Expression variable : variables) {
                if (expression.equals(variable)) {
                    return false;
                }
            }
            if (!opName.equals(OperatorAdd.ADD)
                    && !opName.equals(OperatorMax.MAX)
                    && !opName.equals(OperatorMin.MIN)
                    && !opName.equals(OperatorMod.MOD)
                    && !opName.equals(OperatorMultiply.MULTIPLY)
                    && !opName.equals(OperatorPow.POW)
                    && !opName.equals(OperatorSubtract.SUBTRACT)) {
                return false;
            }
            for (Expression operand : expressionOperator.getOperands()) {
                EvaluatorExplicit op = UtilEvaluatorExplicit.newEvaluator(null, operand, variables, cache, expressionToType);
                if (!(op instanceof EvaluatorExplicitInteger)) {
                    return false;
                }
                if (!TypeInteger.is(op.getType())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public EvaluatorExplicit build() {
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

    private final OperatorEvaluator evaluator;
    private Value[] values;

    private int integerResult;

    private EvaluatorExplicitOperatorBinaryIntegerToInteger(Builder builder) {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = (ExpressionOperator) builder.getExpression();
        variables = builder.getVariables();
        //        Operator operator = ContextValue.get().getOperator(expression.getOperator());
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
        Operator operator = expression.getOperator();
        if (operator.equals(OperatorAdd.ADD)) {
            binaryIntegerToInteger = (a,b) -> a+b;        	
        } else if (operator.equals(OperatorMax.MAX)) {
            binaryIntegerToInteger = (a,b) -> Math.max(a, b);
        } else if (operator.equals(OperatorMin.MIN)) {
            binaryIntegerToInteger = (a,b) -> Math.min(a, b);
        } else if (operator.equals(OperatorMod.MOD)) {
            binaryIntegerToInteger = (a,b) -> a % b;
        } else if (operator.equals(OperatorMultiply.MULTIPLY)) {
            binaryIntegerToInteger = (a,b) -> a * b;
        } else if (operator.equals(OperatorPow.POW)) {
            binaryIntegerToInteger = (a,b) -> (int) Math.pow(a, b);
        } else if (operator.equals(OperatorSubtract.SUBTRACT)) {
            binaryIntegerToInteger = (a,b) -> a - b;
        } else {
            binaryIntegerToInteger = null;
        }
        evaluator = ContextValue.get().getEvaluator(operator, types);
        result = evaluator.resultType().newValue();
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
    public void setValues(Value... values) {
        this.values = values;
        for (EvaluatorExplicit operand : operands) {
            operand.setValues(values);
        }
    }
    
    @Override
    public void evaluate() {
        assert UtilEvaluatorExplicit.assertValues(values);
        for (EvaluatorExplicit operand : operands) {
            operand.evaluate();
        }
        evaluator.apply(result, operandValues);
    }

    @Override
    public Value getResultValue() {
        return result;
    }

    @Override
    public int evaluateInteger() {
        integerResult = binaryIntegerToInteger.call(operands[0].evaluateInteger(),
                operands[1].evaluateInteger());
        return integerResult;
    }
}
