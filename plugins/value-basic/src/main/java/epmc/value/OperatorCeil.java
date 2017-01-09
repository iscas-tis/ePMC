package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorCeil implements Operator {
    private ContextValue context;
    public final static String IDENTIFIER = "ceil"; //"⌈⌉";

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
        ValueAlgebra.asAlgebra(result).set(ValueReal.asReal(operands[0]).ceilInt());
    }

    @Override
    public Type resultType(Type... types) {
        Type result;
        if (!TypeUnknown.isUnknown(types[0]) && !TypeReal.isReal(types[0])) {
            return null;
        }
        result = TypeInteger.get(getContext());
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
