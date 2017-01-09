package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public final class OperatorId implements Operator {
    private ContextValue context;
    public final static String IDENTIFIER = "id";

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
        result.set(operands[0]);
    }

    @Override
    public Type resultType(Type... types) {
        return types[0];
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
