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

import epmc.value.OperatorAnd;
import epmc.value.ValueBoolean;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit.EvaluatorCacheEntry;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;

public final class EvaluatorExplicitOperatorShortcutAnd implements EvaluatorExplicitBoolean {
    public final static class Builder implements EvaluatorExplicit.Builder {
        private Expression[] variables;
        private Expression expression;
        private Map<EvaluatorCacheEntry, EvaluatorExplicit> cache;
		private ExpressionToType expressionType;

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
            if (!expressionOperator.getOperator().equals(OperatorAnd.AND)) {
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
        public EvaluatorExplicit build() throws EPMCException {
            return new EvaluatorExplicitOperatorShortcutAnd(this);
        }

		@Override
		public EvaluatorExplicit.Builder setExpressionToType(
				ExpressionToType expressionToType) {
			this.expressionType = expressionToType;
			return this;
		}
        
		private ExpressionToType getExpressionType() {
			return expressionType;
		}
		
    }
    public final static String IDENTIFIER = "operator-shortcut-and";
    
    private final Expression[] variables;
    private final ExpressionOperator expression;
    private final EvaluatorExplicitBoolean operandLeft;
    private final EvaluatorExplicitBoolean operandRight;
    private final ValueBoolean result;

    private EvaluatorExplicitOperatorShortcutAnd(Builder builder) throws EPMCException {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        variables = builder.getVariables();
        expression = (ExpressionOperator) builder.getExpression();
        operandLeft = (EvaluatorExplicitBoolean) UtilEvaluatorExplicit.newEvaluator(null, expression.getOperand1(), variables, builder.getCache(), builder.getExpressionType());
        operandRight = (EvaluatorExplicitBoolean) UtilEvaluatorExplicit.newEvaluator(null, expression.getOperand2(), variables, builder.getCache(), builder.getExpressionType());
        Type[] types = new Type[expression.getOperands().size()];
        types[0] = operandLeft.getResultValue().getType();
        types[1] = operandRight.getResultValue().getType();
        result = TypeBoolean.get().newValue();
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
        if (!operandLeft.evaluateBoolean(values)) {
            result.set(false);
        } else {
            result.set(operandRight.evaluateBoolean(values));
        }
        return result;
    }
    
    @Override
    public boolean evaluateBoolean(Value... values)
            throws EPMCException {
        for (Value variable : values) {
            assert variable != null;
        }
        if (!operandLeft.evaluateBoolean(values)) {
            return false;
        } else {
            return operandRight.evaluateBoolean(values);
        }
    }
    
    @Override
    public Value getResultValue() {
        return result;
    }
}
