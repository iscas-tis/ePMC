package epmc.param.value.polynomial;

import java.math.BigInteger;

import epmc.param.value.ValueFunction;
import epmc.value.Value;
import epmc.value.ValueSetString;

public final class ValuePolynomial implements ValueFunction, ValueSetString {
    private final TypePolynomial type;
    private int oldNumParameters;
    private int numTerms;
    private int[] monomials;
    private BigInteger[] coefficients;

    public static boolean is(Value value) {
        return value instanceof ValuePolynomial;
    }

    public static ValuePolynomial as(Value value) {
        if (is(value)) {
            return (ValuePolynomial) value;
        } else {
            return null;
        }
    }

    ValuePolynomial(TypePolynomial type) {
        assert type != null;
        this.type = type;
        oldNumParameters = type.getParameterSet().getNumParameters();
        numTerms = 0;
        monomials = new int[0];
        coefficients = new BigInteger[0];
    }

    @Override
    public TypePolynomial getType() {
        return type;
    }

    @Override
    public void set(int value) {
        adjustNumParameters();
        if (value == 0) {
            this.numTerms = 0;
            this.monomials = new int[0];
            this.coefficients = new BigInteger[0];
        } else {
            this.numTerms = 1;
            this.monomials = new int[oldNumParameters];
            this.coefficients = new BigInteger[1];
            this.coefficients[0] = BigInteger.valueOf(value);
        }
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

    public void adjustNumParameters() {
        int newNumParameters = type.getParameterSet().getNumParameters();
        assert oldNumParameters >= 0;
        assert newNumParameters >= 0;
        assert oldNumParameters <= newNumParameters;
        if (oldNumParameters == newNumParameters) {
            return;
        }
        int[] newMonomials = new int[numTerms * newNumParameters];
        for (int termNr = 0; termNr < numTerms; termNr++) {
            for (int paramNr = 0; paramNr < oldNumParameters; paramNr++) {
                newMonomials[termNr * newNumParameters + paramNr] = this.monomials[termNr * oldNumParameters + paramNr];
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
                    Object parameter = this.type.getParameterSet().getParameter(paramNr);
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

    private boolean isConstant() {
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
    public void setParameter(String parameter) {
        assert parameter != null;
        int parameterNumber = this.type.getParameterSet().getParameterNumber(parameter);
        setParameter(parameterNumber);
    }

    public void setNumTerms(int numTerms) {
        this.numTerms = numTerms;
    }

    public int getNumTerms() {
        return numTerms;
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
        this.numTerms = numTerms;
        this.monomials = new int[oldNumParameters * numTerms];
        this.coefficients = new BigInteger[numTerms];
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

    public int[] getMonomials() {
        return monomials;
    }

    public void setMonomials(int[] monomials) {
        this.monomials = monomials;
    }

    public BigInteger[] getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(BigInteger[] coefficients) {
        this.coefficients = coefficients;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValuePolynomial)) {
            return false;
        }
        ValuePolynomial other = (ValuePolynomial) obj;
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
                if (this.monomials[oldNumParameters * termNr + paramNr] != other.monomials[oldNumParameters * termNr
                        + paramNr]) {
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

    public boolean isNumerical() {
        if (numTerms == 0) {
            return true;
        }
        if (numTerms != 1) {
            return false;
        }
        for (int param = 0; param < oldNumParameters; param++) {
            if (this.monomials[param] != 0) {
                return false;
            }
        }
        return true;
    }
    
    public BigInteger getBigInteger() {
        assert isNumerical();
        if (numTerms == 0) {
            return BigInteger.ZERO;
        } else {
            return coefficients[0];
        }
    }
}
