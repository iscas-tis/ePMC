package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;

public final class ValueArrayDoubleJava extends ValueArrayDouble implements ValueContentDoubleArray {
	public static boolean isValueArrayDoubleJava(Value value) {
		return value instanceof ValueArrayDoubleJava;
	}
	
	public static ValueArrayDoubleJava asValueArrayDoubleJava(Value value) {
		if (isValueArrayDoubleJava(value)) {
			return (ValueArrayDoubleJava) value;
		} else {
			return null;
		}
	}
	
    private final static String SPACE = " ";
    private double[] content;
	private final TypeArrayDouble type;
	private boolean immutable;

    ValueArrayDoubleJava(TypeArrayDouble type) {
    	assert type != null;
    	this.type = type;
        this.content = new double[0];
    }
    
    @Override
    public ValueArrayDoubleJava clone() {
        ValueArrayDoubleJava clone = (ValueArrayDoubleJava) getType().newValue();
        clone.set(this);
        return clone;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.length < getTotalSize()) {
            content = new double[getTotalSize()];
        }
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert getType().getEntryType().canImport(value.getType()) : value;
        assert index >= 0 : index;
        assert index < getTotalSize() : index + SPACE + getTotalSize();
        content[index] = ValueNumber.asNumber(value).getDouble();
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType()) :
            this + SPACE + this.getType() + SPACE + value
            + SPACE + value.getType();
        assert index >= 0 : index;
        assert index < getTotalSize() : index + SPACE + getTotalSize();
        double entry = content[index];
        ValueReal.asReal(value).set(entry);
    }
    
    @Override
    public double[] getDoubleArray() {
        return content;
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        for (int entryNr = 0; entryNr < getTotalSize(); entryNr++) {
            long entry = Double.doubleToRawLongBits(content[entryNr]);
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
            entry >>>= 32;
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
    
    @Override
    public TypeArrayDouble getType() {
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
