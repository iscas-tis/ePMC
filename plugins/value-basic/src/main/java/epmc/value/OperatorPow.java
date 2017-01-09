package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorPow implements Operator {
    private ContextValue context;
    public final static String IDENTIFIER = "pow";

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
        ValueReal.asReal(result).pow(operands[0], operands[1]);
    }

    @Override
    public Type resultType(Type... types) {
        assert types != null;
        assert types.length == 2 : types.length;
        Type upper = UtilValue.upper(types);
        if (upper == null) {
            return null;
        }
        if (!TypeUnknown.isUnknown(types[0]) && !TypeReal.isReal(types[0])
                || !TypeUnknown.isUnknown(types[1]) && !TypeReal.isReal(types[1])) {
            return null;
        }
        Type result;
        result = upper;
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
