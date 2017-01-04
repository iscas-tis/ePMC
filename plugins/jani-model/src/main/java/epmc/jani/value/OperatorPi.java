package epmc.jani.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueReal;

/**
 * Operator representing the Euler constant e.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorPi implements Operator {
	/** Identifier of the operator. */
	public final static String IDENTIFIER = "π"; //"e";
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
		assert operands.length == 0;
		ValueReal.asReal(result).pi();
	}

	@Override
	public Type resultType(Type... types) {
		assert types != null;
		assert types.length == 0;
		return TypeReal.get(context);
	}
}
