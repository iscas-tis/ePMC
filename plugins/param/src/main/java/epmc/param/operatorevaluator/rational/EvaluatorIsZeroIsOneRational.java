package epmc.param.operatorevaluator.rational;

import java.math.BigInteger;

import epmc.operator.Operator;
import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorIsZero;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorIsZeroIsOneRational implements OperatorEvaluator {
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
            if (operator != OperatorIsZero.IS_ZERO
                    && operator != OperatorIsOne.IS_ONE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeRational.is(types[0])) {
                return null;
            }
            return new EvaluatorIsZeroIsOneRational(this);
        }        
    }

    private final TypeBoolean resultType;
    private final Operator operator;
    
    private EvaluatorIsZeroIsOneRational(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        operator = builder.operator;
        resultType = TypeBoolean.get();
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
        ValueRational operand = ValueRational.as(operands[0]);
        BigInteger numerator = operand.getNumerator();
        BigInteger denominator = operand.getDenominator();
        
        ValueBoolean resultBoolean = ValueBoolean.as(result);
        if (operator == OperatorIsZero.IS_ZERO) {
            resultBoolean.set(numerator.equals(BigInteger.ZERO) && denominator.equals(BigInteger.ONE));
        } else {
            assert operator == OperatorIsOne.IS_ONE;
            resultBoolean.set(numerator.equals(BigInteger.ONE) && denominator.equals(BigInteger.ONE));
        }
    }
}
