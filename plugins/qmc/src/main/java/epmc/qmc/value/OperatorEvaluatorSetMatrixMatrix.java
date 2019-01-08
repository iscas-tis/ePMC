package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorSetMatrixMatrix implements OperatorEvaluator {
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
            if (!TypeMatrix.is(types[0])) {
                return null;
            }
            if (!TypeMatrix.is(types[1])) {
                return null;
            }
            TypeAlgebra typeOperand = TypeMatrix.as(types[0]).getEntryType();
            TypeAlgebra typeMatrixEntry = TypeMatrix.as(types[1]).getEntryType();
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, typeOperand, typeMatrixEntry);
            if (set == null) {
                return null;
            }
            return new OperatorEvaluatorSetMatrixMatrix(this);
        }
    }

    private final Type resultType;
    private final OperatorEvaluator setArray;

    private OperatorEvaluatorSetMatrixMatrix(Builder builder) {
        resultType = builder.types[1];
        setArray = ContextValue.get().getEvaluator(OperatorSet.SET,
                TypeMatrix.as(builder.types[0]).getEntryType().getTypeArray(),
                TypeMatrix.as(builder.types[1]).getEntryType().getTypeArray());
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
        ValueMatrix operandMatrix = ValueMatrix.as(operands[0]);
        if (resultMatrix == operandMatrix) {
            return;
        }
        if (operandMatrix.isDimensionsUnspecified()) {
            resultMatrix.setDimensionsUnspecified();
        } else {
            resultMatrix.setDimensions(operandMatrix.getNumRows(),
                    operandMatrix.getNumColumns());
        }
        setArray.apply(resultMatrix.getValues(), operandMatrix.getValues());
    }
}
