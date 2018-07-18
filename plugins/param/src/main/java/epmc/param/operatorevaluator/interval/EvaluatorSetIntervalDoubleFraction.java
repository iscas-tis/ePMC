package epmc.param.operatorevaluator.interval;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.param.points.Side;
import epmc.param.points.UtilPoints;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeInterval;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.ValueInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorSetIntervalDoubleFraction implements OperatorEvaluator {
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
            if (operator != OperatorSet.SET) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeRational.is(types[0])) {
                return null;
            }
            if (!TypeInterval.is(types[1])) {
                return null;
            }
            if (!TypeDouble.is(TypeInterval.as(types[1]).getEntryType())) {
                return null;
            }
            return new EvaluatorSetIntervalDoubleFraction(this);
        }
    }

    private final TypeInterval resultType;
    
    private EvaluatorSetIntervalDoubleFraction(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        resultType = TypeInterval.as(builder.types[1]);
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
        ValueInterval resultInterval = ValueInterval.as(result);
        ValueDouble resultLowerDouble = ValueDouble.as(resultInterval.getIntervalLower());
        ValueDouble resultUpperDouble = ValueDouble.as(resultInterval.getIntervalUpper());
        resultLowerDouble.set(UtilPoints.rationalToDouble(valueRational, Side.LEFT));
        resultUpperDouble.set(UtilPoints.rationalToDouble(valueRational, Side.RIGHT));
    }
}
