package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public final class OperatorEq implements Operator {
    private ContextValue context;
    public final static String IDENTIFIER = "=";

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
    	ValueBoolean.asBoolean(result).set(operands[0].isEq(operands[1]));
    }

    @Override
    public Type resultType(Type... types) {
        Type result = null;
        if (UtilValue.allTypesKnown(types) && UtilValue.upper(types) == null) {
            return null;
        }
        result = TypeBoolean.get(getContext());
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
