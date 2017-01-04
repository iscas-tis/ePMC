package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;

final class ValueArrayIntegerJava extends ValueArrayInteger implements ValueContentIntArray {
	private final static String SPACE = " ";
	private final TypeArrayIntegerJava type;
    private int[] content;
	private boolean immutable;

    ValueArrayIntegerJava(TypeArrayIntegerJava type) {
        this.type = type;
        this.content = new int[0];
    }
    
    @Override
    public ValueArrayIntegerJava clone() {
        ValueArrayIntegerJava clone = (ValueArrayIntegerJava) getType().newValue();
        clone.set(this);
        return clone;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.length < getTotalSize()) {
            content = new int[getTotalSize()];
        }
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert getType().getEntryType().canImport(value.getType());
        assert index >= 0;
        assert index < getTotalSize() : index + SPACE + getTotalSize();
        content[index] = ValueInteger.asInteger(value).getInt();
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType());
        assert index >= 0;
        assert index < getTotalSize();
        int entry = content[index];
        ValueAlgebra.asAlgebra(value).set(entry);
    }
    
    @Override
    public int[] getIntArray() {
        return content;
    }

    @Override
    public int getInt(int index) {
        assert index >= 0;
        assert index < getTotalSize() : index + " " + getTotalSize();
        return content[index];
    }

    @Override
    public void setInt(int value, int index) {
        content[index] = value;
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        for (int entryNr = 0; entryNr < getTotalSize(); entryNr++) {
            int entry = content[entryNr];
            hash = entry + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }
    
    @Override
    public TypeArrayIntegerJava getType() {
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
