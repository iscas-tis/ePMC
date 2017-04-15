package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.Value;

public final class ValueJANIDecision implements Value {
	private boolean immutable;

	ValueJANIDecision(TypeJANIDecision janiDecisionType) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(Value o) {
		assert false;
		return 0;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(Value value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImmutable() {
		immutable = true;
	}

	@Override
	public boolean isImmutable() {
		return immutable;
	}

	@Override
	public double distance(Value other) throws EPMCException {
		assert other != null;
		assert other instanceof ValueJANIDecision;
		ValueJANIDecision otherDecision = (ValueJANIDecision) other;
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEq(Value other) throws EPMCException {
		assert other != null;
		assert other instanceof ValueJANIDecision;
		ValueJANIDecision otherDecision = (ValueJANIDecision) other;

		// TODO Auto-generated method stub
		return false;
	}

}
