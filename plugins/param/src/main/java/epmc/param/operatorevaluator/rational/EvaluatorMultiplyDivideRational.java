package epmc.param.operatorevaluator.rational;

import java.math.BigInteger;

import epmc.operator.Operator;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorMultiply;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorMultiplyDivideRational implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            if (operator != OperatorMultiply.MULTIPLY && operator != OperatorDivide.DIVIDE) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeRational.is(types[0])) {
                return null;
            }
            if (!TypeRational.is(types[1])) {
                return null;
            }
            return new EvaluatorMultiplyDivideRational(this);
        }
        
    }

    private final TypeRational resultType;
    private final Operator operator;
    
    private EvaluatorMultiplyDivideRational(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        operator = builder.operator;
        resultType = TypeRational.as(builder.types[0]);
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        ValueRational op1 = ValueRational.as(operands[0]);
        ValueRational op2 = ValueRational.as(operands[1]);
        BigInteger numLeft = op1.getNumerator();
        BigInteger denLeft = op1.getDenominator();
        
        BigInteger numRight;
        BigInteger denRight;

//        System.out.println("OPR " + operator);
  //      System.out.println("OP1 " + op1);
    //    System.out.println("OP2 " + op2);
        if (operator == OperatorMultiply.MULTIPLY) {
            numRight = op2.getNumerator();
            denRight = op2.getDenominator();
        } else {
            assert operator == OperatorDivide.DIVIDE;
            numRight = op2.getDenominator();
            denRight = op2.getNumerator();
            if (denRight.compareTo(BigInteger.ZERO) < 0) {
                numRight = numRight.negate();
                denRight = denRight.negate();
            }
        }
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
        ValueRational resultRational = ValueRational.as(result);
        resultRational.set(num, den);
      //  System.out.println("RESULT " + resultRational);
    }
}
