package epmc.value.operatorevaluator;

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetIntInt implements OperatorEvaluator {
    IDENTIFIER,;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        if (!TypeInteger.isInteger(types[0])) {
            return false;
        }
        if (!TypeInteger.isInteger(types[1])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Type... types) {
        return TypeInteger.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        ValueInteger.asInteger(result).set(ValueInteger.asInteger(operands[0]).getInt());
    }
}
