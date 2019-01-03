package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorSetMatrixAlgebra implements OperatorEvaluator {
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
            /* have other evaluators to set matrix to matrix */
            if (TypeMatrix.is(types[0])) {
                return null;
            }
            if (!TypeAlgebra.is(types[0])) {
                return null;
            }
            if (!TypeMatrix.is(types[1])) {
                return null;
            }
            TypeAlgebra typeOperand = TypeAlgebra.as(types[0]);
            TypeAlgebra typeMatrixEntry = TypeMatrix.as(types[1]).getEntryType();
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, typeOperand, typeMatrixEntry);
            if (set == null) {
                return null;
            }
            return new OperatorEvaluatorSetMatrixAlgebra(this);
        }
    }

    private final Type resultType;

    private OperatorEvaluatorSetMatrixAlgebra(Builder builder) {
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
        ValueMatrix resultMatrix = ValueMatrix.as(result);
        ValueAlgebra operandAlgebra = ValueAlgebra.as(operands[0]);
        resultMatrix.setDimensionsUnspecified();
        resultMatrix.getValues().set(operandAlgebra, 0);
    }
}
