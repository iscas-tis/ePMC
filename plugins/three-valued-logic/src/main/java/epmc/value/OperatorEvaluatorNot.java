package epmc.value;

import epmc.value.operator.OperatorNot;

public final class OperatorEvaluatorNot implements OperatorEvaluator {

    @Override
    public Operator getOperator() {
        return OperatorNot.NOT;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        if (types.length != 1) {
            return false;
        }
        if (!TypeTernary.isTernary(types[0])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Type... types) {
        for (Type type : types) {
            assert type != null;
        }
        assert types.length == 1;
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
        ValueTernary.asTernary(result).set(apply(operand));
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
