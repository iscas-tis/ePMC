package epmc.param.value;

import epmc.value.ContextValue;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeArrayReal;

public final class TypeArrayFractionBigInteger implements TypeArrayReal {
    private final static String ARRAY_INDICATOR = "[](big-integer)";
	private final TypeFractionBigInteger entryType;

	TypeArrayFractionBigInteger(TypeFractionBigInteger entryType) {
		assert entryType != null;
		this.entryType = entryType;
	}
	
	@Override
	public ValueArrayFractionBigInteger newValue() {
        return new ValueArrayFractionBigInteger(this);
	}

	@Override
	public ContextValue getContext() {
		return entryType.getContext();
	}

	@Override
	public TypeFractionBigInteger getEntryType() {
		return entryType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TypeArrayFractionBigInteger)) {
			return false;
		}
		TypeArrayFractionBigInteger other = (TypeArrayFractionBigInteger) obj;
		return this.entryType.equals(other.entryType);
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
		// TODO
		return null;
//        return getContext().makeUnique(new TypeArrayGeneric(this));
    }
}
