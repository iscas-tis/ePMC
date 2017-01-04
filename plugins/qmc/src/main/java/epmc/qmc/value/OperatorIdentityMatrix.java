package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInteger;

public class OperatorIdentityMatrix implements Operator {
    public final static String IDENTIFIER = "identity-matrix";
    private ContextValue context;
    
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
        UtilValueQMC.identityMatrix(ValueArrayAlgebra.asArrayAlgebra(result), ValueInteger.asInteger(operands[0]).getInt());
    }

    @Override
    public Type resultType(Type... types) {
        Type result = null;
        if (!TypeInteger.isInteger(types[0])) {
            return null;
        } else {
            result = TypeInteger.get(getContext()).getTypeArray();
        }
        return result;
    }

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
