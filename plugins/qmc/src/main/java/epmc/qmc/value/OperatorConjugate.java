package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeUnknown;
import epmc.value.UtilValue;
import epmc.value.Value;

public class OperatorConjugate implements Operator {
    public final static String IDENTIFIER = "conjugate";
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
        UtilValueQMC.conjugate(result, operands[0]);
    }

    @Override
    public Type resultType(Type... types) {
        Type result = null;
        Type upper = upper(types);
        if (!TypeUnknown.isUnknown(upper) && !TypeAlgebra.isAlgebra(upper)) {
            return null;
        } else {
            result = upper;
        }
        return result;
    }

    private Type upper(Type... types) {
        Type upper = types[0];
        for (Type type : types) {
            if (upper != null) {
                upper = UtilValue.upper(upper, type);
            }
        }
        return upper;
    }    

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
