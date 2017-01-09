package epmc.jani.measure;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.Type;
import epmc.value.Value;

public final class DiscreteMeasureValue implements DiscreteMeasure, Value {
	/** 1L, as I don't know any better */
	private static final long serialVersionUID = 1L;
	private Operator operator;
	private DiscreteMeasure[] operands;
	
	@Override
	public Type getEntryType() {
		return operator.resultType(getOperandsEntryTypes());
	}

	private Type[] getOperandsEntryTypes() {
		Type[] result = new Type[operands.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = operands[i].getEntryType();
		}
		return result;
	}
	
	@Override
	public void getTotal(Value total) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getFrom() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTo() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void getValue(Value value, int of) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Value clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setImmutable() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isImmutable() {
		// TODO Auto-generated method stub
		return false;
	}

	public void add(Value operand1, Value operand2) throws EPMCException {
		this.operator = getType().getContext().getOperator(OperatorAdd.IDENTIFIER);
		this.operands = new DiscreteMeasure[2];
		this.operands[0] = (DiscreteMeasure) operand1; // TODO castorimport
		this.operands[1] = (DiscreteMeasure) operand2;
	}

	@Override
	public void set(Value value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(Value other) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEq(Value other) throws EPMCException {
		// TODO Auto-generated method stub
		return false;
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