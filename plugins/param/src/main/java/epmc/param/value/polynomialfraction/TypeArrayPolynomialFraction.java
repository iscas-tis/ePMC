package epmc.param.value.polynomialfraction;

import epmc.value.TypeArray;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeArrayGeneric;
import epmc.value.ValueArrayAlgebra;

public final class TypeArrayPolynomialFraction implements TypeArrayAlgebra {
    private final TypePolynomialFraction entryType;

    TypeArrayPolynomialFraction(TypePolynomialFraction entryType) {
        assert entryType != null;
        this.entryType = entryType;
    }

    @Override
    public ValueArrayAlgebra newValue() {
        return new ValueArrayPolynomialFraction(this);
    }

    @Override
    public TypeArray getTypeArray() {
        return new TypeArrayGeneric(this);
    }

    @Override
    public TypePolynomialFraction getEntryType() {
        return entryType;
    }
}
