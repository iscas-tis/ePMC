package epmc.param.value.rational;

import epmc.value.ContextValue;

public final class TypeRationalBigInteger implements TypeRational {

    public TypeRationalBigInteger() {
    }
    
    @Override
    public TypeArrayRational getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayRationalBigInteger(this));
    }

    @Override
    public ValueRational newValue() {
        return new ValueRationalBigInteger(this);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
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
