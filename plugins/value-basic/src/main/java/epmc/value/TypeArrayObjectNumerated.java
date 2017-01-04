package epmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.ValueArray;

public final class TypeArrayObjectNumerated implements TypeArray {
    private final static String ARRAY_INDICATOR = "[](object-numerated)";
    private final boolean objectIdentity;
	private final TypeObject entryType;
        
    TypeArrayObjectNumerated(TypeObject entryType, boolean objectIdentity) {
    	assert entryType != null;
    	this.entryType = entryType;
    	this.objectIdentity = objectIdentity;
    }

    @Override
    public ValueArray newValue() {
        ValueArrayObjectNumerated value = new ValueArrayObjectNumerated(this, objectIdentity);
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (!(obj instanceof TypeArrayObjectNumerated)) {
    		return false;
    	}
        TypeArrayObjectNumerated other = (TypeArrayObjectNumerated) obj;
        if (this.objectIdentity == other.objectIdentity) {
            return false;
        }
        return this.getEntryType().equals(other.getEntryType());
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = getEntryType().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = (objectIdentity ? 11 : 13) + (hash << 6) + (hash << 16) - hash;
        return hash;
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getEntryType());
        builder.append(ARRAY_INDICATOR);
        return builder.toString();
    }
    
	@Override
    public TypeArray getTypeArray() {
        return getContext().makeUnique(new TypeArrayGeneric(this));
    }
}
