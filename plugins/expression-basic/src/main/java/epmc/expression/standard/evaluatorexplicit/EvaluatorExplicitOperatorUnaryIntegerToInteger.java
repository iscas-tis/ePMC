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
import epmc.operator.OperatorAddInverse;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.Value;

public final class EvaluatorExplicitOperatorUnaryIntegerToInteger implements EvaluatorExplicitInteger {
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
            if (!ExpressionOperator.is(expression)) {
                return false;
            }
            ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
            Operator opName = expressionOperator.getOperator();
            for (Expression variable : variables) {
                if (expression.equals(variable)) {
                    return false;
                }
            }
            if (!opName.equals(OperatorAddInverse.ADD_INVERSE)) {
                return false;
            }
            for (Expression child : expressionOperator.getOperands()) {
                EvaluatorExplicit op = UtilEvaluatorExplicit.newEvaluator(null, child, variables, cache, expressionToType);
                if (!TypeInteger.is(op.getType())) {
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
        public EvaluatorExplicit build() {
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

    private final OperatorEvaluator evaluator;
    private Value[] values;

    private EvaluatorExplicitOperatorUnaryIntegerToInteger(Builder builder) {
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
        if (operator.equals(OperatorAddInverse.ADD_INVERSE)) {
            unaryIntegerToInteger = a -> -a;
        } else {
            unaryIntegerToInteger = null;
        }
        evaluator = ContextValue.get().getEvaluator(expression.getOperator(), types);
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
        return unaryIntegerToInteger.call(operands[0].evaluateInteger());
    }
}
