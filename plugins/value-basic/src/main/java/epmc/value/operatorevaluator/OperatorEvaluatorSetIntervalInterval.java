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
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetIntervalInterval implements OperatorEvaluator {
    IDENTIFIER,;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        if (!TypeInterval.isInterval(types[0])) {
            return false;
        }
        if (!TypeInterval.isInterval(types[1])) {
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
        ValueInterval resultInterval = ValueInterval.asInterval(result);
        ValueInterval operandInterval = ValueInterval.asInterval(operands[0]);
        OperatorEvaluator set = ContextValue.get().getOperatorEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        set.apply(resultInterval.getIntervalLower(), operandInterval.getIntervalLower());
        set.apply(resultInterval.getIntervalUpper(), operandInterval.getIntervalUpper());
    }
}
