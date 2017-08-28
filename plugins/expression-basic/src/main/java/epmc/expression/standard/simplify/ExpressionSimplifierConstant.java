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
import epmc.expression.ExpressionToType;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.UtilExpressionStandard;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.value.Value;

public final class ExpressionSimplifierConstant implements ExpressionSimplifier {
    public final static String IDENTIFIER = "constant";

    @Override
    public Expression simplify(ExpressionToType expressionToType, Expression expression) {
        assert expression != null;
        if (expression instanceof ExpressionLiteral) {
            return null;
        }
        if (UtilExpressionStandard.collectIdentifiers(expression).size() == 0) {
            return new ExpressionLiteral.Builder()
                    .setValue(evaluateValue(expressionToType, expression))
                    .build();
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

    private static Value evaluateValue(ExpressionToType expressionToType, Expression expression) {
        assert expression != null;
        EvaluatorExplicit evaluator = UtilEvaluatorExplicit.newEvaluator(expression, expressionToType, new Expression[0]);
        return evaluator.evaluate();
    }
}
