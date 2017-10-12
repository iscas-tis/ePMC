package epmc.value.operatorevaluator;

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeEnum;
import epmc.value.Value;
import epmc.value.ValueEnum;
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetEnumEnum implements OperatorEvaluator {
    IDENTIFIER;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        assert types.length == 2;
        if (!TypeEnum.is(types[0])) {
            return false;
        }
        if (!TypeEnum.is(types[1])) {
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
        assert operands.length >= 1;
        ValueEnum.as(result).set((Enum<?>) ValueEnum.as(operands[0]).getEnum());
    }
}
