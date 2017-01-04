package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeUnknown;
import epmc.value.Value;
import epmc.value.ValueArray;

public class OperatorKetToVector implements Operator {
    public final static String IDENTIFIER = "ket-to-vector";
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
        assert !result.isImmutable();
        Value operand = operands[0];
        Value resultBuffer = null;
        if (result == operand) {
            if (resultBuffer == null) {
                resultBuffer = result.getType().newValue();
            }
            apply(resultBuffer, operand);
            result.set(resultBuffer);
            
        }
        assert operand != null;
        ValueArray operandArray = (ValueArray) operand;
        assert operandArray.getNumDimensions() == 2;
        assert operandArray.getLength(1) == 1;
        int length = operandArray.getLength(0);
        int[] dims = {length};
        ValueArray resultArray = ValueArray.asArray(result);
        resultArray.setDimensions(dims);
        Value entryAcc1 = TypeArray.asArray(result.getType()).getEntryType().newValue();
        for (int index = 0; index < length; index++) {
            operandArray.get(entryAcc1, index);
            resultArray.set(entryAcc1, index);
        }
        
    }

    @Override
    public Type resultType(Type... types) {
        if (!TypeUnknown.isUnknown(types[0]) && !TypeArray.isArray(types[0])) {
            return null;
        }
        Type result = types[0];
        return result;
    }

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
