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

package epmc.multiobjective;

import static epmc.expression.standard.ExpressionPropositional.isPropositional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionTemporal;

public final class UtilLTL {
    public static Set<Expression> collectLTLInner(Expression expression) {
        if (isPropositional(expression)) {
            return Collections.singleton(expression);
        } else if (expression instanceof ExpressionTemporal) {
            ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionTemporal.getOperands()) {
                result.addAll(collectLTLInner(inner));
            }
            return result;
        } else if (expression instanceof ExpressionOperator) {
            ExpressionOperator expressionOperator = (ExpressionOperator) expression;
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionOperator.getOperands()) {
                result.addAll(collectLTLInner(inner));
            }
            return result;
        } else {
            return Collections.singleton(expression);			
        }
    }

    private UtilLTL() {
    }
}
