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
    
	private UtilPCTL() {
	}
}
