package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorKetToVector;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorKetToVector implements OperatorEvaluator {
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
            if (operator != OperatorKetToVector.KET_TO_VECTOR) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            return new OperatorEvaluatorKetToVector(this);
        }
    }

    private final TypeArrayComplex resultType;
    private final Value resultBuffer;
    private final OperatorEvaluator set;
    private final Value entryAcc1;
    
    private OperatorEvaluatorKetToVector(Builder builder) {
        resultType = TypeComplex.get().getTypeArray();
        resultBuffer = resultType.newValue();
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeComplex.get().getTypeArray(), TypeComplex.get().getTypeArray());
        entryAcc1 = resultType.getEntryType().newValue();
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        Value operand = operands[0];
        if (result == operand) {
            apply(resultBuffer, operand);
            set.apply(result, resultBuffer);
            return;
        }
        assert operand != null;
        ValueMatrix operandArray = ValueMatrix.as(operand);
        int length = operandArray.getNumRows();
        ValueArray resultArray = ValueArray.as(result);
        resultArray.setSize(length);
        for (int index = 0; index < length; index++) {
            operandArray.getValues().get(entryAcc1, index);
            resultArray.set(entryAcc1, index);
        }
    }
}
