package epmc.value;

import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.ValueArray;

final class TypeArrayObjectDirect implements TypeArray {
    private final static String ARRAY_INDICATOR = "[](object-direct)";
	private final TypeObject entryType;
    
    TypeArrayObjectDirect(TypeObject entryType) {
    	assert entryType != null;
    	this.entryType = entryType;
    }

    @Override
    public ValueArray newValue() {
        return new ValueArrayObjectDirect(this);
    }

	@Override
	public ContextValue getContext() {
		return entryType.getContext();
	}

	@Override
	public TypeObject getEntryType() {
		return entryType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypeArrayObjectDirect)) {
			return false;
		}
		TypeArrayObjectDirect other = (TypeArrayObjectDirect) obj;
		return this.getEntryType().equals(other.getEntryType());
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
    
	@Override
    public TypeArray getTypeArray() {
        return getContext().makeUnique(new TypeArrayGeneric(this));
    }
}
