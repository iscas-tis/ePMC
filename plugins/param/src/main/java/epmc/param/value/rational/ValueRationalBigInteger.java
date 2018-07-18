package epmc.param.value.rational;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import epmc.value.Value;

public final class ValueRationalBigInteger implements ValueRational {
    public static boolean is(Value value) {
        return value instanceof ValueRationalBigInteger;
    }

    public static ValueRationalBigInteger as(Value value) {
        if (is(value)) {
            return (ValueRationalBigInteger) value;
        } else {
            return null;
        }
    }

    private final static String DIVIDE = "/";
    private final static String ONE_STRING = "1";
    private final static String ZERO_STRING = "0";
    
    private final TypeRationalBigInteger type;
    private BigInteger numerator = new BigInteger(ZERO_STRING);
    private BigInteger denominator = new BigInteger(ONE_STRING);

    ValueRationalBigInteger(TypeRationalBigInteger type) {
        assert type != null;
        this.type = type;
    }
    
    @Override
    public TypeRational getType() {
        return type;
    }

    @Override
    public double getDouble() {
        BigDecimal numDecimal = new BigDecimal(numerator);
        BigDecimal denDecimal = new BigDecimal(denominator);
        BigDecimal resDecimal = numDecimal.divide(denDecimal, MathContext.DECIMAL128);
        return resDecimal.doubleValue();
    }

    @Override
    public int getInt() {
        assert denominator.equals(BigInteger.ONE);
        return numerator.intValue();
    }

    @Override
    public void set(int value) {
        this.numerator = new BigInteger(Integer.toString(value));
        this.denominator = new BigInteger(ONE_STRING);
    }

    @Override
    public void set(String value) {
        assert value != null;
        UtilRational.set(this, value);
    }

    @Override
    public void set(BigInteger numerator, BigInteger denominator) {
        assert numerator != null;
        assert denominator != null;
        assert assertCanonical(numerator, denominator);
        this.numerator = numerator;
        this.denominator = denominator;
    }

    @Override
    public BigInteger getNumerator() {
        return numerator;
    }

    @Override
    public BigInteger getDenominator() {
        return denominator;
    }
    
    @Override
    public String toString() {
        return numerator + DIVIDE + denominator;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValueRationalBigInteger)) {
            return false;
        }
        ValueRationalBigInteger other = (ValueRationalBigInteger) obj;
        if (!numerator.equals(other.numerator)) {
            return false;
        }
        if (!denominator.equals(other.denominator)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = numerator.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = denominator.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    private static boolean assertCanonical(BigInteger numerator, BigInteger denominator) {
        assert numerator != null;
        assert denominator != null;
        assert denominator.compareTo(BigInteger.ZERO) >= 0;
        assert numerator.gcd(denominator).equals(BigInteger.ONE);
        return true;
    }
}
