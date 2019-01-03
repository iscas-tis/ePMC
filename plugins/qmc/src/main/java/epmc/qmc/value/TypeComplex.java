package epmc.qmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeNumBitsKnown;
import epmc.value.TypeReal;

public final class TypeComplex implements TypeAlgebra, TypeNumBitsKnown {
    private final static String COMPLEX = "complex";
    private final TypeReal typeReal;
    private final int numBits;

    public static boolean is(Type type) {
        assert type != null;
        return type instanceof TypeComplex;
    }

    public static TypeComplex as(Type type) {
        assert type != null;
        if (is(type)) {
            return (TypeComplex) type;
        } else {
            return null;
        }
    }

    public static void set(TypeComplex type) {
        assert type != null;
        ContextValue.get().setType(TypeComplex.class, ContextValue.get().makeUnique(type));
    }

    public static TypeComplex get() {
        return ContextValue.get().getType(TypeComplex.class);
    }

    public TypeComplex(TypeReal typeReal) {
        assert typeReal != null;
        this.typeReal = typeReal;
        int numRealBits = TypeNumBitsKnown.getNumBits(typeReal);
        if (numRealBits == TypeNumBitsKnown.UNKNOWN) {
            this.numBits = TypeNumBitsKnown.UNKNOWN;
        } else {
            this.numBits = 2 * numRealBits;
        }
    }

    TypeComplex() {
        this(TypeReal.get());
    }

    @Override
    public int getNumBits() {
        return numBits;
    }    

    public TypeReal getTypeReal() {
        return typeReal;
    }

    @Override
    public ValueComplex newValue() {
        return new ValueComplex(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(COMPLEX);
        return builder.toString();
    }

    @Override
    public TypeArrayComplex getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayComplex(this));
        // TODO 
        //    	return null;
        //    	return (TypeArrayComplex) TypeAlgebra.super.getTypeArray();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = typeReal.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeComplex)) {
            return false;
        }
        TypeComplex other = (TypeComplex) obj;
        if (!this.typeReal.equals(other.typeReal)) {
            return false;
        }
        return true;
    }
}
