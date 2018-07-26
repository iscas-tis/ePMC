package epmc.param.value.dag;

import java.math.BigInteger;

import epmc.param.value.ValueFunction;
import epmc.value.Value;

public final class ValueDag implements ValueFunction {
    private final static String DIVIDE = "/";
    private final static char DOT_CHAR = '.';
    private final static String DOT = ".";
    private final static String DOT_REGEXP = "\\.";
    private final static String INFINITY = "Infinity";
    private final static String M_INFINITY = "-Infinity";
    private final TypeDag type;
    private int number;

    public static boolean is(Value value) {
        return value instanceof ValueDag;
    }

    public static ValueDag as(Value value) {
        if (is(value)) {
            return (ValueDag) value;
        }
        return null;
    }

    ValueDag(TypeDag type) {
        assert type != null;
        this.type = type;
    }

    @Override
    public void set(int value) {
        number = getDag().getNumber(new BigInteger(Integer.toString(value)), BigInteger.ONE);
    }

    @Override
    public void set(String value) {
        BigInteger num = null;
        BigInteger den = null;
        if (value.equals(INFINITY)) {
            num = BigInteger.ONE;
            den = BigInteger.ZERO;
        } else if (value.equals(M_INFINITY)) {
            num = BigInteger.ONE.negate();
            den = BigInteger.ZERO;
        } else {
            if (value.contains(DOT)) {
                int pow = value.length() - 1 - value.indexOf(DOT_CHAR);
                String[] parts = value.split(DOT_REGEXP);
                assert parts.length == 2;
                value = parts[0] + parts[1];
                num = new BigInteger(value);
                den = BigInteger.TEN.pow(pow);
            } else if (value.contains(DIVIDE)) {
                String[] parts = value.split(DIVIDE);
                assert parts.length == 2;
                num = new BigInteger(parts[0]);
                den = new BigInteger(parts[1]);
            } else {
                num = new BigInteger(value);
                den = BigInteger.ONE;
            }
        }
        number = getDag().getNumber(num, den);
    }

    public void set(BigInteger num, BigInteger den) {
        number = getDag().getNumber(num, den);
    }

    @Override
    public void setParameter(String parameter) {
        number = getDag().getParameter(parameter);
    }

    @Override
    public TypeDag getType() {
        return type;
    }

    private Dag getDag() {
        return type.getDag();
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return getDag().toString(number);
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = number + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueDag)) {
            return false;
        }
        ValueDag other = (ValueDag) obj;
        if (this.number != other.number) {
            return false;
        }
        return true;
    }
}
