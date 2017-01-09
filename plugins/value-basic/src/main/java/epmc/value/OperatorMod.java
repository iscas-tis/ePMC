package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorMod implements Operator {
    private ContextValue context;
    public final static String IDENTIFIER = "%";

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
        ValueInteger.asInteger(result).mod(operands[0], operands[1]);
    }

    @Override
    public Type resultType(Type... types) {
        Type result;
        if (!TypeUnknown.isUnknown(types[0]) && !TypeInteger.isInteger(types[0])
                || !TypeUnknown.isUnknown(types[1]) && !TypeInteger.isInteger(types[1])) {
            return null;
        } else {
            result = TypeInteger.get(getContext());
        }
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
