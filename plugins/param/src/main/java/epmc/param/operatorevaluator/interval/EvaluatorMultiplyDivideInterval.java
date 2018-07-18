package epmc.param.operatorevaluator.interval;

import epmc.operator.Operator;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorMultiplyInverse;
import epmc.operator.OperatorSet;
import epmc.param.operator.OperatorNextDown;
import epmc.param.operator.OperatorNextUp;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorMultiplyDivideInterval implements OperatorEvaluator {
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
            if (!TypeInterval.is(types[0])) {
                return null;
            }
            if (!TypeInterval.is(types[1])) {
                return null;
            }
            if (!types[0].equals(types[1])) {
                return null;
            }
            return new EvaluatorMultiplyDivideInterval(this);
        }
    }

    private final TypeInterval resultType;
    private final OperatorEvaluator nextDown;
    private final OperatorEvaluator nextUp;
    private final OperatorEvaluator multiplyReal;
    private final OperatorEvaluator multiplyInverseInterval;
    private final Operator operator;
    private final boolean widen;
    private final ValueInterval buffer;
    private final ValueReal valuell;
    private final ValueReal valuelr;
    private final ValueReal valuerl;
    private final ValueReal valuerr;
    private final ValueReal valueLowest;
    private final ValueReal valueHighest;
    private OperatorEvaluator setReal;
    private final OperatorEvaluator ltReal;
    private final OperatorEvaluator gtReal;
    private final ValueBoolean cmp;
    
    private EvaluatorMultiplyDivideInterval(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        operator = builder.operator;
        resultType = TypeInterval.as(builder.types[0]);
        TypeReal entryType = resultType.getEntryType();
        nextDown = ContextValue.get().getEvaluatorOrNull(OperatorNextDown.NEXT_DOWN, entryType);
        nextUp = ContextValue.get().getEvaluatorOrNull(OperatorNextUp.NEXT_UP, entryType);
        multiplyReal = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, entryType, entryType);
        multiplyInverseInterval = ContextValue.get().getEvaluator(OperatorMultiplyInverse.MULTIPLY_INVERSE, resultType);
        if (operator == OperatorMultiply.MULTIPLY) {
            buffer = null;
        } else {
            assert operator == OperatorDivide.DIVIDE;
            buffer = resultType.newValue();
        }
        setReal = ContextValue.get().getEvaluator(OperatorSet.SET, entryType, entryType);
        widen = nextDown != null && nextUp != null;
        valuell = entryType.newValue();
        valuelr = entryType.newValue();
        valuerl = entryType.newValue();
        valuerr = entryType.newValue();
        valueLowest = entryType.newValue();
        valueHighest = entryType.newValue();
        ltReal = ContextValue.get().getEvaluator(OperatorLt.LT, entryType, entryType);
        gtReal = ContextValue.get().getEvaluator(OperatorGt.GT, entryType, entryType);
        cmp = TypeBoolean.get().newValue();
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
        ValueInterval right = null;
        if (operator == OperatorDivide.DIVIDE) {
            multiplyInverseInterval.apply(buffer, op2Interval);
            right = buffer;
        } else {
            right = op2Interval;
        }
        multiplyReal.apply(valuell, op1Interval.getIntervalLower(), right.getIntervalLower());
        multiplyReal.apply(valuelr, op1Interval.getIntervalLower(), right.getIntervalUpper());
        multiplyReal.apply(valuerl, op1Interval.getIntervalUpper(), right.getIntervalLower());
        multiplyReal.apply(valuerr, op1Interval.getIntervalUpper(), right.getIntervalUpper());
        
        setReal.apply(valueLowest, valuell);
        ltReal.apply(cmp, valuelr, valueLowest);
        if (cmp.getBoolean()) {
            setReal.apply(valueLowest, valuelr);
        }
        ltReal.apply(cmp, valuerl, valueLowest);
        if (cmp.getBoolean()) {
            setReal.apply(valueLowest, valuerl);
        }
        ltReal.apply(cmp, valuerr, valueLowest);
        if (cmp.getBoolean()) {
            setReal.apply(valueLowest, valuerr);
        }
        setReal.apply(valueHighest, valuell);
        gtReal.apply(cmp, valuelr, valueHighest);
        if (cmp.getBoolean()) {
            setReal.apply(valueHighest, valuelr);
        }
        gtReal.apply(cmp, valuerl, valueHighest);
        if (cmp.getBoolean()) {
            setReal.apply(valueHighest, valuerl);
        }
        gtReal.apply(cmp, valuerr, valueHighest);
        if (cmp.getBoolean()) {
            setReal.apply(valueHighest, valuerr);
        }
        setReal.apply(resultInterval.getIntervalLower(), valueLowest);
        setReal.apply(resultInterval.getIntervalUpper(), valueHighest);
        if (widen) {
            nextDown.apply(resultInterval.getIntervalLower(), resultInterval.getIntervalLower());
            nextUp.apply(resultInterval.getIntervalUpper(), resultInterval.getIntervalUpper());
        }
    }
}
