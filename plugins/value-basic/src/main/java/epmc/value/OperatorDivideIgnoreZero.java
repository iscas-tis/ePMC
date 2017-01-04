package epmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeUnknown;
import epmc.value.Value;

public final class OperatorDivideIgnoreZero implements Operator {
    private ContextValue context;
    public final static String IDENTIFIER = "divide-ignore-zero";

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
    	if (ValueAlgebra.asAlgebra(operands[1]).isZero()) {
    		result.set(operands[0]);
    	} else {
    		ValueAlgebra.asAlgebra(result).divide(operands[0], operands[1]);
    	}
    }

    @Override
    public Type resultType(Type... types) {
        Type upper = UtilValue.upper(types);
        Type result;
        if (TypeArray.isArray(types[0]) == TypeArray.isArray(types[1])) {
            if (upper == null) {
                return null;
            }
            if (!TypeUnknown.isUnknown(types[0]) && !TypeAlgebra.isAlgebra(types[0])
                    && !TypeArray.isArray(types[0])
                    || !TypeUnknown.isUnknown(types[1]) && !TypeAlgebra.isAlgebra(types[1])
                    && !TypeArray.isArray(types[1])) {
                return null;
            }
            result = upper;
            if (TypeInteger.isInteger(result)) {
                result = TypeReal.get(getContext());
            }
        } else {
            if (TypeArray.isArray(types[1])) {
                return null;
            }
            TypeArray array;
            Type nonArray;
            if (TypeArray.isArray(types[0]) && !TypeArray.isArray(types[1])) {
                array = TypeArray.asArray(types[0]);
                nonArray = types[1];
            } else {
                array = TypeArray.asArray(types[1]);
                nonArray = types[0];
            }                
            Type entryType = array.getEntryType();
            Type entryUpper = UtilValue.upper(entryType, nonArray);
            if (entryUpper == null) {
                return null;
            }
            if (TypeInteger.isInteger(entryUpper)) {
                entryUpper = TypeReal.get(getContext());
            }
            result = entryUpper.getTypeArray();
        }
        return result;
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
