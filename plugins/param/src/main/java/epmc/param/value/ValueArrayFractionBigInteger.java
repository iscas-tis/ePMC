package epmc.param.value;

import java.math.BigInteger;
import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInteger;

public class ValueArrayFractionBigInteger extends ValueArrayAlgebra {
	private final TypeArrayFractionBigInteger type;
	private BigInteger[] content;
	private boolean immutable;

	ValueArrayFractionBigInteger(TypeArrayFractionBigInteger type) {
        this.type = type;
        this.content = new BigInteger[getTotalSize() * 2];
        for (int entryNr = 0; entryNr < getTotalSize() * 2; entryNr++) {
        	this.content[entryNr] = BigInteger.ZERO;
        }
    }

	@Override
	public ValueArrayFractionBigInteger clone() {
		ValueArrayFractionBigInteger clone = getType().newValue();
		clone.set(this);
		return clone;
	}

	@Override
	protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.length * 2 < getTotalSize()) {
            content = new BigInteger[getTotalSize() * 2];
        }
	}

    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert getType().getEntryType().canImport(value.getType());
        assert index >= 0;
        assert index < getTotalSize();
        if (value instanceof ValueFractionBigInteger) {
        	ValueFractionBigInteger fraction = (ValueFractionBigInteger) value;
        	this.content[index * 2 + 0] = fraction.getNumerator();
        	this.content[index * 2 + 1] = fraction.getDenominator();	
        } else if (ValueInteger.isInteger(value)) {
        	this.content[index * 2 + 0] = BigInteger.valueOf(ValueInteger.asInteger(value).getInt());
        	this.content[index * 2 + 1] = BigInteger.ONE;
        }
    }
    
    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType());
        assert index >= 0;
        assert index < getTotalSize();
        if (value instanceof ValueFractionBigInteger) {
        	ValueFractionBigInteger fraction = (ValueFractionBigInteger) value;
        	fraction.setNumerator(this.content[index * 2 + 0]);
        	fraction.setDenominator(this.content[index * 2 + 1]);
        }
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        for (int entryNr = 0; entryNr < getTotalSize() * 2; entryNr++) {
            BigInteger entry = content[entryNr];
            hash = entry.hashCode() + (hash << 6) + (hash << 16) - hash;
        }        
        return hash;
    }

    @Override
    public TypeArrayFractionBigInteger getType() {
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
