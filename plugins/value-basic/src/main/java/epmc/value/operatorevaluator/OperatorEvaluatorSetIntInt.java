package epmc.value.operatorevaluator;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;

public final class OperatorEvaluatorSetIntInt implements OperatorEvaluator {
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
            if (!TypeInteger.is(types[0])) {
                return null;
            }
            if (!TypeInteger.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorSetIntInt(this);
        }
    }

    private OperatorEvaluatorSetIntInt(Builder builder) {
    }

    @Override
    public Type resultType() {
        return TypeInteger.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert ValueInteger.is(result) : result.getType();
        ValueInteger.as(result).set(ValueInteger.as(operands[0]).getInt());
    }
}
