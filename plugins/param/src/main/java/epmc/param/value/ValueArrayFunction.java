package epmc.param.value;

import epmc.value.ValueArray;

abstract class ValueArrayFunction extends ValueArray {
    @Override
    public abstract ValueArrayFunction clone();
    
    @Override
    public abstract TypeArrayFunction getType();
}
