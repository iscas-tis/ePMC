package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorSuperOperatorMatrix;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorSuperOperatorMatrix implements OperatorEvaluator {
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
            if (operator != OperatorSuperOperatorMatrix.SUPEROPERATOR_MATRIX) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            return new OperatorEvaluatorSuperOperatorMatrix(this);
        }
    }
    
    private final OperatorEvaluator set;

    private OperatorEvaluatorSuperOperatorMatrix(Builder builder) {
        set = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[0], TypeMatrix.get(TypeComplex.get()));
    }

    @Override
    public Type resultType() {
        return TypeSuperOperator.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValueSuperOperator resultSuperOperator = ValueSuperOperator.as(result);
        set.apply(resultSuperOperator.getMatrix(), ValueMatrix.as(operands[0]));
    }
}
