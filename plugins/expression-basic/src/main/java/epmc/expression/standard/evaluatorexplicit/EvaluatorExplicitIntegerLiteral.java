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
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionLiteral;
import epmc.value.Value;

public final class EvaluatorExplicitIntegerLiteral implements EvaluatorExplicitInteger {
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
            assert expression != null;
            assert variables != null;
            if (!(ExpressionLiteral.isLiteral(expression))) {
                return false;
            }
            if (expression.getType(expressionToType) == null) {
                return false;
            }
            if (!TypeInteger.is(expression.getType(expressionToType))) {
                return false;
            }
            return true;
        }

        @Override
        public EvaluatorExplicit build() {
            return new EvaluatorExplicitIntegerLiteral(this);
        }

        @Override
        public EvaluatorExplicit.Builder setExpressionToType(
                ExpressionToType expressionToType) {
            this.expressionToType = expressionToType;
            return this;
        }
    }

    public final static String IDENTIFIER = "integer-literal";
    private final Expression[] variables;
    private final Expression expression;
    private final Value value;
    private final int valueInteger;

    private EvaluatorExplicitIntegerLiteral(Builder builder) {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = builder.getExpression();
        variables = builder.getVariables();
        value = getValue(expression);
        if (ValueInteger.is(value)) {
            valueInteger = ValueInteger.as(value).getInt();
        } else {
            valueInteger = -1;
        }
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
    public Value evaluate(Value... values) {
        assert expression != null;
        assert variables != null;
        return value;
    }

    @Override
    public int evaluateInteger(Value... values) {
        return valueInteger;
    }

    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }

    @Override
    public Value getResultValue() {
        return value;
    }
}
