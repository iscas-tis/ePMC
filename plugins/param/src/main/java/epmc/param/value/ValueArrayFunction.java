package epmc.param.value;

import epmc.value.ValueArray;

abstract class ValueArrayFunction implements ValueArray {
    @Override
    public abstract TypeArrayFunction getType();
}
