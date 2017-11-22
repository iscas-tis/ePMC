package epmc.value.operatorevaluator;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueInteger;

public final class OperatorEvaluatorSetAlgebraInt implements OperatorEvaluator {
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
            if (!TypeInteger.is(types[0])) {
                return null;
            }
            if (!TypeAlgebra.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorSetAlgebraInt(this);
        }
    }

    private final Type resultType;

    private OperatorEvaluatorSetAlgebraInt(Builder builder) {
        resultType = builder.types[1];
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        ValueAlgebra.as(result).set(ValueInteger.as(operands[0]).getInt());
    }
}
