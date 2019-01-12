package epmc.param.value.dag.simplifier;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import epmc.options.Options;
import epmc.param.options.OptionsParam;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class EvaluatorRational implements Evaluator {
    public final static String IDENTIFIER = "rational";

    public final static class Builder implements Evaluator.Builder {
        private Dag dag;

        @Override
        public Evaluator.Builder setDag(Dag dag) {
            this.dag = dag;
            return this;
        }

        @Override
        public Evaluator build() {
            return new EvaluatorRational(this);
        }
        
    }
    
    private final static class Rational {
        private final BigInteger num;
        private final BigInteger den;
        
        public Rational(BigInteger num, BigInteger den) {
            this.num = num;
            this.den = den;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Rational)) {
                return false;
            }
            Rational other = (Rational) obj;
            if (!this.num.equals(other.num)) {
                return false;
            }
            if (!this.den.equals(other.den)) {
                return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = 0;
            hash = num.hashCode() + (hash << 6) + (hash << 16) - hash;
            hash = den.hashCode() + (hash << 6) + (hash << 16) - hash;
            return hash;
        }
        
        @Override
        public String toString() {
            if (den.equals(BigInteger.ONE)) {
                return "Rational(" + num + ")";                
            } else {
                return "Rational(" + num + "/" + den + ")";
            }
        }
    }

    private final static int INVALID = -1;
    
    private final List<Rational> randomNumbersRational = new ArrayList<>();
    private final List<Rational> evalResultsListRational = new ArrayList<>();
    private final Object2IntOpenHashMap<Rational> evalResultsMapRational = new Object2IntOpenHashMap<>();
    private Rational lastValueRational;

    private final Random random = new Random();
    private final int randomBits = Options.get().getInteger(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_BITS);
    
    private final Dag dag;
    
    private EvaluatorRational(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        this.dag = builder.dag;
        evalResultsMapRational.defaultReturnValue(INVALID);
    }
    
    @Override
    public int evaluate(OperatorType type, int operandLeft, int operandRight) {
        Rational value = evaluateRational(type, operandLeft, operandRight);
        lastValueRational = value;
        return evalResultsMapRational.getInt(value);
    }

    private Rational evaluateRational(OperatorType type, int operandLeft, int operandRight) {
        adjustNumParameters();
        switch (type) {
        case NUMBER:
            return evaluateRationalNumber(type, operandLeft, operandRight);
        case PARAMETER:
            return evaluateRationalParameter(type, operandLeft, operandRight);
        case ADD_INVERSE:
            return evaluateRationalAddInverse(type, operandLeft, operandRight);
        case MULTIPLY_INVERSE:
            return evaluateRationalMultiplyInverse(type, operandLeft, operandRight);       
        case ADD:
            return evaluateRationalAdd(type, operandLeft, operandRight);
        case MULTIPLY:
            return evaluateRationalMultiply(type, operandLeft, operandRight);
        default:
            assert false;
            return null;
        }
    }

    private Rational evaluateRationalNumber(OperatorType type, int operandLeft, int operandRight) {
        return new Rational(dag.getValueFromNumber(operandLeft), dag.getValueFromNumber(operandRight));
    }

    private Rational evaluateRationalParameter(OperatorType type, int operandLeft, int operandRight) {
        return randomNumbersRational.get(operandLeft);
    }

    private Rational evaluateRationalAddInverse(OperatorType type, int operandLeft, int operandRight) {
        Rational entry = evalResultsListRational.get(operandLeft);
        BigInteger num = entry.num.negate();
        BigInteger den = entry.den;
        return new Rational(num, den);
    }

    private Rational evaluateRationalMultiplyInverse(OperatorType type, int operandLeft, int operandRight) {
        Rational entry = evalResultsListRational.get(operandLeft);
        BigInteger num = entry.den;
        BigInteger den = entry.num;
        if (den.compareTo(BigInteger.ZERO) < 0) {
            num = num.negate();
            den = den.negate();
        }
        return new Rational(num, den);
    }

    private Rational evaluateRationalAdd(OperatorType type, int operandLeft, int operandRight) {
        BigInteger numLeft = evalResultsListRational.get(operandLeft).num;
        BigInteger denLeft = evalResultsListRational.get(operandLeft).den;
        
        BigInteger numRight = evalResultsListRational.get(operandRight).num;
        BigInteger denRight = evalResultsListRational.get(operandRight).den;
    
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
        return new Rational(num, den);
    }

    private Rational evaluateRationalMultiply(OperatorType type, int operandLeft, int operandRight) {
        BigInteger numLeft = evalResultsListRational.get(operandLeft).num;
        BigInteger denLeft = evalResultsListRational.get(operandLeft).den;
        
        BigInteger numRight = evalResultsListRational.get(operandRight).num;
        BigInteger denRight = evalResultsListRational.get(operandRight).den;

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
        return new Rational(num, den);
    }
    
    private void adjustNumParameters() {
        while (randomNumbersRational.size() < dag.getParameters().getNumParameters()) {
            randomNumbersRational.add(new Rational(
                    new BigInteger(randomBits, random),
                    BigInteger.ONE));
        }
    }

    @Override
    public void commitResult() {
        int number = evalResultsListRational.size();
        evalResultsListRational.add(lastValueRational);
        evalResultsMapRational.put(lastValueRational, number);
    }
}
