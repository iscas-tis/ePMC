package epmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class ValueArrayGeneric extends ValueArray {
    private final TypeArrayGeneric type;
    private Value[] content;
    private boolean immutable;

    ValueArrayGeneric(TypeArrayGeneric type) {
        this.type = type;
        this.content = new Value[0];
    }
    
    @Override
    public ValueArrayGeneric clone() {
        ValueArrayGeneric clone = (ValueArrayGeneric) getType().newValue();
        clone.set(this);
        return clone;
    }

    void setContent(Type entryType, Value[] content) throws EPMCException {
        assert !isImmutable();
        for (int index = 0; index < getTotalSize(); index++) {
            this.content[index].set(content[index]);
        }
    }
    
    @Override
    protected void setDimensionsContent() {
        assert !isImmutable();
        Type entryType = getType().getEntryType();
        if (this.content.length < getTotalSize()) {
            this.content = new Value[getTotalSize()];
            for (int index = 0; index < content.length; index++) {
                this.content[index] = entryType.newValue();
            }
        }
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert getType().getEntryType().canImport(value.getType()) : getType().getEntryType() + " " + value + " " + value.getType();
        assert index >= 0;
        assert index < getTotalSize();
        content[index].set(value);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert value.getType().canImport(getType().getEntryType());
        assert index >= 0 : index;
        assert index < getTotalSize() : index + " " + getTotalSize();
        value.set(content[index]);
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(getDimensions());
        for (int i = 0; i < getTotalSize(); i++) {
            hash = content[i].hashCode() + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    @Override
    public TypeArrayGeneric getType() {
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
