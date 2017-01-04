package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class ValueArrayConstant extends ValueArray {
	private final TypeArrayConstant type;
    private final Value content;
	private boolean immutable;
    
    ValueArrayConstant(TypeArrayConstant type) {
        this.type = type;
        this.content = getType().getEntryType().newValue();
    }
    
    @Override
    public ValueArrayConstant clone() {
    	ValueArrayConstant other = new ValueArrayConstant(getType());
    	other.set(this);
    	return other;
    }

    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
    }

    @Override
    public void set(Value value, int index) {
        assert !isImmutable();
        assert value != null;
        assert content.getType().canImport(value.getType());
        assert index >= 0;
        assert index < getTotalSize();
        content.set(value);
    }
    
    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert index >= 0;
        assert index < getTotalSize();
        assert value.getType().canImport(content.getType());
        value.set(content);
    }
    
    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        hash = content.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public TypeArrayConstant getType() {
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
