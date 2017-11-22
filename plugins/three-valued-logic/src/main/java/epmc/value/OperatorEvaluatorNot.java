package epmc.value;

import epmc.operator.Operator;
import epmc.operator.OperatorNot;

public final class OperatorEvaluatorNot implements OperatorEvaluator {

    public Operator getOperator() {
        return OperatorNot.NOT;
    }

    public boolean canApply(Type... types) {
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        if (types.length != 1) {
            return false;
        }
        if (!TypeTernary.is(types[0])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType() {
        return TypeTernary.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        assert operands.length == 1;
        Ternary operand = UtilTernary.getTernary(operands[0]);
        ValueTernary.as(result).set(apply(operand));
    }

    private Ternary apply(Ternary operand) {
        assert operand != null;
        if (operand.isTrue()) {
            return Ternary.FALSE;
        } else if (operand.isFalse()) {
            return Ternary.TRUE;
        } else if (operand.isUnknown()){
            return Ternary.UNKNOWN;
        } else {
            assert false;
            return null;
        }
    }
}
