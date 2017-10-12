package epmc.jani.value;

import epmc.jani.explorer.TypeDecision;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetLocationLocation implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        assert types.length == 2;
        if (!TypeDecision.is(types[0])) {
            return false;
        }
        if (!TypeLocation.is(types[1])) {
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
        int number = ValueLocation.as(operands[0]).getValueNumber();
        ValueLocation.as(result).setValueNumber(number);
    }
}
