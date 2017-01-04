package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueArray;

final class ValueArrayEnum extends ValueArray {
    private static final int LOG2LONGSIZE = 6;
    private long[] content;
	private final TypeArrayEnum type;
	private boolean immutable;

    ValueArrayEnum(TypeArrayEnum type) {
    	assert type != null;
    	this.type = type;
        this.content = new long[0];
    }
    
    @Override
    public ValueArrayEnum clone() {
    	ValueArrayEnum other = new ValueArrayEnum(getType());
    	other.set(this);
    	return other;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        int numBits = getTotalSize() * getBitsPerEntry();
        int size = ((numBits - 1) >> LOG2LONGSIZE) + 1;
        this.content = new long[size];
    }

    private int getBitsPerEntry() {
        return getType().getEntryType().getNumBits();
    }
    
    private Enum<?>[] getConstants() {
        return ((TypeArrayEnum) getType()).getConstants();
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert ValueEnum.isEnum(value);
        assert getType().getEntryType().getEnumClass() == ValueEnum.asEnum(value).getEnumClass();
        assert index >= 0;
        assert index < getTotalSize();
        int number = ValueEnum.asEnum(value).getEnum().ordinal();
        for (int bitNr = 0; bitNr < getBitsPerEntry(); bitNr++) {
            boolean bitValue = (number & (1 << bitNr)) != 0;
            int bitIndex = index * getBitsPerEntry() + bitNr;
            int offset = bitIndex >> LOG2LONGSIZE;
            if (bitValue) {
                content[offset] |= 1L << bitIndex;
            } else {
                content[offset] &= ~(1L << bitIndex);
            }
        }
    }
    
    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert ValueEnum.isEnum(value);
        assert getType().getEntryType().getEnumClass() == ValueEnum.asEnum(value).getEnumClass();
        assert index >= 0;
        assert index < getTotalSize();
        int number = 0;
        for (int bitNr = 0; bitNr < getBitsPerEntry(); bitNr++) {
            int bitIndex = index * getBitsPerEntry() + bitNr;
            int offset = bitIndex >> LOG2LONGSIZE;
            boolean bitValue = (content[offset] & (1L << bitIndex)) != 0;
            if (bitValue) {
                number |= (1 << bitNr);
            }
        }
        ValueEnum.asEnum(value).set(getConstants()[number]);
    }
    
    @Override
    public TypeArrayEnum getType() {
    	return type;
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        hash = content.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
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
