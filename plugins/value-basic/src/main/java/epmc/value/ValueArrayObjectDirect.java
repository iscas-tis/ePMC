package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueArray;

final class ValueArrayObjectDirect extends ValueArray {
	private final TypeArrayObjectDirect type;
    private Object[] content;
	private boolean immutable;

    ValueArrayObjectDirect(TypeArrayObjectDirect type) {
    	this.type = type;
        this.content = new Object[getTotalSize()];
    }
    
    @Override
    public ValueArrayObjectDirect clone() {
    	ValueArrayObjectDirect other = new ValueArrayObjectDirect(getType());
    	other.set(this);
    	return other;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        if (this.content.length < getTotalSize()) {
            content = new Object[getTotalSize()];
        }
    }
    
    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert ValueObject.isObject(value);
        assert index >= 0;
        assert index < getTotalSize();
        content[index] = ValueObject.asObject(value).getObject();
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert ValueObject.isObject(value);
        assert index >= 0;
        assert index < getTotalSize();
        Object entry = content[index];
        ValueObject.asObject(value).set(entry);
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        for (int entryNr = 0; entryNr < getTotalSize(); entryNr++) {
            long entry = 0;
            if (content[entryNr] != null) {
                entry = content[entryNr].hashCode();
            }
            hash = ((int) entry) + (hash << 6) + (hash << 16) - hash;
        }        
        return hash;
    }
    
    @Override
    public TypeArrayObjectDirect getType() {
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
