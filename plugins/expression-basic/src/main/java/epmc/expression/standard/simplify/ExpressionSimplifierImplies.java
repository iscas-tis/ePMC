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

import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorCache;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expressionevaluator.ExpressionToType;
import epmc.operator.OperatorImplies;
import epmc.operator.OperatorOr;

public final class ExpressionSimplifierImplies implements ExpressionSimplifier {
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
            return new ExpressionSimplifierImplies(this);
        }
    }

    public final static String IDENTIFIER = "implies";
    private ContextExpressionSimplifier simplifier;

    private ExpressionSimplifierImplies(Builder builder) {
        assert builder != null;
        assert builder.expressionToType != null;
        this.simplifier = builder.simplifier;
    }

    @Override
    public Expression simplify(Expression expression) {
        assert expression != null;
        if (!isImplies(expression)) {
            return null;
        }
        ExpressionOperator expressionOperator = ExpressionOperator.as(expression);
        return simplifier.simplify(new ExpressionOperator.Builder()
                .setOperator(OperatorOr.OR)
                .setOperands(
                        UtilExpressionStandard.opNot(expressionOperator.getOperand1()),
                        expressionOperator.getOperand2())
                .setPositional(expression.getPositional())
                .build());
    }

    private static boolean isImplies(Expression expression) {
        if (!ExpressionOperator.is(expression)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorImplies.IMPLIES);
    }
}
