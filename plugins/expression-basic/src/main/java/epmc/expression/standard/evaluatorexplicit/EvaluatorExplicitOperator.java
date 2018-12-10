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

import static epmc.error.UtilError.fail;

import java.util.Arrays;

import epmc.value.ValueBoolean;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ProblemsExpression;
import epmc.expressionevaluator.ExpressionToType;
import epmc.operator.Operator;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.ProblemsValue;
import epmc.value.Type;
import epmc.value.Value;

public final class EvaluatorExplicitOperator implements EvaluatorExplicit, EvaluatorExplicitBoolean {
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
            for (Expression variable : variables) {
                if (expression.equals(variable)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public EvaluatorExplicit build() {
            return new EvaluatorExplicitOperator(this);
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

    public final static String IDENTIFIER = "operator";

    private final Expression[] variables;
    private final ExpressionOperator expression;
    private final Operator operator;
    private final OperatorEvaluator evaluator;
    private final EvaluatorExplicit[] operands;
    private final Value[] operandValues;
    private final Value result;
    private Value[] values;

    private EvaluatorExplicitOperator(Builder builder) {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        variables = builder.getVariables();
        expression = (ExpressionOperator) builder.getExpression();
        operator = expression.getOperator();
        operands = new EvaluatorExplicit[expression.getOperands().size()];
        operandValues = new Value[expression.getOperands().size()];
        Type[] types = new Type[expression.getOperands().size()];
        int opNr = 0;
        for (Expression operand : expression.getOperands()) {
            operands[opNr] = UtilEvaluatorExplicit.newEvaluator(null, operand, variables, builder.getCache(), builder.getExpressionToType());
            operandValues[opNr] = operands[opNr].getResultValue();
            assert operandValues[opNr] != null : opNr;
            types[opNr] = operands[opNr].getResultValue().getType();
            opNr++;
        }
        try {
            evaluator = ContextValue.get().getEvaluator(operator, types);
        } catch (EPMCException e) {
            if (e.getProblem().equals(ProblemsValue.OPTIONS_NO_OPERATOR_AVAILABLE)) {
                fail(ProblemsExpression.EXPRESSION_INCONSISTENT_OPERATOR, expression.getPositional(), operator, Arrays.toString(types));
            }
            throw e;
        }

        assert evaluator != null : operator + " " + Arrays.toString(types) + " " + operands[0];
        assert evaluator.resultType() != null : operator;
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
    public Value getResultValue() {
        return result;
    }

    @Override
    public boolean evaluateBoolean() {
        evaluate();
        return ValueBoolean.as(result).getBoolean();
    }
}
