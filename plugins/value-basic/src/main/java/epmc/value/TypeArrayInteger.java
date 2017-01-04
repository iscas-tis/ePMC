package epmc.value;

public interface TypeArrayInteger extends TypeArrayAlgebra {
    @Override
    ValueArrayInteger newValue();
    
    @Override
    TypeInteger getEntryType();
}
