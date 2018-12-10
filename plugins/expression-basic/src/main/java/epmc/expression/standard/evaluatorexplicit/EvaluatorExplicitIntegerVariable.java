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

import epmc.value.TypeInteger;
import epmc.value.ValueInteger;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expressionevaluator.ExpressionToType;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

public class EvaluatorExplicitIntegerVariable implements EvaluatorExplicitInteger {
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
                if (variable.equals(expression)
                        && expressionToType.getType(variable) != null
                        && TypeInteger.is(expressionToType.getType(variable))) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public EvaluatorExplicit build() {
            return new EvaluatorExplicitIntegerVariable(this);
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

    public final static String IDENTIFIER = "integer-variable";

    private final Expression[] variables;
    private final Expression expression;
    private final int index;
    private final Value result;
    private Value[] values;

    private EvaluatorExplicitIntegerVariable(Builder builder) {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = builder.getExpression();
        variables = builder.getVariables();
        int index = -1;
        for (int i = 0; i < variables.length; i++) {
            Expression variable = variables[i];
            if (variable.equals(expression)) {
                index = i;
                break;
            }
        }
        this.index = index;
        result = builder.getExpressionToType().getType(variables[index]).newValue();
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
        ValueAlgebra.as(result).set(ValueInteger.as(values[index]).getInt());
    }

    @Override
    public int evaluateInteger() {
        assert values != null;
        for (Value value : values) {
            assert value != null;
        }
        return ValueInteger.as(values[index]).getInt();
    }

    @Override
    public Value getResultValue() {
        return result;
    }
}
