package epmc.automaton;

import java.util.HashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeBoolean;

final class ExpressionToTypeBoolean implements ExpressionToType {
	private final Set<Expression> mapped = new HashSet<>();
	private final ContextValue contextValue;

	ExpressionToTypeBoolean(ContextValue contextValue, Expression[] expressions) {
		this.contextValue = contextValue;
		Set<Expression> seen = new HashSet<>();
		assert expressions != null;
		for (Expression expression : expressions) {
			assert expression != null;
			assert !seen.contains(expression);
			seen.add(expression);
		}
		for (Expression expression : expressions) {
			mapped.add(expression);
		}
	}
	
	@Override
	public Type getType(Expression expression) throws EPMCException {
		assert expression != null;
		if (mapped.contains(expression)) {
			return TypeBoolean.get(contextValue);
		}
		return null;
	}

	@Override
	public ContextValue getContextValue() {
		return contextValue;
	}

}
