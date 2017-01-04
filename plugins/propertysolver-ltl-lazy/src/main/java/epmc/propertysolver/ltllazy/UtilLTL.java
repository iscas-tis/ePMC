package epmc.propertysolver.ltllazy;

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
