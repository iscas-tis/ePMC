package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.qmc.operator.OperatorIdentityMatrix;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorIdentityMatrix implements OperatorEvaluator {
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
            if (operator != OperatorIdentityMatrix.IDENTITY_MATRIX) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            return new OperatorEvaluatorIdentityMatrix(this);
        }
    }

    private OperatorEvaluatorIdentityMatrix(Builder builder) {
    }

    @Override
    public Type resultType() {
        return TypeMatrix.get(TypeComplex.get());
    }

    @Override
    public void apply(Value result, Value... operands) {
        UtilValueQMC.identityMatrix(ValueMatrix.as(result), ValueInteger.as(operands[0]).getInt());
    }
}
