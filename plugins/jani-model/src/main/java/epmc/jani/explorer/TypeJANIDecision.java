package epmc.jani.explorer;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;

public final class TypeJANIDecision implements Type {
	private final ExplorerJANI explorer;

	TypeJANIDecision(ExplorerJANI explorer) {
		assert explorer != null;
		this.explorer = explorer;
		StateVariables stateVariables = explorer.getStateVariables();
		for (Expression expression : stateVariables.getIdentifiersArray()) {
			ExpressionIdentifier identifier = (ExpressionIdentifier) expression;
			Type type = stateVariables.getType(identifier);
		}
	}
	
	@Override
	public ContextValue getContext() {
		return explorer.getContextValue();
	}

	@Override
	public ValueJANIDecision newValue() {
		return new ValueJANIDecision(this);
	}

	@Override
	public boolean canImport(Type type) {
		assert type != null;
		if (!(type instanceof TypeJANIDecision)) {
			return false;
		}
		TypeJANIDecision other = (TypeJANIDecision) type;
		if (explorer != other.explorer) {
			return false;
		}
		return true;
	}

	@Override
	public TypeArray getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}

}