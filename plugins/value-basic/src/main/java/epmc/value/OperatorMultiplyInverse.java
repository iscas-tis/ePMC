package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorMultiplyInverse implements Operator {
    private ContextValue context;
    /** Multiplicative inverse, 1/a, unary operator. */
    public final static String IDENTIFIER = "multiply-inverse";

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
        ValueAlgebra.asAlgebra(result).multInverse(operands[0]);
    }

    @Override
    public Type resultType(Type... types) {
        Type upper = UtilValue.upper(types);
        Type result;
        if (UtilValue.allTypesKnown(types) && upper == null) {
            return null;
        } else {
            if (!TypeUnknown.isUnknown(upper) && TypeAlgebra.isAlgebra(upper)) {
                result = upper;
            } else if (TypeArray.isArray(upper)) {
                // TODO dimensions check
                result = upper;
            } else {
                return null;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
