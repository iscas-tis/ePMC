package epmc.param.value.gmp;

import epmc.param.value.rational.TypeArrayRational;
import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.TypeArrayGeneric;

public final class TypeArrayMPQ implements TypeArrayRational {
    private final TypeMPQ entryType;

    TypeArrayMPQ(TypeMPQ entryType) {
        assert entryType != null;
        this.entryType = entryType;
    }
    
    @Override
    public TypeArray getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayGeneric(this));
    }

    @Override
    public TypeMPQ getEntryType() {
        return entryType;
    }

    @Override
    public ValueArrayMPQ newValue() {
        return new ValueArrayMPQ(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeArrayMPQ)) {
            return false;
        }
        TypeArrayMPQ other = (TypeArrayMPQ) obj;
        if (entryType != other.entryType) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = entryType.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    @Override
    public String toString() {
        return "mpq[]";
    }
}
