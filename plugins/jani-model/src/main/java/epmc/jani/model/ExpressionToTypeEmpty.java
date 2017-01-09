package epmc.jani.model;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.ExpressionToType;
import epmc.value.ContextValue;
import epmc.value.Type;

public final class ExpressionToTypeEmpty implements ExpressionToType {
	private final ContextValue contextValue;
	
	public ExpressionToTypeEmpty(ContextValue contextValue) {
		assert contextValue != null;
		this.contextValue = contextValue;
	}
	
	@Override
	public Type getType(Expression expression) throws EPMCException {
		assert expression != null;
		return null;
	}

	@Override
	public ContextValue getContextValue() {
		return contextValue;
	}
	
}
