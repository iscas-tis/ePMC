package epmc.param.value.dag;

import epmc.value.ContextValue;
import epmc.value.TypeArray;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeArrayGeneric;

public final class TypeArrayFunctionDag implements TypeArrayAlgebra {
    private final TypeDag entryType;

    TypeArrayFunctionDag(TypeDag type) {
        assert type != null;
        this.entryType = type;
    }
    
    @Override
    public TypeArray getTypeArray() {
        return ContextValue.get().makeUnique(new TypeArrayGeneric(this));
    }

    @Override
    public TypeDag getEntryType() {
        return entryType;
    }

    @Override
    public ValueArrayDag newValue() {
        return new ValueArrayDag(this);
    }
}
