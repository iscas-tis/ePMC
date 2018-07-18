package epmc.param.value.polynomialfraction;

import epmc.param.value.polynomial.TypeArrayPolynomial;
import epmc.value.TypeArrayAlgebra;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;

public class ValueArrayPolynomialFraction implements ValueArrayAlgebra {
    public static boolean is(Value value) {
        return value instanceof ValueArrayPolynomialFraction;
    }
    
    public static ValueArrayPolynomialFraction as(Value value) {
        if (is(value)) {
            return (ValueArrayPolynomialFraction) value;
        } else {
            return null;
        }
    }

    private final TypeArrayPolynomialFraction type;
    private final TypeArrayPolynomial polyTypeArray;
    private final ValueArrayAlgebra array;

    public ValueArrayPolynomialFraction(TypeArrayPolynomialFraction type) {
        this.type = type;
        polyTypeArray = type.getEntryType().getTypePolynomial().getTypeArray();
        array = polyTypeArray.newValue();
        array.setSize(0);
    }

    @Override
    public void setSize(int size) {
        array.setSize(size * 2);
    }

    @Override
    public int size() {
        return array.size() / 2;
    }

    @Override
    public void get(Value value, int index) {
        ValuePolynomialFraction valueFraction = ValuePolynomialFraction.as(value);
        array.get(valueFraction.getNumerator(), index * 2);
        array.get(valueFraction.getDenominator(), index * 2 + 1);
    }

    @Override
    public void set(Value value, int index) {
        ValuePolynomialFraction valueFraction = ValuePolynomialFraction.as(value);
        array.set(valueFraction.getNumerator(), index * 2);
        array.set(valueFraction.getDenominator(), index * 2 + 1);
    }

    @Override
    public void set(int entry, int index) {
        array.set(entry, index * 2);
        array.set(1, index * 2 + 1);
    }

    @Override
    public TypeArrayAlgebra getType() {
        return type;
    }
}
