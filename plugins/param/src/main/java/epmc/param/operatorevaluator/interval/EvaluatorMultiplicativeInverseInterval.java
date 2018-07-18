package epmc.param.operatorevaluator.interval;

import epmc.operator.Operator;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorLt;
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
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorMultiplicativeInverseInterval implements OperatorEvaluator {
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
            if (!TypeInterval.is(types[0])) {
                return null;
            }
            return new EvaluatorMultiplicativeInverseInterval(this);
        }
    }

    private final TypeInterval resultType;
    private final OperatorEvaluator multiplyInverseReal;
    private final OperatorEvaluator setInterval;
    private final OperatorEvaluator setReal;
    private final ValueInterval buffer;
    private final OperatorEvaluator isZero;
    private final OperatorEvaluator ltReal;
    private final OperatorEvaluator gtReal;
    private final ValueReal zero;
    private final ValueBoolean cmp;
    private final ValueReal negInf;
    private final ValueReal posInf;
    private final OperatorEvaluator nextDown;
    private final OperatorEvaluator nextUp;
    private final boolean widen;
    
    private EvaluatorMultiplicativeInverseInterval(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        resultType = TypeInterval.as(builder.types[0]);
        TypeReal entryType = resultType.getEntryType();
        multiplyInverseReal = ContextValue.get().getEvaluator(OperatorMultiplyInverse.MULTIPLY_INVERSE, entryType);
        setInterval = ContextValue.get().getEvaluator(OperatorSet.SET, resultType, resultType);
        setReal = ContextValue.get().getEvaluator(OperatorSet.SET, entryType, entryType);
        buffer = resultType.newValue();
        ltReal = ContextValue.get().getEvaluator(OperatorLt.LT, entryType, entryType);
        gtReal = ContextValue.get().getEvaluator(OperatorGt.GT, entryType, entryType);
        isZero = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, entryType);
        zero = entryType.newValue();
        zero.set(0);
        cmp = TypeBoolean.get().newValue();
        negInf = UtilValue.newValue(entryType, UtilValue.NEG_INF);
        posInf = UtilValue.newValue(entryType, UtilValue.POS_INF);
        nextDown = ContextValue.get().getEvaluatorOrNull(OperatorNextDown.NEXT_DOWN, entryType);
        nextUp = ContextValue.get().getEvaluatorOrNull(OperatorNextUp.NEXT_UP, entryType);
        widen = nextDown != null && nextUp != null;
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
        boolean containsZero = false;
        isZero.apply(cmp, valueInterval.getIntervalLower());
        if (cmp.getBoolean()) {
            containsZero = true;
        }
        isZero.apply(cmp, valueInterval.getIntervalUpper());
        if (cmp.getBoolean()) {
            containsZero = true;
        }
        ltReal.apply(cmp, valueInterval.getIntervalLower(), zero);
        boolean leftLtZero = cmp.getBoolean();
        gtReal.apply(cmp, valueInterval.getIntervalUpper(), zero);
        boolean rightGtZero = cmp.getBoolean();
        if (leftLtZero && rightGtZero) {
            containsZero = true;
        }
        if (containsZero) {
            setReal.apply(resultInterval.getIntervalLower(), negInf);
            setReal.apply(resultInterval.getIntervalUpper(), posInf);
        } else if (resultInterval == valueInterval) {
            multiplyInverseReal.apply(buffer.getIntervalLower(), valueInterval.getIntervalUpper());
            multiplyInverseReal.apply(buffer.getIntervalUpper(), valueInterval.getIntervalLower());
            setInterval.apply(resultInterval, buffer);
        } else {
            multiplyInverseReal.apply(resultInterval.getIntervalLower(), valueInterval.getIntervalUpper());
            multiplyInverseReal.apply(resultInterval.getIntervalUpper(), valueInterval.getIntervalLower());            
        }
        if (widen) {
            nextDown.apply(resultInterval.getIntervalLower(), resultInterval.getIntervalLower());
            nextUp.apply(resultInterval.getIntervalUpper(), resultInterval.getIntervalUpper());
        }
    }
}
