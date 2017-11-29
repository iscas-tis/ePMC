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

import java.util.ArrayList;
import java.util.List;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionTypeBoolean;
import epmc.expression.standard.ExpressionTypeInteger;
import epmc.expression.standard.ExpressionTypeReal;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.expressionevaluator.ExpressionToType;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueDouble;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

public final class ExpressionSimplifierConstant implements ExpressionSimplifier {
    public final static String IDENTIFIER = "constant";

    @Override
    public Expression simplify(ExpressionToType expressionToType, Expression expression) {
        assert expression != null;
        /* literal expressions should be handled by other simplifier */
        if (ExpressionLiteral.isLiteral(expression)) {
            return null;
        }
        if (UtilExpressionStandard.collectIdentifiers(expression).size() == 0) {
            Value value = UtilEvaluatorExplicit.evaluate(expression, expressionToType);
            if (ValueInteger.is(value)) {
                ValueInteger valueInteger = ValueInteger.as(value);
                return new ExpressionLiteral.Builder()
                        .setValue(Integer.toString(valueInteger.getInt()))
                        .setType(ExpressionTypeInteger.TYPE_INTEGER)
                        .setPositional(expression.getPositional())
                        .build();
            }
            if (ValueBoolean.is(value)) {
                ValueBoolean valueBoolean = ValueBoolean.as(value);
                return new ExpressionLiteral.Builder()
                        .setValue(Boolean.toString(valueBoolean.getBoolean()))
                        .setType(ExpressionTypeBoolean.TYPE_BOOLEAN)
                        .setPositional(expression.getPositional())
                        .build();                
            }
            if (ValueDouble.is(value)) {
                ValueDouble valueDouble = ValueDouble.as(value);
                return new ExpressionLiteral.Builder()
                        .setValue(Double.toString(valueDouble.getDouble()))
                        .setType(ExpressionTypeReal.TYPE_REAL)
                        .setPositional(expression.getPositional())
                        .build();
            }
            if (ValueReal.is(value)) {
                // TODO converting from value to string this way
                // might loose precision
                ValueReal valueReal = ValueReal.as(value);
                return new ExpressionLiteral.Builder()
                        .setValue(valueReal.toString())
                        .setType(ExpressionTypeReal.TYPE_REAL)
                        .setPositional(expression.getPositional())
                        .build();
            }
        }
        List<Expression> newChildren = new ArrayList<>();
        boolean simplified = false;
        for (Expression child : expression.getChildren()) {
            Expression childSimplified = simplify(expressionToType, child);
            if (childSimplified == null) {
                childSimplified = child;
            } else {
                simplified = true;
            }
            newChildren.add(childSimplified);
        }
        if (simplified) {
            return expression.replaceChildren(newChildren);
        } else {
            return null;
        }
    }
}
