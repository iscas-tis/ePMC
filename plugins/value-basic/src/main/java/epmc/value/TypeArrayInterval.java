package epmc.value;

import epmc.value.ContextValue;
import epmc.value.TypeArray;

public final class TypeArrayInterval implements TypeArrayAlgebra {
    private final static String ARRAY_INDICATOR = "[](interval)";
	private final TypeInterval entryType;
    
    TypeArrayInterval(TypeInterval entryType) {
    	assert entryType != null;
    	this.entryType = entryType;
    }
        
    @Override
    public ValueArrayInterval newValue() {
        return new ValueArrayInterval(this);
    }
    
    TypeArray getTypeArrayReal() {
        return entryType.getEntryType().getTypeArray();
    }

	@Override
	public ContextValue getContext() {
		return entryType.getContext();
	}

	@Override
	public TypeInterval getEntryType() {
		return entryType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypeArrayInterval)) {
			return false;
		}
		TypeArrayInterval other = (TypeArrayInterval) obj;
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
