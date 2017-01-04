package epmc.jani.extensions.derivedoperators;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueInteger;

/**
 * Operator to compute signum of a value.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorSgn implements Operator {
	/** Identifier of the operator. */
	public final static String IDENTIFIER = "sgn";
	/** Context of this operator. */
	private ContextValue context;
	/** Zero - value returned if value equals zero. */
	private Value zero;
	/** One - value returned if value is greater than zero. */
	private Value one;
	/** Minus one - value returned if value is smaller than zero. */
	private ValueInteger minusOne;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setContext(ContextValue context) {
		assert context != null;
		this.context = context;
		zero = TypeInteger.get(context).getZero();
		one = TypeInteger.get(context).getOne();
		minusOne = TypeInteger.get(context).newValue();
		minusOne.addInverse(one);
	}

	@Override
	public ContextValue getContext() {
		return context;
	}

	@Override
	public void apply(Value result, Value... operands) throws EPMCException {
		assert result != null;
		assert operands != null;
		assert operands.length >= 1;
		assert operands[0] != null;
		if (operands[0].isEq(zero)) {
			result.set(zero);
		} else if (ValueAlgebra.asAlgebra(operands[0]).isGt(zero)) {
			result.set(one);
		} else if (ValueAlgebra.asAlgebra(operands[0]).isLt(zero)) {
			result.set(minusOne);
		} else {
			assert false;
		}
	}

	@Override
	public Type resultType(Type... types) {
		assert types != null;
		assert types.length >= 1;
		assert types[0] != null;
		return TypeInteger.get(context);
	}
}
