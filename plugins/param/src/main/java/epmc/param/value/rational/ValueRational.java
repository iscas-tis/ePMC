package epmc.param.value.rational;

import java.math.BigInteger;

import epmc.value.Value;
import epmc.value.ValueReal;

public interface ValueRational extends ValueReal {
    public static boolean is(Value value) {
        return value instanceof ValueRational;
    }

    public static ValueRational as(Value value) {
        if (is(value)) {
            return (ValueRational) value;
        } else {
            return null;
        }
    }

    void set(BigInteger numerator, BigInteger denominator);
    
    BigInteger getNumerator();
    
    BigInteger getDenominator();
    
    @Override
    TypeRational getType();
}
