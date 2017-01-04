package epmc.imdp.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInterval;
import epmc.value.ValueReal;

/**
 * Operator to construct an interval from two real numbers.
 * 
 * @author Ernst Moritz Hahn
 */
public final class OperatorInterval implements Operator {
	public final static String IDENTIFIER = "interval";
	private ContextValue context;
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setContext(ContextValue context) {
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
		assert operands.length >= 2;
		assert operands[0] != null;
		assert operands[1] != null;
		assert ValueReal.isReal(operands[0]);
		assert ValueReal.isReal(operands[1]);
		assert ValueInterval.isInterval(result);
		ValueInterval.asInterval(result).getIntervalLower().set(operands[0]);
		ValueInterval.asInterval(result).getIntervalUpper().set(operands[1]);
	}

	@Override
	public Type resultType(Type... types) {
		assert context != null;
		assert types != null;
		assert types.length >= 2;
		assert types[0] != null;
		assert types[1] != null;
		assert TypeReal.isReal(types[0]);
		assert TypeReal.isReal(types[1]);
		return TypeInterval.get(context);
	}
}
