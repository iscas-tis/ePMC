package epmc.param.value;

import epmc.operator.Operator;
import epmc.value.ValueAlgebra;
import epmc.value.ValueSetString;

public interface ValueFunction extends ValueAlgebra, ValueSetString, Operator {
    void setParameter(String parameter);
    
    @Override
    public TypeFunction getType();
}
