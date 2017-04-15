package epmc.jani.explorer;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.ValueArray;

public final class TypeArrayJANIDecisionType implements TypeArray {
	private final TypeJANIDecision entryType;

	TypeArrayJANIDecisionType(TypeJANIDecision typeJANIDecision) {
		this.entryType = typeJANIDecision;
	}
	
	@Override
	public ContextValue getContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TypeArray getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getEntryType() {
		return entryType;
	}

	@Override
	public ValueArray newValue() {
		// TODO Auto-generated method stub
		return null;
	}

}
