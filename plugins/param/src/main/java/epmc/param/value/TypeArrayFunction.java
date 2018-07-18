package epmc.param.value;

import epmc.value.TypeArray;

abstract class TypeArrayFunction implements TypeArray { // TODO
    @Override
    public abstract ValueArrayFunction newValue();
    
    @Override
    public abstract TypeFunction getEntryType();
}
