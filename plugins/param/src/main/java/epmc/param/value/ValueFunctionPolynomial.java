package epmc.param.value;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.BitSet;

import epmc.error.EPMCException;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

public final class ValueFunctionPolynomial implements ValueFunction {
	private static final int NUM_IMPORT_VALUES = 2;
    private final ValueFunctionPolynomial importPolynomials[] = new ValueFunctionPolynomial[NUM_IMPORT_VALUES];
	private TypeFunctionPolynomial type;
	private boolean immutable;
	private int oldNumParameters;
	private int numTerms;
	private int[] monomials;
	private BigInteger[] coefficients;
	private ValueReal asConstant;
	private final BitSet neededParams = new BitSet();

	ValueFunctionPolynomial(TypeFunctionPolynomial type,
			int oldNumParameters,
			int numTerms,
			int[] monomials,
			BigInteger[] coefficients) {
		assert type != null;
		this.type = type;
		this.oldNumParameters = oldNumParameters;
		this.numTerms = numTerms;
		this.monomials = monomials;
		if (this.monomials == null) {
			this.monomials = new int[0];
		}
		this.coefficients = coefficients;
		if (this.coefficients == null) {
			this.coefficients = new BigInteger[0];
		}
	}
	
	ValueFunctionPolynomial(TypeFunctionPolynomial type) {
		this(type, type.getNumParameters(), 0, null, null);
	}

	@Override
	public int compareTo(Value o) {
		assert o != null;
		assert false;
		return -1;
	}

	@Override
	public ValueFunctionPolynomial clone() {
		adjustNumParameters();
		ValueFunctionPolynomial other = new ValueFunctionPolynomial(this.type);
		other.oldNumParameters = this.oldNumParameters;
		other.numTerms = this.numTerms;
		other.monomials = Arrays.copyOf(this.monomials, this.monomials.length);
		other.coefficients = this.coefficients.clone();
		return other;
	}

	@Override
	public TypeFunctionPolynomial getType() {
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
    public void set(Value op) {
        assert !isImmutable();
        assert op != null;
		adjustNumParameters();
        ValueFunctionPolynomial opPoly = castOrImport(op, 0);
    	this.numTerms = opPoly.numTerms;
    	this.monomials = Arrays.copyOf(opPoly.monomials, opPoly.monomials.length);
    	this.coefficients = opPoly.coefficients.clone();
    }

    @Override
    public void set(int value) {
		adjustNumParameters();
    	int numParameters = type.getNumParameters();
		if (value == 0) {
			this.numTerms = 0;
	    	this.monomials = new int[0];
	    	this.coefficients = new BigInteger[0];
		} else {
			this.numTerms = 1;
			this.monomials = new int[numParameters];
			this.coefficients = new BigInteger[1];
			this.coefficients[0] = BigInteger.valueOf(value);
		}
    }
    
    public void set(int oldNumParameters, int numTerms, int[] monomials,
    		BigInteger[] coefficients) {
    	assert oldNumParameters >= 0;
    	assert numTerms >= 0;
    	assert monomials != null;
    	assert coefficients != null;
    	this.oldNumParameters = oldNumParameters;
    	this.numTerms = numTerms;
    	this.monomials = monomials;
    	this.coefficients = coefficients;
    }
    
    @Override
    public void set(String value) {
    	assert value != null;
    	PolynomialParser.parse(this, value);
    }

    public void set(BigInteger value) {
    	assert value != null;
    	adjustNumParameters();
    	if (value.equals(BigInteger.ZERO)) {
        	this.numTerms = 0;
        	this.monomials = new int[0];
        	this.coefficients = new BigInteger[0];
    	} else {
        	this.numTerms = 1;
        	this.monomials = new int[oldNumParameters];
        	this.coefficients = new BigInteger[1];
        	this.coefficients[0] = value;
    	}
    }
    
    @Override
	public void evaluate(ValueReal result, Point point) throws EPMCException {
    	assert result != null;
    	assert ValueReal.isReal(result);
    	assert point != null;
		adjustNumParameters();
		point.adjustNumParameters();
		TypeReal typeReal = TypeReal.asReal(result.getType());
		ValueInteger exponent = TypeInteger.get(result.getType().getContext()).newValue();
		ValueReal currentParamPow = typeReal.newValue();
		ValueReal termValue = typeReal.newValue();
		ValueReal paramValue = typeReal.newValue();
		ValueReal.asReal(result).set(0);
		Value entry = type.getContextPARAM().getTypeReal().newValue();
		this.neededParams.clear();
		for (int termNr = 0; termNr < numTerms; termNr++) {
			for (int paramNr = 0; paramNr < oldNumParameters; paramNr++) {
				if (getExponent(paramNr, termNr) > 0) {
					this.neededParams.set(paramNr);
				}
			}
		}
		
		for (int paramNr = 0; paramNr < oldNumParameters; paramNr++) {
			Object parameter = type.getParameter(paramNr);
			if (this.neededParams.get(paramNr) &&
					(parameter instanceof Unevaluated)) {
				Unevaluated unevaluated = (Unevaluated) parameter;
				unevaluated.evaluate(entry, point);
				point.setDimension(entry, paramNr);
			}
		}
		for (int termNr = 0; termNr < numTerms; termNr++) {
			termValue.set(coefficients[termNr].toString());
			for (int paramNr = 0; paramNr < oldNumParameters; paramNr++) {
				point.getDimension(paramValue, paramNr);
				exponent.set(getExponent(paramNr, termNr));
				currentParamPow.pow(paramValue, exponent);
				termValue.multiply(termValue, currentParamPow);
			}
			result.add(result, termValue);
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
	
	@Override
	public void addInverse(Value operand) throws EPMCException {
		assert !immutable;
		adjustNumParameters();
		ValueFunctionPolynomial function = castOrImport(operand, 0);
		this.numTerms = function.numTerms;
		this.monomials = Arrays.copyOf(function.monomials, function.monomials.length);
		this.coefficients = new BigInteger[function.coefficients.length];
		for (int coeffNr = 0; coeffNr < function.coefficients.length; coeffNr++) {
			this.coefficients[coeffNr] = function.coefficients[coeffNr].negate();
		}
	}
	
	public void addOrSubtract(Value operand1, Value operand2,
			boolean subtract) throws EPMCException {
		assert !immutable;
		adjustNumParameters();
		ValueFunctionPolynomial function1 = castOrImport(operand1, 0);
		ValueFunctionPolynomial function2 = castOrImport(operand2, 1);
		int resultSize = computeAddOrSubNumMonomials(function1, function2, subtract);
		int term1Nr = 0;
		int term2Nr = 0;
		int resultIndex = 0;
		int[] resultMonomials = new int[this.oldNumParameters * resultSize];
		BigInteger[] resultCoefficients = new BigInteger[resultSize];
		while (term1Nr < function1.numTerms && term2Nr < function2.numTerms) {
			int monomCmp = compareMonomials(function1, function2, term1Nr, term2Nr);
			BigInteger coefficient1 = function1.coefficients[term1Nr];
			BigInteger coefficient2 = function2.coefficients[term2Nr];
			switch (monomCmp) {
			case 0:
				BigInteger resultCoefficient;
				if (subtract) {
					resultCoefficient = coefficient1.subtract(coefficient2);					
				} else {
					resultCoefficient = coefficient1.add(coefficient2);
				}
				if (!resultCoefficient.equals(BigInteger.ZERO)) {
					resultCoefficients[resultIndex] = resultCoefficient;
					for (int symbolNr = 0; symbolNr < this.oldNumParameters; symbolNr++) {
						resultMonomials[this.oldNumParameters * resultIndex + symbolNr] =
								function1.monomials[this.oldNumParameters * term1Nr + symbolNr];
					}
					resultIndex++;
				}
				term1Nr++;
				term2Nr++;
				break;
			case 1:
				resultCoefficients[resultIndex] = function1.coefficients[term1Nr];
				for (int symbolNr = 0; symbolNr < this.oldNumParameters; symbolNr++) {
					resultMonomials[this.oldNumParameters * resultIndex + symbolNr] =
							function1.monomials[this.oldNumParameters * term1Nr + symbolNr];
				}
				resultIndex++;
				term1Nr++;
				break;
			case -1:
				if (subtract) {
					resultCoefficients[resultIndex] = function2.coefficients[term2Nr].negate();					
				} else {
					resultCoefficients[resultIndex] = function2.coefficients[term2Nr];
				}
				for (int symbolNr = 0; symbolNr < this.oldNumParameters; symbolNr++) {
					resultMonomials[this.oldNumParameters * resultIndex + symbolNr] =
							function2.monomials[this.oldNumParameters * term2Nr + symbolNr];
				}
				resultIndex++;
				term2Nr++;
				break;
			}
		}

		while (term1Nr < function1.numTerms) {
			resultCoefficients[resultIndex] = function1.coefficients[term1Nr];
			for (int symbolNr = 0; symbolNr < this.oldNumParameters; symbolNr++) {
				resultMonomials[this.oldNumParameters * resultIndex + symbolNr] =
						function1.monomials[this.oldNumParameters * term1Nr + symbolNr];
			}
			resultIndex++;
			term1Nr++;
		}

		while (term2Nr < function2.numTerms) {
			resultCoefficients[resultIndex] = function2.coefficients[term2Nr];
			for (int symbolNr = 0; symbolNr < this.oldNumParameters; symbolNr++) {
				resultMonomials[this.oldNumParameters * resultIndex + symbolNr] =
						function2.monomials[this.oldNumParameters * term2Nr + symbolNr];
			}
			resultIndex++;
			term2Nr++;
		}
		this.coefficients = resultCoefficients;
		this.monomials = resultMonomials;
		this.numTerms = resultIndex;
	}
	
	private int computeAddOrSubNumMonomials(ValueFunctionPolynomial function1,
			ValueFunctionPolynomial function2, boolean subtract) {
		int term1Nr = 0;
		int term2Nr = 0;
		int resultSize = 0;
		while (term1Nr < function1.numTerms && term2Nr < function2.numTerms) {
			int monomCmp = compareMonomials(function1, function2, term1Nr, term2Nr);
			switch (monomCmp) {
			case 0:
				BigInteger coefficient1 = function1.coefficients[term1Nr];
				BigInteger coefficient2 = function2.coefficients[term2Nr];
				BigInteger addTo = subtract ? coefficient2 : coefficient2.negate();
				if (!coefficient1.equals(addTo)) {
					resultSize++;
				}
				term1Nr++;
				term2Nr++;
				break;
			case 1:
				resultSize++;
				term1Nr++;
				break;
			case -1:
				resultSize++;
				term2Nr++;
				break;
			default:
				assert false;
			}
		}
		resultSize += function1.numTerms - term1Nr;
		resultSize += function2.numTerms - term2Nr;
		return resultSize;
	}

	private int compareMonomials(ValueFunctionPolynomial function1,
			ValueFunctionPolynomial function2, int term1Nr, int term2Nr) {
		int result = 0;
		for (int symbolNr = 0; symbolNr < this.oldNumParameters; symbolNr++) {
			int index1 = term1Nr * this.oldNumParameters + symbolNr;
			int index2 = term2Nr * this.oldNumParameters + symbolNr;
			if (function1.monomials[index1] > function2.monomials[index2]) {
				result = 1;
				break;
			} else if (function1.monomials[index1] < function2.monomials[index2]) {
				result = -1;
				break;
			}
		}
		return result;
	}

	public void adjustNumParameters() {
		int newNumParameters = type.getNumParameters();
		assert oldNumParameters >= 0;
		assert newNumParameters >= 0;
		assert oldNumParameters <= newNumParameters;
		if (oldNumParameters == newNumParameters) {
			return;
		}
		int[] newMonomials = new int[numTerms * newNumParameters];
		for (int termNr = 0; termNr < numTerms; termNr++) {
			for (int paramNr = 0; paramNr < oldNumParameters; paramNr++) {
				newMonomials[termNr * newNumParameters + paramNr]
						= this.monomials[termNr * oldNumParameters + paramNr];
			}
		}
		this.monomials = newMonomials;
		this.oldNumParameters = newNumParameters;
	}
	
	void setParameter(int parameterNumber) {
		adjustNumParameters();
		assert parameterNumber >= 0 : parameterNumber;
		assert parameterNumber < oldNumParameters : parameterNumber + " " + oldNumParameters;
		this.numTerms = 1;
		this.monomials = new int[oldNumParameters];
		this.coefficients = new BigInteger[1];
		this.coefficients[0] = BigInteger.ONE;
		this.monomials[parameterNumber] = 1;
	}
	
	@Override
	public ValueFunctionPolynomial castOrImport(Value operand, int number) {
	    assert operand != null;
	    assert number >= 0;
	    assert number < NUM_IMPORT_VALUES;
	    if (operand instanceof ValueFunctionPolynomial) {
	    	ValueFunctionPolynomial result = (ValueFunctionPolynomial) operand;
	    	result.adjustNumParameters();
	    	return result;
	    } else if (ValueInteger.isInteger(operand)) {
	        if (importPolynomials[number] == null) {
	            importPolynomials[number] = getType().newValue();
	        }
	        importPolynomials[number].set(ValueInteger.asInteger(operand).getInt());
	        return (ValueFunctionPolynomial) importPolynomials[number];
	    } else {
	        assert false : operand + " " + operand.getType();
	        return null;
	    }
	}

	@Override
	public String toString() {
		adjustNumParameters();
		if (numTerms == 0) {
			return "0";
		}
		if (isConstant()) {
			return coefficients[0].toString();
		}
		StringBuilder builder = new StringBuilder();
		for (int termNr = 0; termNr < this.numTerms; termNr++) {
			BigInteger coefficent = this.coefficients[termNr];
			assert !coefficients.equals(BigInteger.ZERO);
			boolean hasExponent = false;
			for (int paramNr = 0; paramNr < this.oldNumParameters; paramNr++) {
				int monomial = this.monomials[termNr * oldNumParameters + paramNr];
				if (monomial != 0) {
					hasExponent = true;
					break;
				}
			}
			boolean writeTimes = hasExponent;
			if (!(coefficent.equals(BigInteger.ONE) || coefficent.equals(BigInteger.ONE.negate())) || !hasExponent) {
				builder.append(coefficent);
			} else {
				writeTimes = false;
			}
			if (coefficent.equals(BigInteger.ONE.negate()) && hasExponent) {
				builder.append("-");
			}
			if (writeTimes) {
				builder.append("*");
			}
			for (int paramNr = 0; paramNr < this.oldNumParameters; paramNr++) {
				int monomial = this.monomials[termNr * oldNumParameters + paramNr];
				if (monomial != 0) {
					Object parameter = this.type.getParameter(paramNr);
					builder.append(parameter);
					if (monomial != 1) {
						builder.append("^");
						builder.append(monomial);
					}
					if (paramNr < this.oldNumParameters - 1) {
						builder.append("*");
					}
				}
			}
			if (builder.charAt(builder.length() - 1) == '*') {
				builder.setLength(builder.length() - 1);
			}
			if (termNr < this.numTerms - 1) {
				builder.append("+");
			}
		}
		return builder.toString();
	}

	@Override
	public boolean isConstant() {
		if (this.numTerms == 0) {
			return true;
		}
		if (this.numTerms > 1) {
			return false;
		}
		for (int paramNr = 0; paramNr < oldNumParameters; paramNr++) {
			if (this.monomials[paramNr] != 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void setParameter(Object parameter) {
		assert parameter != null;
		int parameterNumber = this.type.getParameterNumber(parameter);
		setParameter(parameterNumber);
	}
	
	public int getNumTerms() {
		return numTerms;
	}
	
	void multByTerm(ValueFunctionPolynomial p1, ValueFunctionPolynomial p2, int whichTerm) {
		adjustNumParameters();
		p1.adjustNumParameters();
		p2.adjustNumParameters();
		int numTerms = p1.getNumTerms();
		int[] monomials = new int[oldNumParameters * numTerms];
		BigInteger[] coefficients = new BigInteger[numTerms];
		for (int termNr = 0; termNr < p1.getNumTerms(); termNr++) {
			coefficients[termNr] = p1.coefficients[termNr].multiply(p2.coefficients[whichTerm]);
			for (int symbolNr = 0; symbolNr < oldNumParameters; symbolNr++) {
				monomials[oldNumParameters * termNr + symbolNr] =
						p1.monomials[oldNumParameters * termNr + symbolNr] +
						p2.monomials[oldNumParameters * whichTerm + symbolNr];
			}
		}
		
		this.numTerms = numTerms;
		this.monomials = monomials;
		this.coefficients = coefficients;
	}
	
	@Override
	public void multiply(Value operand1, Value operand2)
			throws EPMCException {
		adjustNumParameters();
		ValueFunctionPolynomial function1 = castOrImport(operand1, 0);
		ValueFunctionPolynomial function2 = castOrImport(operand2, 1);
		Geobucket bucket = new Geobucket(type);
		int numTerms = function2.getNumTerms();
		for (int termNr = 0; termNr < numTerms; termNr++) {
			ValueFunctionPolynomial multiplied = type.newValue();
			multiplied.multByTerm(function1, function2, termNr);
			bucket.add(multiplied);
		}
		set(bucket.canonicalise());
	}

	public int getNumParameters() {
		adjustNumParameters();
		return oldNumParameters;
	}

	public void resize(int numParameters, int numTerms) {
		assert numParameters >= 0;
		assert numTerms >= 0;
		this.oldNumParameters = numParameters;
		this.numTerms = numTerms;
		this.monomials = new int[numParameters * numTerms];
		this.coefficients = new BigInteger[numTerms];
	}

	public void resize(int numTerms) {
		assert numTerms >= 0;
		adjustNumParameters();
		resize(this.oldNumParameters, numTerms);
	}

	public BigInteger getCoefficient(int termNr) {
		assert termNr >= 0 : termNr;
		assert termNr < coefficients.length : termNr + " " + coefficients.length;
		return coefficients[termNr];
	}
	
	public int getExponent(int paramNr, int termNr) {
		assert termNr >= 0 : termNr;
		assert termNr < this.numTerms : termNr;
		assert paramNr >= 0 : paramNr;
		assert paramNr < this.oldNumParameters : paramNr;
		return this.monomials[this.oldNumParameters * termNr + paramNr];		
	}

	public void setCoefficient(int termNr, BigInteger coefficient) {
		assert termNr >= 0 : termNr;
		assert termNr < coefficients.length : termNr + " " + coefficients.length;
		assert coefficient != null;
		coefficients[termNr] = coefficient;
	}
	
	public void setCoefficient(int termNr, String coefficient) {
		assert termNr >= 0 : termNr;
		assert termNr < coefficients.length : termNr + " " + coefficients.length;
		assert coefficient != null;
		setCoefficient(termNr, new BigInteger(coefficient));
	}
	
	public void setExponent(int paramNr, int termNr, int exponent) {
		assert termNr >= 0 : termNr;
		assert termNr < this.numTerms : termNr;
		assert paramNr >= 0 : paramNr;
		assert paramNr < this.oldNumParameters : paramNr;
		assert exponent >= 0 : exponent;
		this.monomials[this.oldNumParameters * termNr + paramNr] = exponent;		
	}

	public void setExponent(String parameter, int termNr, int exponent) {
		assert termNr >= 0 : termNr;
		assert termNr < this.numTerms : termNr;
		assert parameter != null;
		assert type.isParameter(parameter);
		assert exponent >= 0 : exponent;
		int paramNr = type.getParameterNumber(parameter);
		this.setExponent(paramNr, termNr, exponent);
	}

	public int[] getMonomials() {
		return monomials;
	}
	
	public BigInteger[] getCoefficients() {
		return coefficients;
	}

	@Override
	public boolean isZero() {
		return this.numTerms == 0;
	}
	
	@Override
	public boolean isOne() {
		if (this.numTerms != 1) {
			return false;
		}
		if (!this.coefficients[0].equals(BigInteger.ONE)) {
			return false;
		}
		for (int paramNr = 0; paramNr < this.oldNumParameters; paramNr++) {
			if (this.monomials[paramNr] != 0) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ValueReal getConstant() throws EPMCException {
		assert isConstant();
		if (asConstant == null) {
			asConstant = getContextPARAM().newValueReal();
		}
		if (coefficients.length == 0) {
			asConstant.set(0);
		} else {
			asConstant.set(coefficients[0].toString());
		}
		return asConstant;
	}
	
	@Override
	public boolean equals(Object obj) {
		assert obj != null;
		if (!(obj instanceof ValueFunctionPolynomial)) {
			return false;
		}
		ValueFunctionPolynomial other = (ValueFunctionPolynomial) obj;
		this.adjustNumParameters();
		other.adjustNumParameters();
		if (this.numTerms != other.numTerms) {
			return false;
		}
		for (int termNr = 0; termNr < numTerms; termNr++) {
			if (!this.coefficients[termNr].equals(other.coefficients[termNr])) {
				return false;
			}
			for (int paramNr = 0; paramNr < oldNumParameters; paramNr++) {
				if (this.monomials[oldNumParameters * termNr + paramNr]
						!= other.monomials[oldNumParameters * termNr + paramNr]) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		adjustNumParameters();
        int hash = 0;
        hash = numTerms + (hash << 6) + (hash << 16) - hash;
        for (int termNr = 0; termNr < numTerms; termNr++) {
        	hash = coefficients[termNr].hashCode() + (hash << 6) + (hash << 16) - hash;
        	for (int paramNr = 0; paramNr < oldNumParameters; paramNr++) {
        		int exp = this.monomials[oldNumParameters * termNr + paramNr];
        		hash = exp + (hash << 6) + (hash << 16) - hash;
        	}
        }
        return hash;
	}
	
	public BitSet getNeededParams() {
		this.neededParams.clear();
		for (int termNr = 0; termNr < numTerms; termNr++) {
			for (int paramNr = 0; paramNr < oldNumParameters; paramNr++) {
				if (getExponent(paramNr, termNr) > 0) {
					this.neededParams.set(paramNr);
				}
			}
		}
		return neededParams;
	}

	@Override
	public void divide(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void multInverse(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
