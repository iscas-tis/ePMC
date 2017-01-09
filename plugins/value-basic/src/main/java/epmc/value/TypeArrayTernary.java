package epmc.value;

import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.ValueArray;

final class TypeArrayTernary implements TypeArray {
    private final static String ARRAY_INDICATOR = "[](ternary)";
	private final TypeTernary entryType;
    
    TypeArrayTernary(TypeTernary entryType) {
    	assert entryType != null;
    	this.entryType = entryType;
    }
    
    @Override
    public ValueArray newValue() {
        return new ValueArrayTernary(this);
    }

	@Override
	public ContextValue getContext() {
		return entryType.getContext();
	}

	@Override
	public TypeTernary getEntryType() {
		return entryType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypeArrayTernary)) {
			return false;
		}
		TypeArrayTernary other = (TypeArrayTernary) obj;
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
