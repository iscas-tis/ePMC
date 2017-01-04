package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorLog implements Operator {
    private ContextValue context;
    public final static String IDENTIFIER = "log";

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
        Value e = UtilValue.newValue(TypeReal.get(getContext()), UtilValue.LOG);
        ValueReal.asReal(result).log(operands[0], e);
    }

    @Override
    public Type resultType(Type... types) {
        Type upper = UtilValue.upper(types);
        if (!TypeUnknown.isUnknown(upper) && !TypeReal.isReal(upper)) {
            return null;
        }
        Type result = TypeReal.get(getContext());
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
