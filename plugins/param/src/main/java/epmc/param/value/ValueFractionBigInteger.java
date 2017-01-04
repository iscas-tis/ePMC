package epmc.param.value;

import java.math.BigInteger;
import java.math.RoundingMode;

import epmc.error.EPMCException;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;

final class ValueFractionBigInteger extends ValueFraction {
	private final static String INVALID = "invalid";
	private final static String ZERO = "0";
	private final static String MINUS = "-";
	private final static String NO_SIGNUM = "";
	private final static String MZERO = "-0";
	private final static String INF = "inf";
	private final static String MINF = "-inf";
	private final static String DIVIDE = "/";
	private final static String DOT = ".";
	private final static String REGEXP_DOT = "\\.";
	private final static String SPACE = " ";
	
	private static final int NUM_IMPORT_VALUES = 2;
    private final static int DOUBLE_MANTISSA_BITS = 52;
//    private final static int DOUBLE_EXPONENT_BITS = 11;
    private final static int DOUBLE_BIAS = 1023;
    private final static long DOUBLE_MANTISSA_MASK = 0xfffffffffffffl;
    private final static BigInteger BI_ZERO = BigInteger.ZERO;
    private final static BigInteger BI_ONE = BigInteger.ONE;
    private final static BigInteger BI_MONE = BigInteger.ONE.negate();
    private final static BigInteger BI_TEN = BigInteger.TEN;
    private final static BigInteger BI_TWO = BigInteger.ONE.add(BigInteger.ONE);
    
    private final ValueFractionBigInteger importPolynomials[] = new ValueFractionBigInteger[NUM_IMPORT_VALUES];
    private BigInteger numerator;
    private BigInteger denominator;
    private TypeFractionBigInteger type;
    private boolean immutable;
    
    ValueFractionBigInteger(TypeFractionBigInteger type, BigInteger num, BigInteger den) {
        this.type = type;
        assert num != null;
        assert den != null;
        this.numerator = num;
        this.denominator = den;
        normalise();
    }
    
    ValueFractionBigInteger(TypeFractionBigInteger type) {
        this(type, BI_ZERO, BI_ZERO);
    }

    private void normalise() {
        if (denominator.equals(BI_ZERO)) {
            int compare = numerator.compareTo(BI_ZERO);
            if (compare < 0) {
                numerator = BI_MONE;
            } else if (compare > 0) {
                numerator = BI_ONE;
            } else {
                numerator = BI_ZERO;
            }
            numerator = BI_ZERO;
        } else {
            BigInteger gcd = numerator.gcd(denominator);
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
            if (denominator.compareTo(BI_ZERO) < 0) {
                numerator = numerator.negate();
                denominator = denominator.negate();
            }
        }
    }

    @Override
    public ValueFractionBigInteger clone() {
        return new ValueFractionBigInteger(getType(), numerator, denominator);
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueFractionBigInteger)) {
            return false;
        }
        ValueFractionBigInteger other = (ValueFractionBigInteger) obj;
        return this.numerator.equals(other.numerator) && this.denominator.equals(other.denominator);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = numerator.hashCode() + (hash << 6) + (hash << 16) - hash;
        hash = denominator.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (isInvalid()) {
            builder.append(INVALID);
        } else if (isPosZero()) {
            builder.append(ZERO);
        } else if (isNegZero()) {
            builder.append(MZERO);
        } else if (isPosInf()) {
            builder.append(INF);
        } else if (isNegInf()) {
            builder.append(MINF);
        } else if (denominator.equals(BI_ONE)) {
            builder.append(numerator);
        } else {
            builder.append(numerator);
            builder.append(DIVIDE);
            builder.append(denominator);
        }
        return builder.toString();
    }
    
    @Override
    public boolean isPosInf() {
        return denominator.equals(BI_ZERO) && numerator.equals(BI_ONE);
    }

    @Override
    public boolean isNegInf() {
        return denominator.equals(BI_ZERO) && numerator.equals(BI_MONE);
    }

    public boolean isInvalid() {
        return denominator.equals(BI_ZERO) && numerator.equals(BI_ZERO);
    }
    
    private void setPosInf() {
        numerator = BI_ONE;
        denominator = BI_ZERO;
    }

    private void setNegInf() {
        numerator = BI_MONE;
        denominator = BI_ZERO;
    }
    
    private void setInvalid() {
        numerator = BI_ZERO;
        denominator = BI_ZERO;
    }

    private void setPosZero() {
        numerator = BI_ONE;
        denominator = BI_ZERO;        
    }

    private void setNegZero() {
        numerator = BI_MONE;
        denominator = BI_ZERO;        
    }

    @Override
    public void add(Value operand1, Value operand2) throws EPMCException {
    	ValueFractionBigInteger fraction1 = castOrImport(operand1, 0);
    	ValueFractionBigInteger fraction2 = castOrImport(operand2, 1);
        if (fraction1.isInvalid() || fraction2.isInvalid()
                || (fraction1.isPosInf() && fraction2.isNegInf())
                || (fraction1.isNegInf() && fraction2.isPosInf())) {
            setInvalid();
        } else if (fraction1.isPosInf() || fraction2.isPosInf()) {
            setPosInf();
        } else if (fraction1.isNegInf() || fraction2.isNegInf()) {
            setNegInf();
        } else if (fraction1.isNegZero() && fraction2.isNegZero()) {
            setNegZero();
        } else {
            BigInteger num = fraction1.numerator.multiply(fraction2.denominator)
                    .add(fraction2.numerator.multiply(fraction1.denominator));
            BigInteger den = fraction1.denominator.multiply(fraction2.denominator);
            this.numerator = num;
            this.denominator = den;
            normalise();
        }
    }
    
    @Override
    public void subtract(Value operand1, Value operand2)
            throws EPMCException {
    	ValueFractionBigInteger fraction1 = castOrImport(operand1, 0);
    	ValueFractionBigInteger fraction2 = castOrImport(operand2, 1);
        if (fraction1.isInvalid() || fraction2.isInvalid()
                || (fraction1.isPosInf() && fraction2.isPosInf())
                || (fraction1.isNegInf() && fraction2.isNegInf())) {
            setInvalid();
        } else if (fraction1.isPosInf() || fraction2.isNegInf()) {
            setPosInf();
        } else if (fraction1.isNegInf() || fraction2.isPosInf()) {
            setNegInf();
        } else if (fraction1.isNegZero() && fraction2.isPosZero()) {
            setNegZero();
        } else {
            BigInteger num = fraction1.numerator.multiply(fraction2.denominator)
                    .subtract(fraction2.numerator.multiply(fraction1.denominator));
            BigInteger den = fraction1.denominator.multiply(fraction2.denominator);
            this.numerator = num;
            this.denominator = den;
            normalise();
        }
    }
    
    @Override
    public void addInverse(Value operand) throws EPMCException {
        assert operand != null;
    	ValueFractionBigInteger fraction = castOrImport(operand, 0);
        if (fraction.isZero()) {
            this.numerator = BI_ZERO;
            this.denominator = fraction.denominator.negate();
        } else {
            this.numerator = fraction.numerator.negate();
            this.denominator = fraction.denominator;
        }
    }
    
    @Override
    public void abs(Value operand) throws EPMCException {
        assert operand != null;
    	ValueFractionBigInteger fraction = castOrImport(operand, 0);
        if (fraction.isNegZero()) {
            setPosZero();
        } else {
            int compare = fraction.numerator.compareTo(BI_ZERO);
            this.numerator = compare < 0 ? fraction.numerator.negate() : fraction.numerator;
            this.denominator = fraction.denominator;
        }
    }

    @Override
    public int ceilInt() {
        BigInteger[] div = numerator.divideAndRemainder(denominator);
        if (!div[1].equals(BI_ZERO)) {
            div[0] = div[0].add(BI_ONE);
        }
        return div[0].intValue();
        // TODO ??
    }
    
    @Override
    public void multInverse(Value operand) throws EPMCException {
    	ValueFractionBigInteger fraction = castOrImport(operand, 0);
        if (fraction.isZero()) {
            setInvalid();
        } else if (fraction.isPosInf()) {
            setPosZero();
        } else if (fraction.isNegInf()) {
            setNegInf();
        } else {
            BigInteger num = fraction.denominator;
            BigInteger den = fraction.numerator;
            this.numerator = num;
            this.denominator = den;
        }
    }
    
    private double[] toDouble(RoundingMode rounding) {
        double[] result = new double[2];
        if (isInvalid()) {
            result[0] = Double.NaN;
            result[1] = Double.NaN;
        } else if (isPosInf()) {
            result[0] = Double.POSITIVE_INFINITY;
            result[1] = Double.POSITIVE_INFINITY;
        } else if (isNegInf()) {
            result[0] = Double.NEGATIVE_INFINITY;
            result[1] = Double.NEGATIVE_INFINITY;
        } else if (isPosZero()) {
            result[0] = 0.0;
            result[1] = 0.0;
        } else if (isNegZero()) {
            result[0] = -0.0;
            result[1] = -0.0;
        } else {
        	result = toDoubleGeneral(rounding);
        }
        return result;
    }

    private double[] toDoubleGeneral(RoundingMode rounding) {
        // TODO check overflow etc.
        double[] result = new double[2];
        int signInt = numerator.signum();
        BigInteger absoluteNumerator = signInt >= 0 ? numerator : numerator.negate();
        int numBits = absoluteNumerator.bitLength();
        int denBits = denominator.bitLength();
        int shiftNumBy = DOUBLE_MANTISSA_BITS + 1 + denBits - numBits;
        BigInteger numShifted = absoluteNumerator.shiftLeft(shiftNumBy);
        BigInteger[] divRem = numShifted.divideAndRemainder(denominator);
        int actualBitLength = divRem[0].bitLength();
        shiftNumBy -= actualBitLength - (DOUBLE_MANTISSA_BITS + 1);
        numShifted = absoluteNumerator.shiftLeft(shiftNumBy);
        divRem = numShifted.divideAndRemainder(denominator);
        assert divRem[0].bitLength() == DOUBLE_MANTISSA_BITS + 1;
        int round1 = rounding == null ? -1 : 0;
        if (!divRem[1].equals(BI_ZERO) && rounding != null) {
            switch (rounding) {
            case CEILING:
                round1 = 1;
                break;
            case DOWN:
                round1 = -1;
                break;
            case FLOOR:
                round1 = 1;
                break;
            case HALF_DOWN:
                divRem[1] = divRem[1].multiply(BI_TWO);
                round1 = numShifted.compareTo(divRem[1]);
                if (round1 == 0) {
                    round1 = -1;
                }
                break;
            case HALF_EVEN:
                divRem[1] = divRem[1].multiply(BI_TWO);
                round1 = numShifted.compareTo(divRem[1]);
                if (round1 == 0) {
                    round1 = divRem[0].testBit(0) ? 1 : -1;
                }
                break;
            case HALF_UP:
                divRem[1] = divRem[1].multiply(BI_TWO);
                round1 = numShifted.compareTo(divRem[1]);
                if (round1 == 0) {
                    round1 = 1;
                }
                break;
            case UNNECESSARY:
                throw new ArithmeticException();
            case UP:
                round1 = 1;
                break;
            default:
                assert false;
                break;
            }
        }
        if (signInt == -1) {
        	round1 = -round1;
        }
        BigInteger divBig1 = divRem[0];
        divBig1 = round1 == 1 ? divBig1.add(BI_ONE) : divBig1;
        int shiftNumBy1 = shiftNumBy;
        if (divBig1.bitLength() > DOUBLE_MANTISSA_BITS + 1) {
            divBig1.shiftRight(1);
            shiftNumBy1--;
        }
        long div1 = divBig1.longValue();
        
        long sign = 0L;
        
        long mantissa1 = div1 & DOUBLE_MANTISSA_MASK;
        long exponent1 = (DOUBLE_BIAS - shiftNumBy1 + DOUBLE_MANTISSA_BITS + 0l)
                << (DOUBLE_MANTISSA_BITS - 0l);
        long value1 = sign | exponent1 | mantissa1;
        result[0] = Double.longBitsToDouble(value1);

        if (rounding == null) {
            divRem[1] = divRem[1].multiply(BI_TWO);
            int round2 = divRem[1].equals(BI_ZERO) ? 0 : 1;
            BigInteger divBig2 = divRem[0];
            divBig2 = round2 == 1 ? divBig2.add(BI_ONE) : divBig2;
            int shiftNumBy2 = shiftNumBy;
            if (divBig2.bitLength() > DOUBLE_MANTISSA_BITS + 1) {
                divBig2.shiftRight(1);
                shiftNumBy2--;
            }
            long div2 = divBig2.longValue();
            long mantissa2 = div2 & DOUBLE_MANTISSA_MASK;
            long exponent2 = (DOUBLE_BIAS - shiftNumBy2 + DOUBLE_MANTISSA_BITS + 0l)
                    << (DOUBLE_MANTISSA_BITS - 0l);
            long value2 = sign | exponent2 | mantissa2;
            result[1] = Double.longBitsToDouble(value2);
            if (signInt == -1) {
            	result[0] = -result[0];
            	result[1] = -result[1];
            }
        }
        return result;
	}

	private double toDouble() {
        return toDouble(null)[0];
    }

    @Override
    public double getDouble() {
        return toDouble();
    }
    
    @Override
    public void multiply(Value operand1, Value operand2)
            throws EPMCException {
    	ValueFractionBigInteger fraction1 = castOrImport(operand1, 0);
    	ValueFractionBigInteger fraction2 = castOrImport(operand2, 1);
        BigInteger num = fraction1.numerator.multiply(fraction2.numerator);
        BigInteger den = fraction1.denominator.multiply(fraction2.denominator);
        this.numerator = num;
        this.denominator = den;
        normalise();
    }
    
    @Override
    public void divide(Value operand1, Value operand2) throws EPMCException {
    	ValueFractionBigInteger fraction1 = castOrImport(operand1, 0);
    	ValueFractionBigInteger fraction2 = castOrImport(operand2, 1);
        BigInteger num = fraction1.numerator.multiply(fraction2.denominator);
        BigInteger den = fraction1.denominator.multiply(fraction2.numerator);
        this.numerator = num;
        this.denominator = den;
        normalise();
    }
    
    @Override
    public void pow(Value operand1, Value operand2) throws EPMCException {
    	assert operand1 != null;
    	assert operand2 != null;
    	assert ValueInteger.isInteger(operand2);
    	set(operand1);
    	numerator = numerator.pow(ValueInteger.asInteger(operand2).getInt());
    	denominator = denominator.pow(ValueInteger.asInteger(operand2).getInt());
    }
    
    @Override
    public int floorInt() {
    	return (int) Math.floor(numerator.doubleValue() / denominator.doubleValue());
    }
    
    @Override
    public boolean isEq(Value operand) throws EPMCException {
    	return compareTo(operand) == 0;
    }
    
    @Override
    public boolean isLt(Value operand) throws EPMCException {
    	return compareTo(operand) < 0;
    }
    
    @Override
    public boolean isGt(Value operand) throws EPMCException {
    	return compareTo(operand) > 0;
    }
    
    @Override
    public boolean isGe(Value operand) throws EPMCException {
    	return compareTo(operand) >= 0;
    }
    
    @Override
    public boolean isLe(Value operand) throws EPMCException {
    	return compareTo(operand) <= 0;
    }
    
    @Override
    public boolean isOne() {
        return this.numerator.equals(BI_ONE) && this.denominator.equals(BI_ONE);
    }
    
    @Override
    public boolean isZero() {
        return this.numerator.equals(BI_ZERO) && !this.denominator.equals(BI_ZERO);
    }

    private boolean isPosZero() {
        return this.numerator.equals(BI_ZERO) && this.denominator.equals(BI_ONE);
    }
    
    private boolean isNegZero() {
        return this.numerator.equals(BI_ZERO) && this.denominator.equals(BI_MONE);
    }
    
    @Override
    public void set(String value) {
    	// TODO fix following
        assert value != null;
        value = value.trim();
        if (value.contains(DIVIDE)) {
            String[] numDen = value.split(DIVIDE);
            assert numDen.length == 2;
            numDen[0] = numDen[0].trim();
            numDen[1] = numDen[1].trim();
            this.numerator = new BigInteger(numDen[0], 10);
            this.denominator = new BigInteger(numDen[1], 10);
        } else {
        	int expPos = -1;
        	int exp = 0;
        	boolean negate = false;
        	if (value.charAt(0) == '-') {
        		negate = true;
        		value = value.substring(1);
        	} else if (value.charAt(0) == '+') {
        		value = value.substring(1);        		
        	}
        	if (value.contains("e")) {
        		expPos = value.indexOf('e');
        	} else if (value.contains("E")) {
        		expPos = value.indexOf('E');
        	}
        	if (expPos != -1) {
        		String expString = value.substring(expPos + 1, value.length());
        		exp = Integer.parseInt(expString);
        		value = value.substring(0, expPos);
        	}
        	int pow = -exp;
        	if (value.contains(DOT)) {
        		pow += value.length() - 1 - value.indexOf('.');
        		String[] parts = value.split(REGEXP_DOT);
        		assert parts.length == 2;
        		value = parts[0] + parts[1];
        	}
            
            this.numerator = new BigInteger((negate ? MINUS : NO_SIGNUM) + value);
            if (pow >= 0) {
            	this.denominator = BI_TEN.pow(pow);
            } else {
            	BigInteger powTen = BI_TEN.pow(-pow);
            	this.numerator = this.numerator.multiply(powTen);
            	this.denominator = BI_ONE;
            }
        }
        normalise();
    }
    
    @Override
    public void set(int value) {
    	this.numerator = BigInteger.valueOf(value);
    	this.denominator = BI_ONE;
    }
    
    @Override
    public void set(Value value) {
    	assert value != null;
    	if (value instanceof ValueFractionBigInteger) {
    		ValueFractionBigInteger other = (ValueFractionBigInteger) value;
    		this.numerator = other.numerator;
    		this.denominator = other.denominator;
    	} else if (ValueInteger.isInteger(value)) {
    		set(ValueInteger.asInteger(value).getInt());
    	} else if (ValueReal.isReal(value)) {
    		set(value.toString());
    	}
    }

    @Override
    public TypeFractionBigInteger getType() {
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
    
	private ValueFractionBigInteger castOrImport(Value operand, int number) {
	    assert operand != null;
	    assert number >= 0;
	    assert number < NUM_IMPORT_VALUES;
	    if (operand instanceof ValueFractionBigInteger) {
	    	ValueFractionBigInteger result = (ValueFractionBigInteger) operand;
	    	return result;
	    } else if (ValueInteger.isInteger(operand)) {
	        if (importPolynomials[number] == null) {
	            importPolynomials[number] = (ValueFractionBigInteger) getType().newValue();
	        }
	        importPolynomials[number].set(ValueInteger.asInteger(operand).getInt());
	        return importPolynomials[number];
	    } else {
	        assert false : operand + SPACE + operand.getType();
	        return null;
	    }
	}

	@Override
	public int compareTo(Value operand) {
		ValueFractionBigInteger fraction = castOrImport(operand, 0);
		BigInteger left = this.numerator.multiply(fraction.denominator);
		BigInteger right = fraction.numerator.multiply(this.denominator);
		return left.compareTo(right);
	}
	
	void setNumerator(BigInteger num) {
		assert num != null;
		this.numerator = num;
	}

	void setNumerator(int num) {
		setNumerator(BigInteger.valueOf(num));
	}
	
	void setDenominator(BigInteger den) {
		assert den != null;
		this.denominator = den;
	}
	
	void setDenominator(int den) {
		setDenominator(BigInteger.valueOf(den));
	}
	
	BigInteger getNumerator() {
		return this.numerator;
	}
	
	BigInteger getDenominator() {
		return this.denominator;
	}

	@Override
	public int signInt() throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int intcastInt() throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void set(double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exp(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void log(Value operand1, Value operand2) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sqrt(Value operand) throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pi() throws EPMCException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getInt() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double norm() throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double distance(Value other) throws EPMCException {
		// TODO Auto-generated method stub
		return 0;
	}
}
