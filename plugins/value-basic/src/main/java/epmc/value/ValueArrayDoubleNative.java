package epmc.value;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.sun.jna.Memory;

import epmc.error.EPMCException;
import epmc.value.Value;

final class ValueArrayDoubleNative extends ValueArrayDouble implements ValueContentMemory {
	private final TypeArrayDoubleNative type;
    private Memory content;
	private boolean immutable;

    ValueArrayDoubleNative(TypeArrayDoubleNative type) {
        assert type != null;
        this.type = type;
        int numBytes = getTotalSize() * 8;
        if (numBytes == 0) {
            numBytes = 1;
        }
        assert numBytes >= 1 : numBytes;
        this.content = new Memory(1);
    }
    
    @Override
    public ValueArrayDoubleNative clone() {
    	ValueArrayDoubleNative other = new ValueArrayDoubleNative(getType());
    	other.set(this);
    	return other;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.size() / 8 < getTotalSize()) {
            content = new Memory(getTotalSize() * 8);
        }
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert getType().getEntryType().canImport(value.getType()) :  value;
        assert index >= 0 : index;
        assert index < getTotalSize() : index + " " + getTotalSize();
        content.setDouble(index * 8, ValueNumber.asNumber(value).getDouble());
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType()) : value;
        assert index >= 0;
        assert index < getTotalSize();
        double entry = content.getDouble(index * 8);
        ValueReal.asReal(value).set(entry);
    }
    
    @Override
    public ByteBuffer getMemory() {
        return content.getByteBuffer(0, Double.BYTES * getTotalSize());
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        for (int entryNr = 0; entryNr < getTotalSize(); entryNr++) {
            long entry = Double.doubleToRawLongBits(content.getDouble(entryNr * 8));
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
            entry >>>= 32;
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
    
    @Override
    public TypeArrayDoubleNative getType() {
    	return type;
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
