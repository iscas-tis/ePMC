package epmc.value.operatorevaluator;

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetDoubleDouble implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        assert types.length == 2;
        if (!TypeDouble.is(types[0])) {
            return false;
        }
        if (!TypeDouble.is(types[1])) {
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
        ValueDouble.as(result).set(ValueDouble.as(operands[0]).getDouble());
    }
}
