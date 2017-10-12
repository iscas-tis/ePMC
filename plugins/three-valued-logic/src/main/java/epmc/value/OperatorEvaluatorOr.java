package epmc.value;

import epmc.value.operator.OperatorOr;

public final class OperatorEvaluatorOr implements OperatorEvaluator {

    @Override
    public Operator getOperator() {
        return OperatorOr.OR;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        for (Type type : types) {
            assert type != null;
        }
        if (types.length != 2) {
            return false;
        }
        /* if both operands are standard boolean, should be handled by
         * standard boolean operator evaluators. */
        if (TypeBoolean.is(types[0]) && TypeBoolean.is(types[1])) {
            return false;
        }
        if (!TypeBoolean.is(types[0]) && !TypeTernary.is(types[0])) {
            return false;
        }
        if (!TypeBoolean.is(types[1]) && !TypeTernary.is(types[1])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Type... types) {
        for (Type type : types) {
            assert type != null;
        }
        assert types.length == 2;
        return TypeTernary.get();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        assert operands.length == 2;
        Ternary op1 = UtilTernary.getTernary(operands[0]);
        Ternary op2 = UtilTernary.getTernary(operands[1]);
        ValueTernary.as(result).set(apply(op1, op2));
    }

    private Ternary apply(Ternary op1, Ternary op2) {
        assert op1 != null;
        assert op2 != null;
        if (op1.isTrue() || op2.isTrue()) {
            return Ternary.TRUE;
        } else if (op1.isFalse() && op2.isFalse()) {
            return Ternary.FALSE;
        } else {
            return Ternary.UNKNOWN;
        }
    }
}
