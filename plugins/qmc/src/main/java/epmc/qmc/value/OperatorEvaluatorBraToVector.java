package epmc.qmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.qmc.operator.OperatorBraToVector;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorBraToVector implements OperatorEvaluator {
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
            if (operator != OperatorBraToVector.BRA_TO_VECTOR) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            return new OperatorEvaluatorBraToVector(this);
        }
    }

    private final TypeArrayComplex resultType;
    private final Value resultBuffer;
    private final OperatorEvaluator set;
    private final Value entryAcc1;
    private final Value entryAcc2;
    
    private OperatorEvaluatorBraToVector(Builder builder) {
        resultType = TypeComplex.get().getTypeArray();
        resultBuffer = resultType.newValue();
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeComplex.get().getTypeArray(), TypeComplex.get().getTypeArray());
        entryAcc1 = resultType.getEntryType().newValue();
        entryAcc2 = resultType.getEntryType().newValue();
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        Value operand = operands[0];
        if (result == operand) {
            apply(operand, resultBuffer, operand);
            set.apply(result, resultBuffer);
            return;
        }
        assert operand != null;
        ValueMatrix operandArray = ValueMatrix.as(operand);
        assert operandArray.getNumRows() == 1;
        int length = operandArray.getNumColumns();
        ValueArray resultArray = ValueArray.as(result);
        resultArray.setSize(length);
        for (int index = 0; index < length; index++) {
            operandArray.getValues().get(entryAcc1, index);
            UtilValueQMC.conjugate(entryAcc2, entryAcc1);
            resultArray.set(entryAcc2, index);
        }
    }
}
