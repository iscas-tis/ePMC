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

package epmc.param.expressionevaluator;

import java.util.List;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expressionevaluator.ExpressionToType;
import epmc.options.Options;
import epmc.param.options.OptionsParam;
import epmc.param.value.TypeFunction;
import epmc.param.value.ValueFunction;
import epmc.value.Value;

public final class EvaluatorExplicitIdentifierParameter implements EvaluatorExplicit {
    public final static class Builder implements EvaluatorExplicit.Builder {
        private Expression[] variables;
        private Expression expression;

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
            if (!(ExpressionIdentifierStandard.is(expression))) {
                return false;
            }
            ExpressionIdentifierStandard expressionIdentifier = ExpressionIdentifierStandard.as(expression);
            List<String> parameters = Options.get().get(OptionsParam.PARAM_PARAMETER);
            if (!parameters.contains(expressionIdentifier.getName())) {
                return false;
            }
            return true;
        }

        @Override
        public EvaluatorExplicit build() {
            return new EvaluatorExplicitIdentifierParameter(this);
        }

        @Override
        public EvaluatorExplicit.Builder setExpressionToType(
                ExpressionToType expressionToType) {
            return this;
        }
    }

    public final static String IDENTIFIER = "parameter-literal";
    private final Expression[] variables;
    private final Expression expression;
    private final ValueFunction value;

    private EvaluatorExplicitIdentifierParameter(Builder builder) {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = builder.getExpression();
        variables = builder.getVariables();
        value = TypeFunction.get().newValue();
        value.setParameter(ExpressionIdentifierStandard.as(expression).getName());
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
    }
    
    @Override
    public void evaluate() {
        assert expression != null;
        assert variables != null;
    }

    @Override
    public Value getResultValue() {
        return value;
    }
}
