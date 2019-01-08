package epmc.qmc.value;

import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeArrayGeneric;

public final class TypeArrayComplex implements TypeArrayAlgebra {
    private final static String ARRAY_INDICATOR = "[](complex)";
    private final TypeComplex entryType;

    TypeArrayComplex(TypeComplex entryType) {
        assert entryType != null;
        this.entryType = entryType;
    }

    @Override
    public ValueArrayComplex newValue() {
        return new ValueArrayComplex(this);
    }

    @Override
    public TypeComplex getEntryType() {
        return entryType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeArrayComplex)) {
            return false;
        }
        TypeArrayComplex other = (TypeArrayComplex) obj;
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
        return ContextValue.get().makeUnique(new TypeArrayGeneric(this));
    }
}
