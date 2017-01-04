package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public final class OperatorAddInverse implements Operator {
    private ContextValue context;
    /** Additive inverse, -a, unary operator. */
    public final static String IDENTIFIER = "--";

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
        ValueAlgebra.asAlgebra(result).addInverse(operands[0]);
    }

    @Override
    public Type resultType(Type... types) {
        return UtilValue.algebraicResultType(this, types);
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
