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

package epmc.expression.standard.simplify;

import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expressionevaluator.ExpressionToType;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorSubtract;

public final class ExpressionSimplifierSubtract implements ExpressionSimplifier {
    public final static class Builder implements ExpressionSimplifier.Builder {
        private ExpressionToType expressionToType;

        @Override
        public Builder setExpressionToType(ExpressionToType expressionToType) {
            this.expressionToType = expressionToType;
            return this;
        }

        @Override
        public Builder setEvaluatorCache(
                EvaluatorCache cache) {
            return this;
        }

        @Override
        public ExpressionSimplifier build() {
            return new ExpressionSimplifierSubtract(this);
        }

        @Override
        public Builder setSimplifier(
                ContextExpressionSimplifier simplifier) {
            return this;
        }
    }

    public final static String IDENTIFIER = "subtract";

    private ExpressionSimplifierSubtract(Builder builder) {
        assert builder != null;
        assert builder.expressionToType != null;
    }

    @Override
    public Expression simplify(Expression expression) {
        assert expression != null;
        if (!isSubtract(expression)) {
            return null;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        if (isZero(expressionOperator.getOperand1())) {
            return new ExpressionOperator.Builder()
                    .setOperator(OperatorAddInverse.ADD_INVERSE)
                    .setOperands(expressionOperator.getOperand2())
                    .setPositional(expression.getPositional())
                    .build();
        }
        if (isZero(expressionOperator.getOperand2())) {
            return new ExpressionOperator.Builder()
                    .setOperator(OperatorAddInverse.ADD_INVERSE)
                    .setOperands(expressionOperator.getOperand1())
                    .setPositional(expression.getPositional())
                    .build();
        }
        if (expressionOperator.getOperand1().equals(expressionOperator.getOperand2())) {
            return new ExpressionLiteral.Builder()
                    .setValue("0")
                    .setType(ExpressionTypeInteger.TYPE_INTEGER)
                    .setPositional(expression.getPositional())
                    .build();
        }
        return null;
    }

    private static boolean isSubtract(Expression expression) {
        if (!ExpressionOperator.is(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorSubtract.SUBTRACT);
    }

    private boolean isZero(Expression expression) {
        assert expression != null;
        ValueBoolean cmp = TypeBoolean.get().newValue();
        if (ExpressionLiteral.is(expression)) {
            Value value = UtilEvaluatorExplicit.evaluate(expression);
            OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, value.getType());
            isZero.apply(cmp, value);
        }
        return ExpressionLiteral.is(expression) && cmp.getBoolean();
    }
}
