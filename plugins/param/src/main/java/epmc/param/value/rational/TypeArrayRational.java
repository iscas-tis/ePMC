package epmc.param.value.rational;

import epmc.value.TypeArrayReal;

public interface TypeArrayRational extends TypeArrayReal {
    TypeRational getEntryType();
    
    @Override
    public ValueArrayRational newValue();
}
