package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeInteger;
import epmc.value.TypeUnknown;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public class OperatorBaseKet implements Operator {
    public final static String IDENTIFIER = "base-ket";
    private ContextValue context;
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setContext(ContextValue context) {
    	assert context != null;
        this.context = context;
    }

    @Override
    public ContextValue getContext() {
        return context;
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
        assert !result.isImmutable();
        ValueArray op1 = ValueArray.asArray(operands[0]);
        ValueArray op2 = ValueArray.asArray(operands[1]);
        Value resultBuffer = null;
        if (result == op1 || result == op2) {
            if (resultBuffer == null) {
                resultBuffer = result.getType().newValue();
            }
            apply(resultBuffer, op1, op2);
            result.set(resultBuffer);
            
        }
        if (ValueArray.isArray(op1)) {
            UtilValueQMC.vectorToKet(ValueArray.asArray(result), op1, op2);
        } else {
            UtilValueQMC.toBaseKet(ValueArrayAlgebra.asArrayAlgebra(result), op1, op2);
        }
    }

    @Override
    public Type resultType(Type... types) {
        Type result;
        assert types != null;
        assert this.context != null;
        if (!TypeUnknown.isUnknown(types[0]) && TypeArray.isArray(types[0])) {
            result = types[0];
        } else if (!TypeUnknown.isUnknown(types[0]) && !TypeArray.isArray(types[0])) {
            result = TypeInteger.get(getContext()).getTypeArray();
        } else {
        	result = null;
        }
        return result;
    }

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
