package epmc.param.operatorevaluator.interval;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.param.operator.OperatorNextDown;
import epmc.param.operator.OperatorNextUp;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.Value;
import epmc.value.ValueInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorSetIntervalInterval implements OperatorEvaluator {
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
            if (!TypeInterval.is(types[0])) {
                return null;
            }
            if (!TypeInterval.is(types[1])) {
                return null;
            }
            return new EvaluatorSetIntervalInterval(this);
        }
    }

    private final OperatorEvaluator setEntry;
    private final boolean widen;
    private final OperatorEvaluator nextDown;
    private final OperatorEvaluator nextUp;
    private final TypeInterval resultType;
    
    private EvaluatorSetIntervalInterval(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        TypeInterval operandType = TypeInterval.as(builder.types[0]);
        resultType = TypeInterval.as(builder.types[1]);
        setEntry = ContextValue.get().getEvaluator(OperatorSet.SET, operandType.getEntryType(), resultType.getEntryType());
        nextDown = ContextValue.get().getEvaluatorOrNull(OperatorNextDown.NEXT_DOWN, resultType.getEntryType());
        nextUp = ContextValue.get().getEvaluatorOrNull(OperatorNextUp.NEXT_UP, resultType.getEntryType());
        widen = !operandType.equals(resultType) && nextDown != null && nextUp != null;
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
        ValueInterval valueInterval = ValueInterval.as(operands[0]);
        ValueInterval resultInterval = ValueInterval.as(result);
        setEntry.apply(resultInterval.getIntervalLower(), valueInterval.getIntervalLower());
        setEntry.apply(resultInterval.getIntervalUpper(), valueInterval.getIntervalUpper());
        if (widen) {
            nextDown.apply(resultInterval.getIntervalLower(), resultInterval.getIntervalLower());
            nextUp.apply(resultInterval.getIntervalUpper(), resultInterval.getIntervalUpper());
        }
    }
}
