package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorIte implements Operator {
    private ContextValue context;
    /** If-then-else, ternary operator. */
    public final static String IDENTIFIER = "ite"; //"?:";

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
    	if (ValueBoolean.asBoolean(operands[0]).getBoolean()) {
    		result.set(operands[1]);
    	} else {
    		result.set(operands[2]);    		
    	}
    }

    @Override
    public Type resultType(Type... types) {
        assert types != null;
        assert types[0] != null;
        assert types[1] != null;
        assert types[2] != null;
        if (!TypeUnknown.isUnknown(types[0]) && !TypeBoolean.isBoolean(types[0])) {
            return null;
        }
        Type itUpper = UtilValue.upper(types[1], types[2]);
        if (itUpper == null) {
            return null;
        }
        Type result = itUpper;
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
