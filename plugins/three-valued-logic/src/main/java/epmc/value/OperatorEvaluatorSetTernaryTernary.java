package epmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;

public enum OperatorEvaluatorSetTernaryTernary implements OperatorEvaluator {
    IDENTIFIER,;

    public Operator getOperator() {
        return OperatorSet.SET;
    }

    public boolean canApply(Type... types) {
        assert types != null;
        assert types.length == 2;
        if (!TypeTernary.is(types[0])) {
            return false;
        }
        if (!TypeTernary.is(types[1])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType() {
        return null; // TODO
//        return types[1];
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        ValueTernary resultTernary = ValueTernary.as(result);
        ValueTernary operandBoolean = ValueTernary.as(operands[0]);
        resultTernary.set(operandBoolean.getTernary());
    }
}
