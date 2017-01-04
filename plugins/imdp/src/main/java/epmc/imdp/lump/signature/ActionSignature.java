package epmc.imdp.lump.signature;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.TypeInterval;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueInterval;

final class ActionSignature implements Cloneable {
	private final ContextValue contextValue;
    private int size;
	private int[] blocks;
    private ValueInterval[] values;
    
    ActionSignature(ContextValue contextValue) {
    	assert contextValue != null;
    	this.contextValue = contextValue;
    	blocks = new int[0];
    	values = new ValueInterval[0];
    }
    
    @Override
    protected ActionSignature clone() {
    	ActionSignature clone = new ActionSignature(getContextValue());
    	clone.size = this.size;
    	clone.blocks = new int[size];
    	clone.values = new ValueInterval[size];
    	for (int number = 0; number < size; number++) {
    		clone.blocks[number] = this.blocks[number];
    		clone.values[number] = UtilValue.clone(this.values[number]);
    	}
    	return clone;
    }
    
    void setSize(int size) {
    	assert size >= 0;
    	int oldSize = this.size;
    	if (size > oldSize) {
    		blocks = new int[size];
    		values = new ValueInterval[size];
    		for (int number = 0; number < size; number++) {
    			values[number] = getTypeInterval().newValue();
    		}
    	}
    	this.size = size;
    }
    
    int getSize() {
    	return size;
    }
    
    void setBlock(int number, int block) {
    	assert number >= 0;
    	assert number < size;
    	assert block >= 0;
    	blocks[number] = block;
    }
    
    int getBlock(int number) {
    	assert number >= 0;
    	assert number < size;
    	return blocks[number];
    }
    
    void setValue(int number, Value value) {
    	assert number >= 0;
    	assert number < size;
    	assert value != null;
    	assert TypeInterval.get(getContextValue()).canImport(value.getType());
    	values[number].set(value);
    }
    
    Value getValue(int number) {
    	assert number >= 0;
    	assert number < size;
    	return values[number];
    }
    
    private TypeInterval getTypeInterval() {
    	return TypeInterval.get(getContextValue());
    }
    
    private ContextValue getContextValue() {
    	return contextValue;
    }

	@Override
	public boolean equals(Object obj) {
		assert obj != null;
		if (!(obj instanceof ActionSignature)) {
			return false;
		}
		ActionSignature other = (ActionSignature) obj;
		if (this.size != other.size) {
			return false;
		}
		for (int number = 0; number < size; number++) {
			if (this.blocks[number] != other.blocks[number]) {
				return false;
			}
		}
		for (int number = 0; number < size; number++) {
			try {
				if (!this.values[number].isEq(other.values[number])) {
					return false;
				}
			} catch (EPMCException e) {
				throw new RuntimeException(e);
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = 0;
        hash = size + (hash << 6) + (hash << 16) - hash;
        for (int number = 0; number < size; number++) {
        	int block = blocks[number];
	        hash = block + (hash << 6) + (hash << 16) - hash;
        	ValueInterval entry = values[number];
			double valueLower = entry.getIntervalLower().getDouble();
			long roundedLower = Math.round(valueLower / 1E-6);
	        hash = Long.hashCode(roundedLower) + (hash << 6) + (hash << 16) - hash;
			double valueUpper = entry.getIntervalLower().getDouble();
			long roundedUpper = Math.round(valueUpper / 1E-6);
	        hash = Long.hashCode(roundedUpper) + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for (int number = 0; number < size; number++) {
			builder.append(blocks[number]);
			builder.append(":");
			builder.append(values[number]);
			builder.append(",");
		}
		builder.delete(builder.length() - 1, builder.length());
		builder.append("]");
		return builder.toString();
	}
}
