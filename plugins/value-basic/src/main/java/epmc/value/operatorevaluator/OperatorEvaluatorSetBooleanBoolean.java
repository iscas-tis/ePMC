package epmc.value.operatorevaluator;

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operator.OperatorSet;

public final class OperatorEvaluatorSetBooleanBoolean implements OperatorEvaluator {
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
            if (types.length != 2) {
                return null;
            }
            assert types.length == 2;
            if (!TypeBoolean.is(types[0])) {
                return null;
            }
            if (!TypeBoolean.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorSetBooleanBoolean(this);
        }
    }

    private OperatorEvaluatorSetBooleanBoolean(Builder builder) {
    }

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        assert types.length == 2;
        if (!TypeBoolean.is(types[0])) {
            return false;
        }
        if (!TypeBoolean.is(types[1])) {
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
        assert operands.length >= 1;
        ValueBoolean.as(result).set(ValueBoolean.as(operands[0]).getBoolean());
    }
}
