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

import epmc.error.Positional;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionTypeBoolean;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expressionevaluator.ExpressionToType;
import epmc.operator.OperatorNot;
import epmc.operator.OperatorOr;
import epmc.value.ValueBoolean;

public final class ExpressionSimplifierOr implements ExpressionSimplifier {
    public final static class Builder implements ExpressionSimplifier.Builder {
        private ExpressionToType expressionToType;
        private ContextExpressionSimplifier simplifier;

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
        public Builder setSimplifier(
                ContextExpressionSimplifier simplifier) {
            this.simplifier = simplifier;
            return this;
        }

        @Override
        public ExpressionSimplifier build() {
            return new ExpressionSimplifierOr(this);
        }
    }

    public final static String IDENTIFIER = "or";
    private ContextExpressionSimplifier simplifier;

    private ExpressionSimplifierOr(Builder builder) {
        assert builder != null;
        assert builder.expressionToType != null;
        this.simplifier = builder.simplifier;
    }

    @Override
    public Expression simplify(Expression expression) {
        assert expression != null;
        if (!isOr(expression)) {
            return null;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
        if (isFalse(expressionOperator.getOperand1())) {
            return expressionOperator.getOperand2().replacePositional(expression.getPositional());
        }
        if (isFalse(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1().replacePositional(expression.getPositional());
        }
        if (isTrue(expressionOperator.getOperand1())) {
            return getTrue(expression.getPositional());
        }
        if (isTrue(expressionOperator.getOperand2())) {
            return getTrue(expression.getPositional());
        }
        if (expressionOperator.getOperand1().equals(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1().replacePositional(expression.getPositional());
        }
        if (isNot(expressionOperator.getOperand1())
                && ((ExpressionOperator) expressionOperator.getOperand1()).getOperand1()
                .equals(expressionOperator.getOperand2())) {
            return getTrue(expression.getPositional());
        }
         if (isNot(expressionOperator.getOperand2())
                && ((ExpressionOperator) expressionOperator.getOperand2()).getOperand1()
                .equals(expressionOperator.getOperand1())) {
            return getTrue(expression.getPositional());
        }
        Expression left = simplifier.simplify(expressionOperator.getOperand1());
        Expression right = simplifier.simplify(expressionOperator.getOperand2());
        if (left != null && right != null) {
            return new ExpressionOperator.Builder()
                    .setOperator(OperatorOr.OR)
                    .setOperands(left, right)
                    .setPositional(expression.getPositional())
                    .build();
        }
        if (left != null) {
            return new ExpressionOperator.Builder()
                    .setOperator(OperatorOr.OR)
                    .setOperands(left, expressionOperator.getOperand2())
                    .setPositional(expression.getPositional())
                    .build();
        }
        if (right != null) {
            return new ExpressionOperator.Builder()
                    .setOperator(OperatorOr.OR)
                    .setOperands(expressionOperator.getOperand1(), right)
                    .setPositional(expression.getPositional())
                    .build();
        }
        return null;
    }

    private static boolean isNot(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorNot.NOT);
    }

    private static boolean isOr(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorOr.OR);
    }

    private static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        return ValueBoolean.isFalse(UtilEvaluatorExplicit.evaluate(expression));
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        return ValueBoolean.isTrue(UtilEvaluatorExplicit.evaluate(expression));
    }
    
    
    private final Expression getTrue(Positional positional) {
        return new ExpressionLiteral.Builder()
                .setType(ExpressionTypeBoolean.TYPE_BOOLEAN)
                .setValue("true")
                .setPositional(positional)
                .build();
    }
}
