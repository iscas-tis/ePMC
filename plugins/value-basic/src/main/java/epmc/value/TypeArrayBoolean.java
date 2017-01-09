package epmc.value;

import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.ValueArray;

public final class TypeArrayBoolean implements TypeArray {
    private final static String ARRAY_INDICATOR = "[](boolean)";
	private final TypeBoolean entryType;
    
    TypeArrayBoolean(TypeBoolean entryType) {
    	assert entryType != null;
    	this.entryType = entryType;
    }

    @Override
    public ValueArray newValue() {
        return new ValueArrayBoolean(this);
    }

	@Override
	public ContextValue getContext() {
		return entryType.getContext();
	}

	@Override
	public TypeBoolean getEntryType() {
		return entryType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypeArrayBoolean)) {
			return false;
		}
		TypeArrayBoolean other = (TypeArrayBoolean) obj;
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
