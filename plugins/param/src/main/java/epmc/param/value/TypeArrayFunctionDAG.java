package epmc.param.value;

import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.TypeArrayGeneric;

final class TypeArrayFunctionDAG extends TypeArrayFunction {
    private final static String ARRAY_INDICATOR = "[](function-dag)";
	private final TypeFunctionDAG entryType;

    TypeArrayFunctionDAG(TypeFunctionDAG entryType) {
    	assert entryType != null;
    	this.entryType = entryType;
    }

    @Override
    public ValueArrayFunctionDAG newValue() {
        return new ValueArrayFunctionDAG(this);
    }

	@Override
	public ContextValue getContext() {
		return entryType.getContext();
	}

	@Override
	public TypeFunctionDAG getEntryType() {
		return entryType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypeArrayFunctionDAG)) {
			return false;
		}
		TypeArrayFunctionDAG other = (TypeArrayFunctionDAG) obj;
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
