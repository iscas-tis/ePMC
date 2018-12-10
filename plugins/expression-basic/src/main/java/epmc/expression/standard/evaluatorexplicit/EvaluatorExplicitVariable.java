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

import epmc.value.ValueBoolean;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expressionevaluator.ExpressionToType;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
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
        public boolean canHandle() {
            for (Expression variable : variables) {
                if (variable.equals(expression)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public EvaluatorExplicit build() {
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
    private final OperatorEvaluator set;
    private Value[] values;

    private EvaluatorExplicitVariable(Builder builder) {
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
        assert builder.getExpressionToType().getType(variables[index]) != null : variables[index] + " " +  builder.getExpressionToType();
        result = builder.getExpressionToType().getType(variables[index]).newValue();
        set = ContextValue.get().getEvaluator(OperatorSet.SET, builder.getExpressionToType().getType(variables[index]), builder.getExpressionToType().getType(variables[index]));
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
    }
    
    @Override
    public void evaluate() {
        assert values != null;
        for (Value value : values) {
            assert value != null;
        }
        set.apply(result, values[index]);
    }

    @Override
    public boolean evaluateBoolean() {
        assert values != null;
        for (Value value : values) {
            assert value != null;
        }
        return ValueBoolean.as(values[index]).getBoolean();
    }

    @Override
    public Value getResultValue() {
        return result;
    }
}
