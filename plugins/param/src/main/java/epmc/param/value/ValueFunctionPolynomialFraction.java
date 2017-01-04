package epmc.param.value;

import java.math.BigInteger;
import java.util.BitSet;

import epmc.error.EPMCException;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

class ValueFunctionPolynomialFraction implements ValueFunction, ValueAlgebra {
	private static final int NUM_IMPORT_VALUES = 2;
    private final ValueFunctionPolynomialFraction importPolynomials[] = new ValueFunctionPolynomialFraction[NUM_IMPORT_VALUES];
	private TypeFunctionPolynomialFraction type;
	private boolean immutable;
	private final ValueFunctionPolynomial numerator;
	private final ValueFunctionPolynomial denominator;
	private ValueReal asConstant;
	private BitSet neededParams = new BitSet();
	
	public ValueFunctionPolynomialFraction(
			TypeFunctionPolynomialFraction type) {
		assert type != null;
		this.type = type;
		TypeFunctionPolynomial typePolynomial = type.getTypePolynomial();
		this.numerator = typePolynomial.newValue();
		this.denominator = typePolynomial.newValue();
		this.denominator.set(1);
	}

	@Override
	public TypeFunctionPolynomialFraction getType() {
		return type;
	}

	@Override
	public void setImmutable() {
		this.immutable = true;
	}

	@Override
	public boolean isImmutable() {
		return immutable;
	}

	@Override
	public int compareTo(Value o) {
		assert o != null;
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ValueFunctionPolynomialFraction clone() {
		ValueFunctionPolynomialFraction result = new ValueFunctionPolynomialFraction(
				this.type);
		result.numerator.set(this.numerator);
		result.denominator.set(this.denominator);
		return result;
	}

	@Override
	public void evaluate(ValueReal result, Point point) throws EPMCException {
    	assert result != null;
    	assert ValueReal.isReal(result);
    	assert point != null;
		adjustNumParameters();
		point.adjustNumParameters();
		Value entry = type.getContextPARAM().getTypeReal().newValue();
		this.neededParams.clear();
		this.neededParams.or(this.numerator.getNeededParams());
		this.neededParams.or(this.denominator.getNeededParams());
		for (int paramNr = 0; paramNr < type.getNumParameters(); paramNr++) {
			Object parameter = type.getParameter(paramNr);
			if (this.neededParams.get(paramNr) &&
					(parameter instanceof Unevaluated)) {
				Unevaluated unevaluated = (Unevaluated) parameter;
				unevaluated.evaluate(entry, point);
				point.setDimension(entry, paramNr);
			}
		}
		
		ValueReal num = UtilValue.clone(result);
		this.numerator.evaluate(num, point);
		ValueReal den = UtilValue.clone(result);
		this.denominator.evaluate(den, point);
		result.divide(num, den);
	}

	@Override
	public void setParameter(Object parameter) {
		assert parameter != null;
		this.numerator.setParameter(parameter);
		this.denominator.set(1);
	}

	@Override
	public void set(Value value) {
        ValueFunctionPolynomialFraction opPoly = castOrImport(value, 0);
        this.denominator.set(opPoly.denominator);
        this.numerator.set(opPoly.numerator);
	}

	@Override
	public void set(int value) {
		this.numerator.set(value);
		this.denominator.set(1);
	}

	@Override
	public void set(String value) {
		assert value != null;
		if (value.contains(".")) {
            int pow = value.length() - 1 - value.indexOf('.');
            String[] parts = value.split("\\.");
            assert parts.length == 2;
            value = parts[0] + parts[1];
            this.numerator.set(value);
            this.denominator.set(BigInteger.TEN.pow(pow));
			normalise();
		} else if (value.contains("/")) {
			String[] parts = value.split("/");
			assert parts.length == 2;
			this.numerator.set(parts[0]);
			this.denominator.set(parts[1]);
			normalise();
		} else {
			this.numerator.set(value);
			this.denominator.set(1);
		}
	}

	@Override
	public void add(Value operand1, Value operand2) throws EPMCException {
		addOrSubtract(operand1, operand2, false);
	}
	
	@Override
	public void subtract(Value operand1, Value operand2)
			throws EPMCException {
		addOrSubtract(operand1, operand2, true);
	}
	
	public void addOrSubtract(Value operand1, Value operand2, boolean subtract)
			throws EPMCException {
        ValueFunctionPolynomialFraction poly1 = castOrImport(operand1, 0);
        ValueFunctionPolynomialFraction poly2 = castOrImport(operand2, 1);
        ValueFunctionPolynomial newNumerator = getTypePolynomial().newValue();
        ValueFunctionPolynomial newDenominator = getTypePolynomial().newValue();
        ValueFunctionPolynomial newNumeratorLeft = getTypePolynomial().newValue();
        ValueFunctionPolynomial newNumeratorRight = getTypePolynomial().newValue();
        newNumeratorLeft.multiply(poly1.numerator, poly2.denominator);
        newNumeratorRight.multiply(poly2.numerator, poly1.denominator);
        if (subtract) {
            newNumerator.subtract(newNumeratorLeft, newNumeratorRight);
        } else {
            newNumerator.add(newNumeratorLeft, newNumeratorRight);        	
        }
        newDenominator.multiply(poly1.denominator, poly2.denominator);
        this.numerator.set(newNumerator);
        this.denominator.set(newDenominator);
        normalise();
	}
	
	@Override
	public void addInverse(Value operand) throws EPMCException {
        ValueFunctionPolynomialFraction poly = castOrImport(operand, 0);
        this.numerator.addInverse(poly.numerator);
        this.denominator.set(poly.denominator);
        normalise();
	}
	
	@Override
	public void multInverse(Value operand) throws EPMCException {
        ValueFunctionPolynomialFraction poly = castOrImport(operand, 0);
        this.numerator.set(poly.denominator);
        this.denominator.set(poly.numerator);
        normalise();
	}
	
	@Override
	public ValueFunctionPolynomialFraction castOrImport(Value operand, int number) {
	    assert operand != null;
	    assert number >= 0;
	    assert number < NUM_IMPORT_VALUES;
	    if (operand instanceof ValueFunctionPolynomialFraction) {
	    	ValueFunctionPolynomialFraction result = (ValueFunctionPolynomialFraction) operand;
	    	result.adjustNumParameters();
	    	return result;
	    } else if (ValueInteger.isInteger(operand)) {
	        if (importPolynomials[number] == null) {
	            importPolynomials[number] = getType().newValue();
	        }
	        importPolynomials[number].set(ValueInteger.asInteger(operand).getInt());
	        return (ValueFunctionPolynomialFraction) importPolynomials[number];
	    } else if (operand instanceof ValueFraction) {
	        if (importPolynomials[number] == null) {
	            importPolynomials[number] = getType().newValue();
	        }
	        importPolynomials[number].set(operand.toString());
	        return (ValueFunctionPolynomialFraction) importPolynomials[number];
	    } else {
	        assert false : operand + " " + operand.getType();
	        return null;
	    }
	}

	@Override
	public void multiply(Value operand1, Value operand2)
			throws EPMCException {
        ValueFunctionPolynomialFraction poly1 = castOrImport(operand1, 0);
        ValueFunctionPolynomialFraction poly2 = castOrImport(operand2, 1);
        multiply(poly1.numerator, poly1.denominator, poly2.numerator, poly2.denominator);
	}

	@Override
	public void divide(Value operand1, Value operand2)
			throws EPMCException {
        ValueFunctionPolynomialFraction poly1 = castOrImport(operand1, 0);
        ValueFunctionPolynomialFraction poly2 = castOrImport(operand2, 1);
        multiply(poly1.numerator, poly1.denominator, poly2.denominator, poly2.numerator);
	}

	private void multiply(
			ValueFunctionPolynomial poly1Numerator,
			ValueFunctionPolynomial poly1Denominator,
			ValueFunctionPolynomial poly2Numerator,
			ValueFunctionPolynomial poly2Denominator
			) throws EPMCException {
		poly1Numerator = UtilValue.clone(poly1Numerator);
		poly1Denominator = UtilValue.clone(poly1Denominator);
		poly2Numerator = UtilValue.clone(poly2Numerator);
		poly2Denominator = UtilValue.clone(poly2Denominator);
        getTypePolynomial().cancelCommonFactors(poly1Numerator, poly2Denominator);
        getTypePolynomial().cancelCommonFactors(poly1Denominator, poly2Numerator);
        ValueFunctionPolynomial newNumerator = getTypePolynomial().newValue();
        ValueFunctionPolynomial newDenominator = getTypePolynomial().newValue();
        newNumerator.multiply(poly1Numerator, poly2Numerator);
        newDenominator.multiply(poly1Denominator, poly2Denominator);
        this.numerator.set(newNumerator);
        this.denominator.set(newDenominator);
	}
	
	void adjustNumParameters() {
		this.numerator.adjustNumParameters();
		this.denominator.adjustNumParameters();
	}
	
	TypeFunctionPolynomial getTypePolynomial() {
		return type.getTypePolynomial();
	}
	
	private void normalise() {
		getTypePolynomial().cancelCommonFactors(this.numerator, this.denominator);
	}
	
	@Override
	public String toString() {
		if (this.denominator.isOne()) {
			return this.numerator.toString();
		} else {
			return this.numerator + " / " + this.denominator;
		}
	}

	@Override
	public boolean isConstant() {
		return this.numerator.isConstant() && this.denominator.isConstant();
	}

	@Override
	public ValueReal getConstant() {
		assert isConstant();
		if (asConstant == null) {
			asConstant = getContextPARAM().newValueReal();
		}
		try {
			asConstant.divide(this.numerator.getConstant(), this.denominator.getConstant());
		} catch (EPMCException e) {
			assert false;
		}
		return asConstant;
	}
	
	ValueFunctionPolynomial getNumerator() {
		return numerator;
	}
	
	public ValueFunctionPolynomial getDenominator() {
		return denominator;
	}
	
	@Override
	public boolean equals(Object obj) {
		assert obj != null;
		if (!(obj instanceof ValueFunctionPolynomialFraction)) {
			return false;
		}
		ValueFunctionPolynomialFraction other = (ValueFunctionPolynomialFraction) obj;
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
        hash = this.numerator.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = this.denominator.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
	}
}
