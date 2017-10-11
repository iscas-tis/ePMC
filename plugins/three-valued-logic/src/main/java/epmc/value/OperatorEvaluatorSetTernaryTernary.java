package epmc.value;

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetTernaryTernary implements OperatorEvaluator {
    IDENTIFIER,;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        assert types.length == 2;
        if (!TypeTernary.isTernary(types[0])) {
            return false;
        }
        if (!TypeTernary.isTernary(types[1])) {
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
        ValueTernary resultTernary = ValueTernary.asTernary(result);
        ValueTernary operandBoolean = ValueTernary.asTernary(operands[0]);
        resultTernary.set(operandBoolean.getTernary());
    }
}
