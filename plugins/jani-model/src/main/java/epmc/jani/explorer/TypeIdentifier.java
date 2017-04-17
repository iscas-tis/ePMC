package epmc.jani.explorer;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;

public final class TypeIdentifier implements Type {
	private ExplorerJANI explorer;

	TypeIdentifier(ExplorerJANI explorer) {
		assert explorer != null;
		this.explorer = explorer;
	}
	
	@Override
	public ContextValue getContext() {
		return explorer.getContextValue();
	}

	@Override
	public Value newValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canImport(Type type) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TypeArray getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
