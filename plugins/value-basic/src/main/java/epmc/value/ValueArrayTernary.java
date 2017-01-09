package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueArray;

final class ValueArrayTernary extends ValueArray {
    private static final int LOG2LONGSIZE = 6;
	private final TypeArrayTernary type;
    private long[] content;
	private boolean immutable;

    ValueArrayTernary(TypeArrayTernary type) {
    	assert type != null;
    	this.type = type;
        int numBits = getTotalSize() * getBitsPerEntry();
        int numLongs = ((numBits - 1) >> LOG2LONGSIZE) + 1;
        this.content = new long[numLongs];
    }
    
    @Override
    public ValueArray clone() {
    	ValueArrayTernary other = new ValueArrayTernary(getType());
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
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert getType().getEntryType().canImport(value.getType());
        assert index >= 0;
        assert index < getTotalSize();
        ValueTernary valueTernary = (ValueTernary) value;
        int number = valueTernary.getTernary().ordinal();
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
        assert value.getType().canImport(getType().getEntryType());
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
        ValueTernary.asTernary(value).set(Ternary.values()[number]);
    }
    
    @Override
    public TypeArrayTernary getType() {
    	return type;
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        hash = Arrays.hashCode(content) + (hash << 6) + (hash << 16) - hash;
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
