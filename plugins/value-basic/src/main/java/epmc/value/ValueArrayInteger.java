package epmc.value;

public abstract class ValueArrayInteger extends ValueArrayAlgebra {
    public abstract int getInt(int index);
    
    public abstract void setInt(int value, int index);
    
    @Override
    public abstract TypeArrayInteger getType();
}
