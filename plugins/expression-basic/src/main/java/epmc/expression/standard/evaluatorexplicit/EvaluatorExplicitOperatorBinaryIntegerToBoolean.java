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
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorNe;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public final class EvaluatorExplicitOperatorBinaryIntegerToBoolean implements EvaluatorExplicitBoolean {
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
            if (!opName.equals(OperatorEq.EQ)
                    && !opName.equals(OperatorGe.GE)
                    && !opName.equals(OperatorGt.GT)
                    && !opName.equals(OperatorLe.LE)
                    && !opName.equals(OperatorLt.LT)
                    && !opName.equals(OperatorNe.NE)) {
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
            return new EvaluatorExplicitOperatorBinaryIntegerToBoolean(this);
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
    private static interface BinaryIntegerToBoolean {
        boolean call(int a, int b);
    }

    public final static String IDENTIFIER = "operator-binary-integer-to-boolean";

    private final Expression[] variables;
    private final ExpressionOperator expression;
    private final EvaluatorExplicitInteger[] operands;
    private final Value[] operandValues;
    private final Value result;
    private final BinaryIntegerToBoolean binaryIntegerToBoolean;

    private final OperatorEvaluator evaluator;
    private Value[] values;

    private boolean resultBoolean;

    private EvaluatorExplicitOperatorBinaryIntegerToBoolean(Builder builder) {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = (ExpressionOperator) builder.getExpression();
        variables = builder.getVariables();
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
        if (operator.equals(OperatorEq.EQ)) {
            binaryIntegerToBoolean = (a,b) -> a == b;        	
        } else if (operator.equals(OperatorGe.GE)) {
            binaryIntegerToBoolean = (a,b) -> a >= b;        	
        } else if (operator.equals(OperatorGt.GT)) {
            binaryIntegerToBoolean = (a,b) -> a > b;
        } else if (operator.equals(OperatorLe.LE)) {
            binaryIntegerToBoolean = (a,b) -> a <= b;
        } else if (operator.equals(OperatorLt.LT)) {
            binaryIntegerToBoolean = (a,b) -> a < b;
        } else if (operator.equals(OperatorNe.NE)) {
            binaryIntegerToBoolean = (a,b) -> a != b;
        } else {
            binaryIntegerToBoolean = null;
        }
        this.evaluator = ContextValue.get().getEvaluator(expression.getOperator(), types);
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
        assert values != null;
        assert UtilEvaluatorExplicit.assertValues(values);
        for (EvaluatorExplicit operand : operands) {
            operand.evaluate();
        }
        evaluator.apply(result, operandValues);
    }

    @Override
    public boolean evaluateBoolean() {
        assert values != null;
        resultBoolean = binaryIntegerToBoolean.call(operands[0].evaluateInteger(),
                operands[1].evaluateInteger());
        return resultBoolean;
    }

    @Override
    public Value getResultValue() {
        return result;
    }
}
