package epmc.time;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

public final class ValueClock implements ValueAlgebra {
	private boolean immutable;
	private TypeClock type;
	private int value;

	ValueClock(TypeClock type) {
		assert false;
		this.type = type;
	}
	
	@Override
	public ValueClock clone() {
		ValueClock clone = type.newValue();
		clone.set(value);
		return clone;
	}

	@Override
	public TypeClock getType() {
		return type;
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
	public void set(int value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "valueClock(" + value + ")";
	}

	@Override
	public void add(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void divide(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subtract(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void multiply(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addInverse(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOne() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPosInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void set(Value value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double norm() throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double distance(Value other) throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
