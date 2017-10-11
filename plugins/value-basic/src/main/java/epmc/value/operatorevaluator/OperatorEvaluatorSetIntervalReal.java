package epmc.value.operatorevaluator;

import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetIntervalReal implements OperatorEvaluator {
    IDENTIFIER,;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        if (!TypeReal.isReal(types[0])) {
            return false;
        }
        if (!TypeInterval.isInterval(types[1])) {
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
        ValueInterval resultInterval = ValueInterval.asInterval(result);
        ValueReal operandReal = ValueReal.asReal(operands[0]);
        OperatorEvaluator set = ContextValue.get().getOperatorEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        set.apply(resultInterval.getIntervalLower(), operandReal);
        set.apply(resultInterval.getIntervalUpper(), operandReal);
    }
}
