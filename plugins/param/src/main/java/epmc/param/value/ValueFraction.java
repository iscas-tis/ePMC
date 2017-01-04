package epmc.param.value;

import epmc.value.ValueReal;

abstract class ValueFraction implements ValueReal {
    @Override
	public abstract ValueFraction clone();
}
