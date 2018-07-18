package epmc.param.value.rational;

import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.TypeArrayGeneric;

public final class TypeArrayRationalBigInteger implements TypeArrayRational {
    private final TypeRationalBigInteger entryType;

    TypeArrayRationalBigInteger(TypeRationalBigInteger entryType) {
        assert entryType != null;
        this.entryType = entryType;
    }

    @Override
    public ValueArrayRational newValue() {
        return new ValueArrayRationalBigInteger(this);
    }

    @Override
    public TypeArray getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayGeneric(this));
    }

    @Override
    public TypeRational getEntryType() {
        return entryType;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeArrayRationalBigInteger)) {
            return false;
        }
        TypeArrayRationalBigInteger other = (TypeArrayRationalBigInteger) obj;
        if (entryType != other.entryType) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
}
