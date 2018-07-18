package epmc.param.operatorevaluator.rational;

import java.math.BigInteger;

import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorSubtract;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddSubtractRational implements OperatorEvaluator {
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
            if (operator != OperatorAdd.ADD && operator != OperatorSubtract.SUBTRACT) {
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
            return new EvaluatorAddSubtractRational(this);
        }
        
    }

    private final TypeRational resultType;
    private final Operator operator;
    
    private EvaluatorAddSubtractRational(Builder builder) {
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
        if (operator == OperatorAdd.ADD) {
            numRight = op2.getNumerator();
            denRight = op2.getDenominator();
        } else {
            assert operator == OperatorSubtract.SUBTRACT;
            numRight = op2.getNumerator().negate();
            denRight = op2.getDenominator();
        }
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
        ValueRational resultRational = ValueRational.as(result);
        resultRational.set(num, den);
    }
}
