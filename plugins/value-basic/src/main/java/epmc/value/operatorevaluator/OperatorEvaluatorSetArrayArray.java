package epmc.value.operatorevaluator;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;

public final class OperatorEvaluatorSetArrayArray implements OperatorEvaluator {
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
            if (!TypeArray.is(types[0])) {
                return null;
            }
            if (!TypeArray.is(types[1])) {
                return null;
            }
            Type fromEntryType = TypeArray.as(types[0]).getEntryType();
            Type toEntryType = TypeArray.as(types[1]).getEntryType();
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, fromEntryType, toEntryType);
            if (set == null) {
                return null;
            }
            return new OperatorEvaluatorSetArrayArray(this);
        }
    }

    private final Type resultType;

    private OperatorEvaluatorSetArrayArray(Builder builder) {
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
        ValueArray resultArray = ValueArray.as(result);
        ValueArray operandArray = ValueArray.as(operands[0]);
        if (resultArray == operandArray) {
            return;
        }
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, operandArray.getType().getEntryType(), resultArray.getType().getEntryType());
        int size = operandArray.size();
        Value entryOperand = operandArray.getType().getEntryType().newValue();
        Value resultOperand = resultArray.getType().getEntryType().newValue();
        resultArray.setSize(size);
        for (int index = 0; index < size; index++) {
            operandArray.get(entryOperand, index);
            set.apply(resultOperand, entryOperand);
            resultArray.set(resultOperand, index);
        }
    }
}
