package epmc.param.value.polynomialfraction;

import java.io.IOException;
import java.math.BigInteger;

import epmc.options.UtilOptions;
import epmc.param.options.OptionsParam;
import epmc.param.value.ValueFunction;
import epmc.param.value.polynomial.PolynomialFractionExporter;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueSetString;

public class ValuePolynomialFraction implements ValueFunction, ValueAlgebra, ValueSetString {
    private final static String DIVIDE = "/";
    private final static char DOT_CHAR = '.';
    private final static String DOT = ".";
    private final static String DOT_REGEXP = "\\.";
    private TypePolynomialFraction type;
    private final ValuePolynomial numerator;
    private final ValuePolynomial denominator;
 
    public static boolean is(Value value) {
        return value instanceof ValuePolynomialFraction;
    }
    
    public static ValuePolynomialFraction as(Value value) {
        if (is(value)) {
            return (ValuePolynomialFraction) value;
        } else {
            return null;
        }
    }
    
    public ValuePolynomialFraction(TypePolynomialFraction type) {
        assert type != null;
        this.type = type;
        TypePolynomial typePolynomial = type.getTypePolynomial();
        numerator = typePolynomial.newValue();
        denominator = typePolynomial.newValue();
        denominator.set(1);
    }

    @Override
    public TypePolynomialFraction getType() {
        return type;
    }

    @Override
    public void setParameter(String parameter) {
        assert parameter != null;
        numerator.setParameter(parameter);
        denominator.set(1);
    }

    @Override
    public void set(int value) {
        numerator.set(value);
        denominator.set(1);
    }

    @Override
    public void set(String value) {
        assert value != null;
        if (value.contains(DOT)) {
            int pow = value.length() - 1 - value.indexOf(DOT_CHAR);
            String[] parts = value.split(DOT_REGEXP);
            assert parts.length == 2;
            value = parts[0] + parts[1];
            numerator.set(value);
            denominator.set(BigInteger.TEN.pow(pow));
            normalise();
        } else if (value.contains(DIVIDE)) {
            String[] parts = value.split(DIVIDE);
            assert parts.length == 2;
            numerator.set(parts[0]);
            denominator.set(parts[1]);
            normalise();
        } else {
            numerator.set(value);
            denominator.set(1);
        }
        assert !denominator.toString().equals("0");
    }

    public void adjustNumParameters() {
        numerator.adjustNumParameters();
        denominator.adjustNumParameters();
    }

    TypePolynomial getTypePolynomial() {
        return type.getTypePolynomial();
    }

    public void normalise() {
        getTypePolynomial().cancelCommonFactors(numerator, denominator);
        assert !denominator.toString().equals("0");
    }

    @Override
    public String toString() {
        PolynomialFractionExporter.Builder builder = UtilOptions.getInstance(OptionsParam.PARAM_FRACTION_EXPORTER);
        builder.addFunction(this);
        PolynomialFractionExporter exporter = builder.build();
        StringBuilder sbui = new StringBuilder();
        try {
            exporter.export(sbui);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return sbui.toString();
    }

    public ValuePolynomial getNumerator() {
        return numerator;
    }

    public ValuePolynomial getDenominator() {
        return denominator;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValuePolynomialFraction)) {
            return false;
        }
        ValuePolynomialFraction other = (ValuePolynomialFraction) obj;
        if (!this.numerator.equals(other.numerator)) {
            return false;
        }
        if (!this.denominator.equals(other.denominator)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = numerator.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = denominator.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
}
