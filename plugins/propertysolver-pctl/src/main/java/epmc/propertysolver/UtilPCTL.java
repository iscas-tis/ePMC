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
import epmc.expression.standard.ExpressionOperator;
import epmc.expression.standard.ExpressionPropositional;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionTemporalFinally;
import epmc.expression.standard.ExpressionTemporalGlobally;
import epmc.expression.standard.ExpressionTemporalNext;
import epmc.expression.standard.ExpressionTemporalRelease;
import epmc.expression.standard.ExpressionTemporalUntil;

public final class UtilPCTL {
    public static Set<Expression> collectPCTLInner(Expression expression) {
        if (ExpressionTemporalNext.is(expression)) {
            ExpressionTemporalNext expressionTemporal = ExpressionTemporalNext.as(expression);
            return collectPCTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalFinally.is(expression)) {
            ExpressionTemporalFinally expressionTemporal = ExpressionTemporalFinally.as(expression);
            return collectPCTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalGlobally.is(expression)) {
            ExpressionTemporalGlobally expressionTemporal = ExpressionTemporalGlobally.as(expression);
            return collectPCTLInner(expressionTemporal.getOperand());
        } else if (ExpressionTemporalRelease.is(expression)) {
            ExpressionTemporalRelease expressionTemporal = ExpressionTemporalRelease.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectPCTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectPCTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else if (ExpressionTemporalUntil.is(expression)) {
            ExpressionTemporalUntil expressionTemporal = ExpressionTemporalUntil.as(expression);
            Set<Expression> result = new LinkedHashSet<>();
            result.addAll(collectPCTLInner(expressionTemporal.getOperandLeft()));
            result.addAll(collectPCTLInner(expressionTemporal.getOperandRight()));
            return result;
        } else {
            return Collections.singleton(expression);			
        }
    }

    public static boolean isPCTLState(Expression stateProp) {
        if (ExpressionPropositional.is(stateProp)) {
            return true;
        }
        if (ExpressionQuantifier.is(stateProp)) {
            return true;
        }
        if (ExpressionOperator.is(stateProp)) {
            ExpressionOperator asOperator = ExpressionOperator.as(stateProp);
            for (Expression operand : asOperator.getOperands()) {
                if (!isPCTLState(operand)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public static boolean isPCTLPath(Expression pathProp) {
        if (ExpressionTemporalNext.is(pathProp)) {
            ExpressionTemporalNext next = ExpressionTemporalNext.as(pathProp);
            return isPCTLState(next.getOperand());
        } else if (ExpressionTemporalFinally.is(pathProp)) {
            ExpressionTemporalFinally expFinally = ExpressionTemporalFinally.as(pathProp);
            return isPCTLState(expFinally.getOperand());
        } else if (ExpressionTemporalGlobally.is(pathProp)) {
            ExpressionTemporalGlobally expGlobally = ExpressionTemporalGlobally.as(pathProp);
            return isPCTLState(expGlobally.getOperand());
        } else if (ExpressionTemporalRelease.is(pathProp)) {
            ExpressionTemporalRelease expRelease = ExpressionTemporalRelease.as(pathProp);
            return isPCTLState(expRelease.getOperandLeft())
                    && isPCTLState(expRelease.getOperandRight());
        } else if (ExpressionTemporalUntil.is(pathProp)) {
            ExpressionTemporalUntil expRelease = ExpressionTemporalUntil.as(pathProp);
            return isPCTLState(expRelease.getOperandLeft())
                    && isPCTLState(expRelease.getOperandRight());
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
        if (ExpressionTemporalGlobally.is(pathProp)) {
            return true;
        }
        if (ExpressionTemporalRelease.is(pathProp)) {
            return true;
        }
        if (ExpressionTemporalUntil.is(pathProp)) {
            return true;
        }
        return false;
    }

    private UtilPCTL() {
    }
}
