package epmc.param.operatorevaluator.interval;

import epmc.operator.Operator;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddInverseInterval implements OperatorEvaluator {
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
            if (operator != OperatorAddInverse.ADD_INVERSE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeInterval.is(types[0])) {
                return null;
            }
            return new EvaluatorAddInverseInterval(this);
        }
    }

    private final TypeInterval resultType;
    private final OperatorEvaluator negateReal;
    private final OperatorEvaluator set;
    private final ValueInterval buffer;
    
    private EvaluatorAddInverseInterval(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        resultType = TypeInterval.as(builder.types[0]);
        TypeReal entryType = resultType.getEntryType();
        negateReal = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, entryType);
        set = ContextValue.get().getEvaluator(OperatorSet.SET, resultType, resultType);
        buffer = resultType.newValue();
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
        if (valueInterval == resultInterval) {
            negateReal.apply(buffer.getIntervalLower(), valueInterval.getIntervalUpper());
            negateReal.apply(buffer.getIntervalUpper(), valueInterval.getIntervalLower());
            set.apply(resultInterval, buffer);
        } else {
            negateReal.apply(resultInterval.getIntervalLower(), valueInterval.getIntervalUpper());
            negateReal.apply(resultInterval.getIntervalUpper(), valueInterval.getIntervalLower());
        }
    }
}
