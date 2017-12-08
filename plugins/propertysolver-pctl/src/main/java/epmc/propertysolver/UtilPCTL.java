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

package epmc.propertysolver;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionTemporal;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalNext;

public final class UtilPCTL {
    public static Set<Expression> collectPCTLInner(Expression expression) {
        if (expression instanceof ExpressionTemporal) {
            ExpressionTemporal expressionTemporal = ExpressionTemporal.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            for (Expression inner : expressionTemporal.getOperands()) {
                result.addAll(collectPCTLInner(inner));
            }
            return result;
        } else if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            return collectPCTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            return collectPCTLInner(expressionTemporal.getOperand());
        } else {
            return Collections.singleton(expression);			
        }
    }

    public static boolean isPCTLPath(Expression pathProp) {
        if (ExpressionTemporal.is(pathProp)) {
            ExpressionTemporal temporal = ExpressionTemporal.as(pathProp);
            for (Expression operand : temporal.getOperands()) {
                if (!ExpressionPropositional.is(operand)) {
                    return false;
                }
            }
            return true;
        } else if (ExpressionTemporalNext.is(pathProp)) {
            ExpressionTemporalNext next = ExpressionTemporalNext.as(pathProp);
            return ExpressionPropositional.is(next.getOperand());
        } else if (ExpressionTemporalFinally.is(pathProp)) {
            ExpressionTemporalFinally expFinally = ExpressionTemporalFinally.as(pathProp);
            return ExpressionPropositional.is(expFinally.getOperand());
        } else {
            return false;
        }
    }

    public static boolean isPCTLPathUntil(Expression pathProp) {
        if (!isPCTLPath(pathProp)) {
            return false;
        }
        if (ExpressionTemporalFinally.is(pathProp)) {
            return true;
        }
        if (!ExpressionTemporal.is(pathProp)) {
            return false;
        }
        ExpressionTemporal asTemporal = (ExpressionTemporal) pathProp;
        switch (asTemporal.getTemporalType()) {
        case GLOBALLY:
        case RELEASE:
        case UNTIL:
            break;
        default:
            return false;

        }
        return true;
    }

    private UtilPCTL() {
    }
}
