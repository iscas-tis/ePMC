package epmc.qmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeArrayGenericAlgebra;

public final class TypeMatrix implements TypeAlgebra {
    private final static String MATRIX = "matrix(%s)";

    public static TypeMatrix get(TypeArrayAlgebra typeArray) {
        assert typeArray != null;
        return ContextValue.get().makeUnique(new TypeMatrix.Builder()
                .setTypeArray(typeArray).build());
    }

    public static TypeMatrix get(TypeAlgebra typeAlgebra) {
        assert typeAlgebra != null;
        return TypeMatrix.get(typeAlgebra.getTypeArray());
    }

    public final static class Builder {
        private TypeArrayAlgebra typeArray;

        public Builder setTypeArray(TypeArrayAlgebra typeArray) {
            this.typeArray = typeArray;
            return this;
        }

        private TypeArrayAlgebra getTypeArray() {
            return typeArray;
        }        

        public TypeMatrix build() {
            assert typeArray != null;
            return ContextValue.get().makeUnique(new TypeMatrix(this));
        }
    }

    public final static boolean is(Type type) {
        return type instanceof TypeMatrix;
    }

    public static TypeMatrix as(Type type) {
        if (is(type)) {
            return (TypeMatrix) type;
        } else {
            return null;
        }
    }

    private final TypeArrayAlgebra typeArray;

    private TypeMatrix(Builder builder) {
        assert builder != null;
        assert builder.getTypeArray() != null;
        this.typeArray = builder.getTypeArray();
    }

    @Override
    public ValueMatrix newValue() {
        return new ValueMatrix(this);
    }

    public ValueMatrix newValue(int numRows, int numColumns) {
        ValueMatrix result = newValue();
        result.setDimensions(numRows, numColumns);
        return result;
    }

    public TypeArray getArrayType() {
        return typeArray;
    }

    public TypeAlgebra getEntryType() {
        return typeArray.getEntryType();
    }

    @Override
    public TypeArrayGenericAlgebra getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayGenericAlgebra(this));
    }

    @Override
    public String toString() {
        return String.format(MATRIX, getEntryType());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypeMatrix)) {
            return false;
        }
        TypeMatrix other = (TypeMatrix) obj;
        if (!this.typeArray.equals(other.typeArray)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = typeArray.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
}
