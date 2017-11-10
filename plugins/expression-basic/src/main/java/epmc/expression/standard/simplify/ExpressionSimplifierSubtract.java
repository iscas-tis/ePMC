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
import epmc.value.TypeInterval;
import epmc.value.ValueBoolean;
import epmc.value.operator.OperatorIsZero;
import epmc.value.operator.OperatorSubtract;
import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expressionevaluator.ExpressionToType;


public final class ExpressionSimplifierSubtract implements ExpressionSimplifier {
    public final static String IDENTIFIER = "subtract";

    @Override
    public Expression simplify(ExpressionToType expressionToType, Expression expression) {
        assert expression != null;
        if (!isSubtract(expression)) {
            return null;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        if (isZero(expressionOperator.getOperand1())) {
            return UtilExpressionStandard.opAddInverse(expressionOperator.getOperand2());
        }
        if (isZero(expressionOperator.getOperand2())) {
            return expressionOperator.getOperand1();
        }
        if (expressionOperator.getOperand1().equals(expressionOperator.getOperand2())) {
            return new ExpressionLiteral.Builder()
                    .setValue(TypeInterval.get().getZero())
                    .build();
        }
        return null;
    }

    private static boolean isSubtract(Expression expression) {
        if (!(expression instanceof ExpressionOperator)) {
            return false;
        }
        ExpressionOperator expressionOperator = (ExpressionOperator) expression;
        return expressionOperator.getOperator()
                .equals(OperatorSubtract.SUBTRACT);
    }

    private boolean isZero(Expression expression) {
        assert expression != null;
        ValueBoolean cmp = TypeBoolean.get().newValue();
        if (ExpressionLiteral.isLiteral(expression)) {
            OperatorEvaluator isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, ExpressionLiteral.asLiteral(expression).getValue().getType());
            isZero.apply(cmp, ExpressionLiteral.asLiteral(expression).getValue());
        }
        return ExpressionLiteral.isLiteral(expression) && cmp.getBoolean();
    }
}
