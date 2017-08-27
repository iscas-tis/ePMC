package epmc.value;

import epmc.error.EPMCException;
import epmc.value.operator.OperatorIff;

public final class OperatorEvaluatorIff implements OperatorEvaluator {

	@Override
	public Operator getOperator() {
		return OperatorIff.IFF;
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
		if (TypeBoolean.isBoolean(types[0]) && TypeBoolean.isBoolean(types[1])) {
			return false;
		}
		if (!TypeBoolean.isBoolean(types[0]) && !TypeTernary.isTernary(types[0])) {
			return false;
		}
		if (!TypeBoolean.isBoolean(types[1]) && !TypeTernary.isTernary(types[1])) {
			return false;
		}
		return true;
	}

	@Override
	public Type resultType(Operator operator, Type... types) {
		assert operator != null;
		for (Type type : types) {
			assert type != null;
		}
		assert types.length == 2;
		return TypeTernary.get();
	}

	@Override
	public void apply(Value result, Value... operands) throws EPMCException {
		assert result != null;
		assert operands != null;
		for (Value operand : operands) {
			assert operand != null;
		}
		assert operands.length == 2;
		Ternary op1 = UtilTernary.getTernary(operands[0]);
		Ternary op2 = UtilTernary.getTernary(operands[1]);
		ValueTernary.asTernary(result).set(apply(op1, op2));
	}

	private Ternary apply(Ternary op1, Ternary op2) {
		assert op1 != null;
		assert op2 != null;
        if (op1.isKnown() && op2.isKnown()) {
            return op1 == op2 ? Ternary.TRUE : Ternary.FALSE;
        } else {
            return Ternary.UNKNOWN;
        }
	}
}