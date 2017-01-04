package epmc.rddl.value;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.TypeEnumerable;
import epmc.value.Value;
import epmc.value.ValueEnumerable;

public class ValueRDDLEnum implements ValueEnumerable {
    private static final long serialVersionUID = 1L;
    private final TypeRDDLEnum type;
    private boolean immutable;
    private int number;
    
    ValueRDDLEnum(TypeRDDLEnum type) {
        assert type != null;
        this.type = type;
    }

    @Override
    public ValueRDDLEnum clone() {
        ValueRDDLEnum clone = new ValueRDDLEnum(type);
        clone.number = this.number;
        return clone;
    }

    @Override
    public TypeEnumerable getType() {
        return type;
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
    public void set(Value value) {
        assert value != null;
        assert value instanceof ValueRDDLEnum;
        ValueRDDLEnum other = (ValueRDDLEnum) value;
        this.number = other.number;
    }
    
    @Override
    public void set(String value) {
        assert value != null;
        this.number = type.getContextRDDL().getEnumConstantNumber(value);
    }
    
    @Override
    public String toString() {
        return type.getContextRDDL().getNumberToEnumConstant().get(this.number);
    }

    void setNumber(int number) {
    	assert number >= 0;
    	this.number = number;
    }
    
    public int getNumber() {
        return number;
    }
    
    public int getInternalNumber() {
        return type.getToInternalNumber()[number];
    }
    
    @Override
    public int getValueNumber() {
    	return this.number;
    }

    @Override
    public boolean isEq(Value other) throws EPMCException {
    	assert other != null;
    	if (!(other instanceof ValueRDDLEnum)) {
    		return false;
    	}
    	ValueRDDLEnum o = (ValueRDDLEnum) other;
    	return this.number == o.number;
    }

    @Override
    public int hashCode() {
    	return this.number;
    }
    
    @Override
    public boolean equals(Object obj) {
    	assert obj != null;
    	if (!(obj instanceof ValueRDDLEnum)) {
    		return false;
    	}
    	ValueRDDLEnum o = (ValueRDDLEnum) obj;
    	return this.number == o.number;
    }
    
    @Override
    public void setValueNumber(int number) {
    	setNumber(type.getToInternalNumber()[number]);
    }

	@Override
	public int compareTo(Value other) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double distance(Value other) throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}
}
