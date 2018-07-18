package epmc.param.operatorevaluator.interval;

import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.param.operator.OperatorNextDown;
import epmc.param.operator.OperatorNextUp;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInterval;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddSubtractInterval implements OperatorEvaluator {
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
            if (!TypeInterval.is(types[0])) {
                return null;
            }
            if (!TypeInterval.is(types[1])) {
                return null;
            }
            if (!types[0].equals(types[1])) {
                return null;
            }
            return new EvaluatorAddSubtractInterval(this);
        }
        
    }

    private final TypeInterval resultType;
    private final OperatorEvaluator nextDown;
    private final OperatorEvaluator nextUp;
    private final OperatorEvaluator add;
    private final OperatorEvaluator subtract;
    private final OperatorEvaluator set;
    private final Operator operator;
    private final boolean widen;
    private final ValueInterval buffer;
    
    private EvaluatorAddSubtractInterval(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        operator = builder.operator;
        resultType = TypeInterval.as(builder.types[0]);
        TypeReal entryType = resultType.getEntryType();
        nextDown = ContextValue.get().getEvaluatorOrNull(OperatorNextDown.NEXT_DOWN, entryType);
        nextUp = ContextValue.get().getEvaluatorOrNull(OperatorNextUp.NEXT_UP, entryType);
        if (operator == OperatorAdd.ADD) {
            add = ContextValue.get().getEvaluator(OperatorAdd.ADD, entryType, entryType);
            subtract = null;
            buffer = null;
        } else {
            assert operator == OperatorSubtract.SUBTRACT;
            add = null;
            subtract = ContextValue.get().getEvaluator(OperatorSubtract.SUBTRACT, entryType, entryType);
            buffer = resultType.newValue();
        }
        widen = nextDown != null && nextUp != null;
        set = ContextValue.get().getEvaluator(OperatorSet.SET, resultType, resultType);
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
        ValueInterval resultInterval = ValueInterval.as(result);
        ValueInterval op1Interval = ValueInterval.as(operands[0]);
        ValueInterval op2Interval = ValueInterval.as(operands[1]);
        if (operator == OperatorAdd.ADD) {
            add.apply(resultInterval.getIntervalLower(), op1Interval.getIntervalLower(), op2Interval.getIntervalLower());
            add.apply(resultInterval.getIntervalUpper(), op1Interval.getIntervalUpper(), op2Interval.getIntervalUpper());
        } else if (resultInterval == op1Interval || resultInterval == op2Interval) {
            assert operator != OperatorSubtract.SUBTRACT;
            subtract.apply(buffer.getIntervalLower(), op1Interval.getIntervalLower(), op2Interval.getIntervalUpper());
            subtract.apply(buffer.getIntervalUpper(), op1Interval.getIntervalUpper(), op2Interval.getIntervalLower());
            set.apply(resultInterval, buffer);
        } else {
            assert operator != OperatorSubtract.SUBTRACT;
            subtract.apply(resultInterval.getIntervalLower(), op1Interval.getIntervalLower(), op2Interval.getIntervalUpper());
            subtract.apply(resultInterval.getIntervalUpper(), op1Interval.getIntervalUpper(), op2Interval.getIntervalLower());
        }
        if (widen) {
            nextDown.apply(resultInterval.getIntervalLower(), resultInterval.getIntervalLower());
            nextUp.apply(resultInterval.getIntervalUpper(), resultInterval.getIntervalUpper());
        }
    }
}
