package epmc.jani.extensions.derivedoperators;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueNumber;

/**
 * Operator to truncate a real to an integer.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorTrunc implements Operator {
	/** Identifier of the operator. */
	public final static String IDENTIFIER = "trunc";
	/** Context of this operator. */
	private ContextValue context;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setContext(ContextValue context) {
		assert context != null;
		this.context = context;
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
		ValueAlgebra.asAlgebra(result).set(ValueNumber.asNumber(operands[0]).intcastInt());
	}

	@Override
	public Type resultType(Type... types) {
		assert types != null;
		assert types.length >= 1;
		assert types[0] != null;
		return TypeInteger.get(context);
	}
}
