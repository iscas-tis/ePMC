package epmc.param.value.polynomial;

import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeArrayGeneric;
import epmc.value.ValueArrayAlgebra;

public final class TypeArrayPolynomial implements TypeArrayAlgebra {
    private TypePolynomial typeEntry;

    public TypeArrayPolynomial(TypePolynomial typeEntry) {
        assert typeEntry != null;
        this.typeEntry = typeEntry;
    }

    @Override
    public TypeArray getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayGeneric(this));
    }

    @Override
    public TypePolynomial getEntryType() {
        return typeEntry;
    }

    @Override
    public ValueArrayAlgebra newValue() {
        return new ValueArrayPolynomial(this);
    }
}
