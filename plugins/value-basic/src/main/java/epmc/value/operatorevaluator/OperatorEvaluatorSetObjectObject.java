package epmc.value.operatorevaluator;

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeObject;
import epmc.value.Value;
import epmc.value.ValueObject;
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetObjectObject implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        assert types.length == 2;
        if (!TypeObject.isObject(types[0])) {
            return false;
        }
        if (!TypeObject.isObject(types[1])) {
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
        ValueObject.asObject(result).set((Object) ValueObject.asObject(operands[0]).getObject());
    }
}
