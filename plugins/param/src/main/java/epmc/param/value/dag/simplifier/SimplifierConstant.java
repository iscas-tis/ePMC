package epmc.param.value.dag.simplifier;

import java.math.BigInteger;

import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;

public final class SimplifierConstant implements Simplifier {
    public final static class Builder implements Simplifier.Builder {
        private Dag dag;

        @Override
        public Builder setDag(Dag dag) {
            this.dag = dag;
            return this;
        }

        @Override
        public Simplifier build() {
            return new SimplifierConstant(this);
        }
        
    }

    private final Dag dag;
    private OperatorType type;
    private int operandLeft;
    private int operandRight;
    private int num;
    private int den;

    private SimplifierConstant(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        dag = builder.dag;
    }

    @Override
    public void setType(OperatorType type) {
        this.type = type;
    }

    @Override
    public void setOperandLeft(int operandLeft) {
        this.operandLeft = operandLeft;
    }

    @Override
    public void setOperandRight(int operandRight) {
        this.operandRight = operandRight;
    }
    
    @Override
    public boolean simplify() {
        switch (type) {
        case ADD_INVERSE:
            return simplifyAddInverse();
        case MULTIPLY_INVERSE:
            return simplifyMultiplyInverse();
        case ADD:
            return simplifyAdd();
        case MULTIPLY:
            return simplifyMultiply();
        default:
            return false;
        }
    }

    private boolean simplifyAddInverse() {
        if (dag.getOperatorType(operandLeft) != OperatorType.NUMBER) {
            return false;
        }
        BigInteger num = dag.getNumberNumerator(operandLeft).negate();
        BigInteger den = dag.getNumberDenominator(operandLeft);
        this.num = dag.getNumberOfValue(num);
        this.den = dag.getNumberOfValue(den);
        return true;
    }

    private boolean simplifyMultiplyInverse() {
        if (dag.getOperatorType(operandLeft) != OperatorType.NUMBER) {
            return false;
        }
        BigInteger num = dag.getNumberDenominator(operandLeft);
        BigInteger den = dag.getNumberNumerator(operandLeft);
        if (den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        }
        this.num = dag.getNumberOfValue(num);
        this.den = dag.getNumberOfValue(den);
        return true;
    }

    private boolean simplifyAdd() {
        if (dag.getOperatorType(operandLeft) != OperatorType.NUMBER) {
            return false;
        }
        if (dag.getOperatorType(operandRight) != OperatorType.NUMBER) {
            return false;
        }
        BigInteger numLeft = dag.getNumberNumerator(operandLeft);
        BigInteger denLeft = dag.getNumberDenominator(operandLeft);
        
        BigInteger numRight = dag.getNumberNumerator(operandRight);
        BigInteger denRight = dag.getNumberDenominator(operandRight);

        BigInteger numLeftTimesDenRight = numLeft.multiply(denRight);
        BigInteger numRightTimesDenLeft = numRight.multiply(denLeft);
        BigInteger num = numLeftTimesDenRight.add(numRightTimesDenLeft);
        BigInteger gcdNumWithDenLeft = num.gcd(denLeft);
        num = num.divide(gcdNumWithDenLeft);
        denLeft = denLeft.divide(gcdNumWithDenLeft);
        BigInteger gcdNumWithDenRight = num.gcd(denRight);
        num = num.divide(gcdNumWithDenRight);
        denRight = denRight.divide(gcdNumWithDenRight);
        BigInteger den = denLeft.multiply(denRight);
        if (den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        }
        this.num = dag.getNumberOfValue(num);
        this.den = dag.getNumberOfValue(den);
        return true;
    }

    private boolean simplifyMultiply() {
        if (dag.getOperatorType(operandLeft) != OperatorType.NUMBER) {
            return false;
        }
        if (dag.getOperatorType(operandRight) != OperatorType.NUMBER) {
            return false;
        }
        BigInteger numLeft = dag.getNumberNumerator(operandLeft);
        BigInteger denLeft = dag.getNumberDenominator(operandLeft);
        
        BigInteger numRight = dag.getNumberNumerator(operandRight);
        BigInteger denRight = dag.getNumberDenominator(operandRight);

        BigInteger gcdNumLeftWithDenRight = numLeft.gcd(denRight);
        BigInteger gcdNumRightWithDenLeft = numRight.gcd(denLeft);
        numLeft = numLeft.divide(gcdNumLeftWithDenRight);
        denRight = denRight.divide(gcdNumLeftWithDenRight);
        numRight = numRight.divide(gcdNumRightWithDenLeft);
        denLeft = denLeft.divide(gcdNumRightWithDenLeft);
        BigInteger num = numLeft.multiply(numRight);
        BigInteger den = denLeft.multiply(denRight);
        
        if (den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        }
        this.num = dag.getNumberOfValue(num);
        this.den = dag.getNumberOfValue(den);
        return true;
    }

    @Override
    public OperatorType getResultType() {
        return OperatorType.NUMBER;
    }

    @Override
    public int getResultOperandLeft() {
        return num;
    }

    @Override
    public int getResultOperandRight() {
        return den;
    }
}
