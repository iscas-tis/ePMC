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
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionOperator;
import epmc.expressionevaluator.ExpressionToType;
import epmc.operator.OperatorOr;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;

public final class EvaluatorExplicitOperatorShortcutOr implements EvaluatorExplicitBoolean {
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
            if (!expressionOperator.getOperator().equals(OperatorOr.OR)) {
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
            return new EvaluatorExplicitOperatorShortcutOr(this);
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

    public final static String IDENTIFIER = "operator-shortcut-or";

    private final Expression[] variables;
    private final ExpressionOperator expression;
    private final EvaluatorExplicitBoolean[] operands;
    private final Value[] operandValues;
    private final ValueBoolean result;
    private Value[] values;

    private boolean booleanResult;

    public EvaluatorExplicitOperatorShortcutOr(Builder builder) {
        assert builder != null;
        assert builder.getExpression() != null;
        assert builder.getVariables() != null;
        expression = (ExpressionOperator) builder.getExpression();
        variables = builder.getVariables();
        operands = new EvaluatorExplicitBoolean[expression.getOperands().size()];
        operandValues = new Value[expression.getOperands().size()];
        Type[] types = new Type[expression.getOperands().size()];
        int opNr = 0;
        for (Expression operand : expression.getOperands()) {
            operands[opNr] = (EvaluatorExplicitBoolean) UtilEvaluatorExplicit.newEvaluator(null, operand, variables, builder.getCache(), builder.getExpressionToType());
            operandValues[opNr] = operands[opNr].getResultValue();
            types[opNr] = operands[opNr].getResultValue().getType();
            opNr++;
        }
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
    public void setValues(Value... values) {
        this.values = values;
        operands[0].setValues(values);
        operands[1].setValues(values);
    }
    
    @Override
    public void evaluate() {
        assert UtilEvaluatorExplicit.assertValues(values);
        if (operands[0].evaluateBoolean()) {
            result.set(true);
        } else {
            result.set(operands[1].evaluateBoolean());
        }
    }

    @Override
    public boolean evaluateBoolean() {
        assert UtilEvaluatorExplicit.assertValues(values);
        if (operands[0].evaluateBoolean()) {
            booleanResult = true;
            return true;
        } else {
            booleanResult = operands[1].evaluateBoolean();
            return booleanResult;
        }
    }

    @Override
    public Value getResultValue() {
        return result;
    }
}
