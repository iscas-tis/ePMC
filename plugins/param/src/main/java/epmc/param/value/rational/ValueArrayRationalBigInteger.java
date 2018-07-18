package epmc.param.value.rational;

import java.math.BigInteger;

import epmc.value.UtilValue;
import epmc.value.Value;

public final class ValueArrayRationalBigInteger implements ValueArrayRational {
    private final TypeArrayRationalBigInteger type;
    private BigInteger[] content = new BigInteger[0];

    public static boolean is(Value value) {
        return value instanceof ValueArrayRationalBigInteger;
    }

    public static ValueArrayRationalBigInteger as(Value value) {
        if (is(value)) {
            return (ValueArrayRationalBigInteger) value;
        } else {
            return null;
        }
    }

    ValueArrayRationalBigInteger(TypeArrayRationalBigInteger type) {
        assert type != null;
        this.type = type;
    }
    
    @Override
    public void set(int entry, int index) {
        content[index * 2] = new BigInteger(Integer.toString(entry));
        content[index * 2 + 1] = BigInteger.ONE;
    }

    @Override
    public TypeArrayRational getType() {
        return type;
    }

    @Override
    public void setSize(int size) {
        content = new BigInteger[size * 2];
    }

    @Override
    public int size() {
        return content.length / 2;
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert index >= 0 : index;
        assert index < content.length / 2 : index;
        assert ValueRationalBigInteger.is(value) : value;
        ValueRationalBigInteger valueRationalBigInteger = ValueRationalBigInteger.as(value);
        valueRationalBigInteger.set(content[index * 2], content[index * 2 + 1]);
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert index >= 0 : index;
        assert index < content.length / 2 : index;
        assert ValueRationalBigInteger.is(value) : value;
        ValueRationalBigInteger valueRationalBigInteger = ValueRationalBigInteger.as(value);
        content[index * 2] = valueRationalBigInteger.getNumerator();
        content[index * 2 + 1] = valueRationalBigInteger.getDenominator();
    }
    
    @Override
    public String toString() {
        return UtilValue.arrayToString(this);
    }
}
