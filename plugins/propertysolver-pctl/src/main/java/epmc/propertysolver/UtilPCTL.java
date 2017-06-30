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

import static epmc.expression.standard.ExpressionPropositional.isPropositional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionTemporal;

public final class UtilPCTL {
	public static Set<Expression> collectPCTLInner(Expression expression) {
		if (expression instanceof ExpressionTemporal) {
			ExpressionTemporal expressionTemporal = (ExpressionTemporal) expression;
			Set<Expression> result = new LinkedHashSet<>();
			for (Expression inner : expressionTemporal.getOperands()) {
				result.addAll(collectPCTLInner(inner));
			}
			return result;
		} else {
			return Collections.singleton(expression);			
		}
	}
	
    public static boolean isPCTLPath(Expression pathProp) {
        if (!(pathProp instanceof ExpressionTemporal)) {
            return false;
        }
        ExpressionTemporal asQuantifier = (ExpressionTemporal) pathProp;
        for (Expression operand : asQuantifier.getOperands()) {
            if (!isPropositional(operand)) {
                return false;
            }
        }
        
        return true;
    }

    public static boolean isPCTLPathUntil(Expression pathProp) {
    	if (!isPCTLPath(pathProp)) {
    		return false;
    	}
        ExpressionTemporal asTemporal = (ExpressionTemporal) pathProp;
        switch (asTemporal.getTemporalType()) {
		case FINALLY:
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
