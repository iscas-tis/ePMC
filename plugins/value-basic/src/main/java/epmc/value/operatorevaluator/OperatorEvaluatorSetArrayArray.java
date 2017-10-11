package epmc.value.operatorevaluator;

import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetArrayArray implements OperatorEvaluator {
    IDENTIFIER,;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        if (!TypeArray.isArray(types[0])) {
            return false;
        }
        if (!TypeArray.isArray(types[1])) {
            return false;
        }
        Type fromEntryType = TypeArray.asArray(types[0]).getEntryType();
        Type toEntryType = TypeArray.asArray(types[1]).getEntryType();
        OperatorEvaluator set = ContextValue.get().getOperatorEvaluator(OperatorSet.SET, fromEntryType, toEntryType);
        if (set == null) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Type... types) {
        return types[1];
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        ValueArray resultArray = ValueArray.asArray(result);
        ValueArray operandArray = ValueArray.asArray(operands[0]);
        if (resultArray == operandArray) {
            return;
        }
        OperatorEvaluator set = ContextValue.get().getOperatorEvaluator(OperatorSet.SET, operandArray.getType(), resultArray.getType());
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
