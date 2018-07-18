package epmc.param.operatorevaluator.rational;

import java.math.BigInteger;

import epmc.operator.Operator;
import epmc.operator.OperatorMultiplyInverse;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorMultiplyInverseRational implements OperatorEvaluator {
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
            if (operator != OperatorMultiplyInverse.MULTIPLY_INVERSE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeRational.is(types[0])) {
                return null;
            }
            return new EvaluatorMultiplyInverseRational(this);
        }
        
    }

    private final TypeRational resultType;
    
    private EvaluatorMultiplyInverseRational(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
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
        ValueRational valueRational = ValueRational.as(operands[0]);
        ValueRational resultRational = ValueRational.as(result);
        BigInteger num = valueRational.getDenominator();
        BigInteger den = valueRational.getNumerator();
        if (den.compareTo(BigInteger.ZERO) == -1) {
            num = num.negate();
            den = den.negate();
        }
        resultRational.set(num, den);
    }
}
