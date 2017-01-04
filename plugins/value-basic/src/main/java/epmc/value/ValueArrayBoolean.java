package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueArray;

final class ValueArrayBoolean extends ValueArray {
    private static final int LOG2LONGSIZE = 6;
	private final TypeArrayBoolean type;
    private long[] content;
	private boolean immutable;
    
    ValueArrayBoolean(TypeArrayBoolean type) {
    	this.type = type;
        int numLongs = ((getTotalSize() - 1) >> LOG2LONGSIZE) + 1;
        this.content = new long[numLongs];
    }
    
    @Override
    public ValueArray clone() {
    	ValueArrayBoolean other = new ValueArrayBoolean(getType());
    	other.set(this);
    	return other;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        int size = ((getTotalSize() - 1) >> LOG2LONGSIZE) + 1;
        this.content = new long[size];
    }

     @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert ValueBoolean.isBoolean(value);
        assert index >= 0;
        assert index < getTotalSize();
        int offset = index >> LOG2LONGSIZE;
        if (ValueBoolean.asBoolean(value).getBoolean()) {
            content[offset] |= 1L << index;
        } else {
            content[offset] &= ~(1L << index);
        }
    }
    
    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert ValueBoolean.isBoolean(value);
        assert index >= 0;
        assert index < getTotalSize();
        int offset = index >> 6;
        ValueBoolean.asBoolean(value).set((content[offset] & (1L << index)) != 0);
    }    
    
    public int nextSetBit(int index) {
        assert index >= 0;
        assert index < getTotalSize();
        int offset = index >> LOG2LONGSIZE;
        long mask = 1L << index;
        while (offset < content.length) {
            long currentWord = content[offset];
            do {
                if ((currentWord & mask) != 0) {
                    return index;
                }
                mask <<= 1;
                index++;
            } while (mask != 0);
            mask = 1L;
            offset++;
        }
        return -1;
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        hash = Arrays.hashCode(content) + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public TypeArrayBoolean getType() {
    	return type;
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
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
    
}
