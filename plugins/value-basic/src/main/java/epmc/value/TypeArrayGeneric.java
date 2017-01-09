package epmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;

public final class TypeArrayGeneric implements TypeArray {
    private final static String ARRAY_INDICATOR = "[](generic)";
    private final Type entryType;
    
    public TypeArrayGeneric(Type entryType) {
        assert entryType != null;
        this.entryType = entryType;
    }
    
    @Override
    public ValueArrayGeneric newValue() {
        return new ValueArrayGeneric(this);
    }

    @Override
    public ContextValue getContext() {
        return entryType.getContext();
    }

    @Override
    public Type getEntryType() {
        return entryType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeArrayGeneric)) {
            return false;
        }
        TypeArrayGeneric other = (TypeArrayGeneric) obj;
        if (!this.getEntryType().equals(other.getEntryType())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = getEntryType().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getEntryType());
        builder.append(ARRAY_INDICATOR);
        return builder.toString();
    }
    
    public TypeArray getTypeArray() {
        return getContext().makeUnique(new TypeArrayGeneric(this));
    }
}
