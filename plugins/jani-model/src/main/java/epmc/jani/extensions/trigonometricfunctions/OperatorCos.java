package epmc.jani.extensions.trigonometricfunctions;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueTrigonometric;

/**
 * Operator to compute cosinus of a value.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorCos implements Operator {
	/** Identifier of the operator. */
	public final static String IDENTIFIER = "cos";
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
		ValueTrigonometric.asTrigonometric(result).cos(operands[0]);
	}

	@Override
	public Type resultType(Type... types) {
		assert types != null;
		return UtilValue.algebraicResultNonIntegerType(this, types);
	}
}
