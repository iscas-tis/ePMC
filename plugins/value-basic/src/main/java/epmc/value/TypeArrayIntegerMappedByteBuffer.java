package epmc.value;

import epmc.value.ContextValue;

final class TypeArrayIntegerMappedByteBuffer implements TypeArrayInteger {
    private final static String ARRAY_INDICATOR = "[](integer-mapped-byte-buffer)";
	private final TypeInteger entryType;

    TypeArrayIntegerMappedByteBuffer(TypeInteger entryType) {
    	assert entryType != null;
    	this.entryType = entryType;
    }
    
    @Override
    public ValueArrayIntegerMappedByteBuffer newValue() {
        return new ValueArrayIntegerMappedByteBuffer(this);
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
		if (!(obj instanceof TypeArrayIntegerMappedByteBuffer)) {
			return false;
		}
		TypeArrayIntegerMappedByteBuffer other = (TypeArrayIntegerMappedByteBuffer) obj;
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
