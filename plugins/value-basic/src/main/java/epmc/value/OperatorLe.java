package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorLe implements Operator {
    private ContextValue context;
    /** Less or equal, a <= b, binary operator. */
    public final static String IDENTIFIER = "≤";

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
    	ValueBoolean.asBoolean(result).set(ValueAlgebra.asAlgebra(operands[0]).isLe(operands[1]));
    }

    @Override
    public Type resultType(Type... types) {
        for (Type type : types) {
            if (!TypeUnknown.isUnknown(type) && !TypeAlgebra.isAlgebra(type)) {
                return null;
            }
        }
        Type result = TypeBoolean.get(getContext());
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
