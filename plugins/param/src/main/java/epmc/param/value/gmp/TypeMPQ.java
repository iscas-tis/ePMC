package epmc.param.value.gmp;

import epmc.param.value.rational.TypeArrayRational;
import epmc.param.value.rational.TypeRational;
import epmc.value.ContextValue;
import epmc.value.Type;

public final class TypeMPQ implements TypeRational {
    public static boolean is(Type type) {
        return type instanceof TypeMPQ;
    }
    
    public static TypeMPQ as(Type type) {
        if (is(type)) {
            return (TypeMPQ) type;
        } else {
            return null;
        }
    }
    
    public TypeMPQ() {
    }
    
    @Override
    public ValueMPQ newValue() {
        return new ValueMPQ(this);
    }

    @Override
    public TypeArrayRational getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayMPQ(this));
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeMPQ)) {
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
    
    @Override
    public String toString() {
        return "mpq";
    }
}
