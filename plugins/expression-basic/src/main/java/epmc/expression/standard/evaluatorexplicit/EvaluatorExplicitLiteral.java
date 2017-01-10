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
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionLiteral;
import epmc.value.Value;

public final class EvaluatorExplicitLiteral implements EvaluatorExplicit, EvaluatorExplicitBoolean{
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
            if (!(expression instanceof ExpressionLiteral)) {
                return false;
            }
            return true;
        }

        @Override
        public EvaluatorExplicit build() throws EPMCException {
            return new EvaluatorExplicitLiteral(this);
        }

		@Override
		public EvaluatorExplicit.Builder setExpressionToType(
				ExpressionToType expressionToType) {
			this.expressionToType = expressionToType;
			return this;
		}
    }
    
    public final static String IDENTIFIER = "literal";
    private final Expression[] variables;
    private final Expression expression;
    private final Value value;
    private final boolean booleanValue;

    private EvaluatorExplicitLiteral(Builder builder) {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        this.expression = builder.getExpression();
        this.variables = builder.getVariables();
        value = getValue(expression);
        if (ValueBoolean.isBoolean(value)) {
            booleanValue = ValueBoolean.asBoolean(value).getBoolean();
        } else {
            booleanValue = false;
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
    public Value evaluate(Value... values) throws EPMCException {
        assert expression != null;
        assert variables != null;
        return value;
    }
    
    @Override
    public boolean evaluateBoolean(Value... values) throws EPMCException {
        return booleanValue;
    }

    @Override
    public Value getResultValue() {
        return value;
    }

    private static Value getValue(Expression expression) {
        assert expression != null;
        assert expression instanceof ExpressionLiteral;
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return expressionLiteral.getValue();
    }

}
