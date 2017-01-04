package epmc.param.value;

import epmc.error.EPMCException;
import epmc.value.Operator;
import epmc.value.OperatorAdd;
import epmc.value.OperatorAddInverse;
import epmc.value.OperatorDivide;
import epmc.value.OperatorMax;
import epmc.value.OperatorMin;
import epmc.value.OperatorMultiply;
import epmc.value.OperatorMultiplyInverse;
import epmc.value.OperatorSubtract;
import epmc.value.Value;
import epmc.value.ValueReal;

final class ValueFunctionDAG implements ValueFunction {
    private int entry;
    private TypeFunctionDAG type;
    private boolean immutable;
    
    ValueFunctionDAG(TypeFunctionDAG type, int entry) {
        this.type = type;
        this.entry = entry;
    }
    
    @Override
    public TypeFunctionDAG getType() {
        return type;
    }

    @Override
    public ValueFunction clone() {
        return new ValueFunctionDAG(getType(), entry);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueFunctionDAG)) {
            return false;
        }
        ValueFunctionDAG other = (ValueFunctionDAG) obj;
        return entry == other.entry;
    }

    @Override
    public int hashCode() {
        return entry;
    }

    @Override
    public String toString() {
        return getType().entryToString(entry);
    }

    private Value applyOp(Operator operator, Value... operands) throws EPMCException {
        assert operator != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
            assert operand instanceof ValueFunctionDAG;
        }
        int[] entries = new int[operands.length];
        for (int i = 0; i < operands.length; i++) {
            entries[i] = ((ValueFunctionDAG) operands[i]).entry;
        }
        this.entry = getType().apply(operator, entries);
        return this;
    }

    @Override
    public void add(Value operand1, Value operand2) throws EPMCException {
        applyOp(getType().getContext().getOperator(OperatorAdd.IDENTIFIER), operand1, operand2);
    }
    
    @Override
    public void subtract(Value operand1, Value operand2)
            throws EPMCException {
        applyOp(getType().getContext().getOperator(OperatorSubtract.IDENTIFIER), operand1, operand2);
    }
    
    @Override
    public void multiply(Value operand1, Value operand2)
            throws EPMCException {
        applyOp(getType().getContext().getOperator(OperatorMultiply.IDENTIFIER), operand1, operand2);
    }
    
    @Override
    public void divide(Value operand1, Value operand2) throws EPMCException {
        applyOp(getType().getContext().getOperator(OperatorDivide.IDENTIFIER), operand1, operand2);
    }
    
    @Override
    public void addInverse(Value operand) throws EPMCException {
        applyOp(getType().getContext().getOperator(OperatorAddInverse.IDENTIFIER), operand);
    }
    
    @Override
    public void multInverse(Value operand) throws EPMCException {
        applyOp(getType().getContext().getOperator(OperatorMultiplyInverse.IDENTIFIER), operand);
    }
    
    @Override
    public void max(Value operand1, Value operand2) throws EPMCException {
        applyOp(getType().getContext().getOperator(OperatorMax.IDENTIFIER), operand1, operand2);
    }
    
    @Override
    public void min(Value operand1, Value operand2) throws EPMCException {
        applyOp(getType().getContext().getOperator(OperatorMin.IDENTIFIER), operand1, operand2);
    }
    
    @Override
    public boolean isEq(Value other) throws EPMCException {
        assert other != null;
        assert other instanceof ValueFunctionDAG;
        ValueFunctionDAG function = (ValueFunctionDAG) other;
        return this.entry == function.entry;
    }
    
    @Override
    public void set(Value value) {
        assert value != null;
        assert value instanceof ValueFunctionDAG;
        ValueFunctionDAG function = (ValueFunctionDAG) value;
        this.entry = function.entry;
    }
    
    @Override
    public void set(int value) {
        this.entry = getType().newEntry(value);
    }
    
    @Override
    public boolean isOne() {
        return this.entry == getType().getOneEntry();
    }
    
    @Override
    public boolean isZero() {
        return this.entry == getType().getZeroEntry();
    }
    
    @Override
    public boolean isPosInf() {
        return this.entry == getType().getPosInfEntry();
    }

    @Override
    public int compareTo(Value other) {
        assert other != null;
        assert other instanceof ValueFunctionDAG;
        ValueFunctionDAG function = (ValueFunctionDAG) other;
        return Integer.compare(entry, function.entry);
    }
    
    public int getFEntry() {
        return entry;
    }

    void setEntry(int entry) {
        assert entry >= 0;
        this.entry = entry;
    }

    @Override
    public void evaluate(ValueReal result, Point point) {
        assert result != null;
        assert point != null;
        getType().evaluate(result, entry, point);
    }

    @Override
    public void setImmutable() {
        this.immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return this.immutable;
    }

	@Override
	public void setParameter(Object parameter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConstant() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ValueReal getConstant() {
		return null;
		// TODO Auto-generated method stub
		
	}

	@Override
	public ValueFunction castOrImport(Value operand, int number) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
