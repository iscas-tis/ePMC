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
import epmc.expressionevaluator.ExpressionToType;
import epmc.operator.OperatorAnd;
import epmc.operator.OperatorNot;

public final class ExpressionSimplifierAnd implements ExpressionSimplifier {
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
            return new ExpressionSimplifierAnd(this);
        }
    }
    
    public final static String IDENTIFIER = "and";
    private final ContextExpressionSimplifier simplifier;

    private ExpressionSimplifierAnd(Builder builder) {
        assert builder != null;
        assert builder.expressionToType != null;
        this.simplifier = builder.simplifier;
    }

    @Override
    public Expression simplify(Expression expression) {
        assert expression != null;
        if (!isAnd(expression)) {
            return null;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
        if (isFalse(expressionOperator.getOperand1())) {
            return getFalse(expressionOperator.getPositional());
        }
        if (isFalse(expressionOperator.getOperand2())) {
            return getFalse(expressionOperator.getPositional());
        }
        if (isTrue(expressionOperator.getOperand1())) {
            return expressionOperator.getOperand2().replacePositional(expressionOperator.getPositional());
        }
        if (isTrue(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1().replacePositional(expressionOperator.getPositional());
        }
        if (expressionOperator.getOperand1()
                .equals(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1().replacePositional(expressionOperator.getPositional());
        }
        if (isNot(expressionOperator.getOperand1())
                && (ExpressionOperator.as(expressionOperator.getOperand1()))
                .getOperand1()
                .equals(expressionOperator.getOperand2())) {
            return getFalse(expressionOperator.getPositional());
        }
        if (isNot(expressionOperator.getOperand2())
                && (ExpressionOperator.as(expressionOperator.getOperand2()).getOperand1())
                .equals(expressionOperator.getOperand1())) {
            return getFalse(expressionOperator.getPositional());
        }
        Expression left = simplifier.simplify(expressionOperator.getOperand1());
        Expression right = simplifier.simplify(expressionOperator.getOperand2());
        if (left != null && right != null) {
            return new ExpressionOperator.Builder()
                    .setOperator(OperatorAnd.AND)
                    .setOperands(left, right)
                    .setPositional(expressionOperator.getPositional())
                    .build();
        }
        if (left != null) {
            return new ExpressionOperator.Builder()
                    .setOperator(OperatorAnd.AND)
                    .setOperands(left, expressionOperator.getOperand2())
                    .setPositional(expressionOperator.getPositional())
                    .build();
        }
        if (right != null) {
            return new ExpressionOperator.Builder()
                    .setOperator(OperatorAnd.AND)
                    .setOperands(expressionOperator.getOperand1(), right)
                    .setPositional(expressionOperator.getPositional())
                    .build();
        }
        return null;
    }

    private static boolean isNot(Expression expression) {
        if (!ExpressionOperator.is(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorNot.NOT);
    }

    private static boolean isAnd(Expression expression) {
        if (!ExpressionOperator.is(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
        return expressionOperator.getOperator()
                .equals(OperatorAnd.AND);
    }

    private static boolean isFalse(Expression expression) {
        assert expression != null;
        if (!ExpressionLiteral.is(expression)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = ExpressionLiteral.as(expression);
        return !Boolean.valueOf(expressionLiteral.getValue());
    }

    private static boolean isTrue(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionLiteral)) {
            return false;
        }
        ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
        return Boolean.valueOf(expressionLiteral.getValue());
    }
    
    private final Expression getFalse(Positional positional) {
        return new ExpressionLiteral.Builder()
                .setType(ExpressionTypeBoolean.TYPE_BOOLEAN)
                .setValue("false")
                .setPositional(positional)
                .build();
    }
}
