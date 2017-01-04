package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueArray;

final class ValueArrayInterval extends ValueArrayAlgebra implements ValueContentDoubleArray {
	private final TypeArrayInterval type;
    private ValueArray content;
	private boolean immutable;

    ValueArrayInterval(TypeArrayInterval type) {
    	assert type != null;
    	this.type = type;
        this.content = UtilValue.newArray(type.getTypeArrayReal(), getTotalSize() * 2);
    }
    
    @Override
    public TypeArrayInterval getType() {
    	return type;
    }
    
    @Override
    public ValueArrayInterval clone() {
        ValueArrayInterval clone = (ValueArrayInterval) getType().newValue();
        clone.set(this);
        return clone;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.getTotalSize() < getTotalSize() * 2) {
            content = UtilValue.newArray(getType().getTypeArrayReal(), getTotalSize() * 2);
        }
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert ValueInterval.isInterval(value);
        assert index >= 0;
        assert index < getTotalSize() : index + " " + getTotalSize();
        content.set(ValueInterval.asInterval(value).getIntervalLower(), index * 2);
        content.set(ValueInterval.asInterval(value).getIntervalUpper(), index * 2 + 1);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert ValueInterval.isInterval(value);
        assert index >= 0;
        assert index < getTotalSize();
        content.get(ValueInterval.asInterval(value).getIntervalLower(), index * 2);
        content.get(ValueInterval.asInterval(value).getIntervalUpper(), index * 2 + 1);
    }
    
    @Override
    public double[] getDoubleArray() {
        return ValueContentDoubleArray.getContent(content);
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        hash = content.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
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
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
