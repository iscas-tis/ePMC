package epmc.value.operatorevaluator;

import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInterval;
import epmc.value.operator.OperatorSet;

public final class OperatorEvaluatorSetIntervalInterval implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private boolean built;
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            assert !built;
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            assert !built;
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert !built;
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            built = true;
            if (operator != OperatorSet.SET) {
                return null;
            }
            if (!TypeInterval.is(types[0])) {
                return null;
            }
            if (!TypeInterval.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorSetIntervalInterval(this);
        }
    }

    private OperatorEvaluatorSetIntervalInterval(Builder builder) {
    }

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        if (!TypeInterval.is(types[0])) {
            return false;
        }
        if (!TypeInterval.is(types[1])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Type... types) {
        return types[1];
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        ValueInterval resultInterval = ValueInterval.as(result);
        ValueInterval operandInterval = ValueInterval.as(operands[0]);
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        set.apply(resultInterval.getIntervalLower(), operandInterval.getIntervalLower());
        set.apply(resultInterval.getIntervalUpper(), operandInterval.getIntervalUpper());
    }
}
