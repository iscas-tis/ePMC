package epmc.value;

import epmc.value.ContextValue;

final class TypeArrayIntegerNative implements TypeArrayInteger {
    private final static String ARRAY_INDICATOR = "[](integer-native)";
	private final TypeInteger entryType;
    
    TypeArrayIntegerNative(TypeInteger entryType) {
    	assert entryType != null;
    	this.entryType = entryType;
    }
    
    @Override
    public ValueArrayInteger newValue() {
        return new ValueArrayIntegerNative(this);
    }

	@Override
	public ContextValue getContext() {
		return entryType.getContext();
	}

	@Override
	public TypeInteger getEntryType() {
		return entryType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypeArrayIntegerNative)) {
			return false;
		}
		TypeArrayIntegerNative other = (TypeArrayIntegerNative) obj;
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
	public TypeArrayAlgebra getTypeArray() {
		// TODO Auto-generated method stub
		return null;
	}
}
