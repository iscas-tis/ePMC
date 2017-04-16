package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class ValueArrayJANIDecision extends ValueArray {
	private final TypeArrayJANIDecisionType type;
	private boolean immutable;

	public ValueArrayJANIDecision(TypeArrayJANIDecisionType type) {
		assert type != null;
		this.type = type;
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub		
	}

	@Override
	public void setImmutable() {
		this.immutable = true;
	}

	@Override
	public boolean isImmutable() {
		return immutable;
	}

	@Override
	public TypeArray getType() {
		return type;
	}

	@Override
	protected void setDimensionsContent() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void get(Value presStateProb, int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void set(Value value, int index) {
		// TODO Auto-generated method stub
		
	}

}
