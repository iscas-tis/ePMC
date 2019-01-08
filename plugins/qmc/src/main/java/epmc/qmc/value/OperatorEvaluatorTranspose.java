package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorTranspose;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorTranspose implements OperatorEvaluator {
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
            if (operator != OperatorTranspose.TRANSPOSE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeMatrix.is(types[0])) {
                return null;
            }
            return new OperatorEvaluatorTranspose(this);
        }
    }

    private final TypeMatrix resultType;
    private final OperatorEvaluator set;
    private final Value entryAcc1;
    private final Value resultBuffer;

    private OperatorEvaluatorTranspose(Builder builder) {
        resultType = TypeMatrix.as(builder.types[0]);
        set = ContextValue.get().getEvaluator(OperatorSet.SET, builder.types[0], builder.types[0]);
        entryAcc1 = resultType.getEntryType().newValue();
        resultBuffer = resultType.newValue();
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        Value operand = operands[0];
        ValueMatrix operandMatrix = UtilValueQMC.castOrImport(ValueMatrix.as(result), operand, 0, false);
        if (result == operandMatrix) {
            apply(resultBuffer, operand);
            set.apply(result, resultBuffer);
            return;
        }
        ValueMatrix resultMatrix = ValueMatrix.as(result);
        resultMatrix.setDimensions(operandMatrix.getNumColumns(), operandMatrix.getNumRows());
        for (int row = 0; row < operandMatrix.getNumRows(); row++) {
            for (int col = 0; col < operandMatrix.getNumColumns(); col++) {
                operandMatrix.get(entryAcc1, row, col);
                resultMatrix.set(entryAcc1, col, row);
            }
        }
    }
}
