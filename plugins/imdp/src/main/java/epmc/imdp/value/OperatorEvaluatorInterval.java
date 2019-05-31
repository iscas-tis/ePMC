package epmc.imdp.value;

import epmc.imdp.operator.OperatorInterval;
import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorInterval implements OperatorEvaluator {
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
        public void setTypes(Type... types) {
            assert !built;
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert !built;
            built = true;
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            if (operator != OperatorInterval.INTERVAL) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeReal.is(types[0])) {
                return null;
            }
            if (!TypeReal.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorInterval(this);
        }
        
    }

    OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());

    private OperatorEvaluatorInterval(Builder builder) {
    }

    @Override
    public Type resultType() {
        return TypeInterval.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length == 2;
        assert operands[0] != null;
        assert operands[1] != null;
        assert ValueReal.is(operands[0]);
        assert ValueReal.is(operands[1]);
        assert ValueInterval.is(result);
        set.apply(ValueInterval.as(result).getIntervalLower(), operands[0]);
        set.apply(ValueInterval.as(result).getIntervalUpper(), operands[1]);
    }
}
