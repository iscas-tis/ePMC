package epmc.qmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeArrayGenericAlgebra;
import epmc.value.TypeNumBitsKnown;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;

public final class TypeSuperOperator implements TypeWeightTransition, TypeWeight, TypeNumBitsKnown {
    private final static String SUPEROPERATOR = "superoperator";
    public static final int DIMENSIONS_UNSPECIFIED = -1;
    private final int numBits;
    private final TypeMatrix typeMatrix;

    public static boolean is(Type type) {
        return type instanceof TypeSuperOperator;
    }
    
    public static TypeSuperOperator as(Type type) {
        if (is(type)) {
            return (TypeSuperOperator) type;
        } else {
            return null;
        }
    }
    
    public static TypeSuperOperator get() {
        return ContextValue.get().getType(TypeSuperOperator.class);
    }

    public static void set(TypeSuperOperator type) {
        assert type != null;
        ContextValue.get().setType(TypeSuperOperator.class,
                ContextValue.get().makeUnique(type));
    }

    public TypeSuperOperator() {
        this.typeMatrix = TypeMatrix.get(TypeComplex.get());
        numBits = -1;
    }

    public TypeReal getTypeReal() {
        return TypeReal.get();
    }

    TypeComplex getTypeComplex() {
        return TypeComplex.get();
    }

    TypeMatrix getTypeMatrix() {
        return typeMatrix;
    }

    @Override
    public ValueSuperOperator newValue() {
        return new ValueSuperOperator(this);
    }

    public Type getTypeList() {
        return getTypeMatrix().getTypeArray();
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!super.equals(obj)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = super.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(SUPEROPERATOR);
        return builder.toString();
    }

    @Override
    public int getNumBits() {
        return numBits;
    }

    @Override
    public TypeArrayAlgebra getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayGenericAlgebra(this));
    }
}
