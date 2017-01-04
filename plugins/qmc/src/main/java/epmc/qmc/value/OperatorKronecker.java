package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeUnknown;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;

public class OperatorKronecker implements Operator {
    public final static String IDENTIFIER = "kronecker";
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
        if (!ValueArray.isArray(result)) {
        	ValueAlgebra.asAlgebra(result).multiply(operands[0], operands[1]);
            return;
        }
        ValueArray resultBuffer = null;
        Value op1 = operands[0];
        Value op2 = operands[1];
        ValueArray op1Array = UtilValueQMC.castOrImport(ValueArray.asArray(result), op1, 0, true);
        ValueArray op2Array = UtilValueQMC.castOrImport(ValueArray.asArray(result), op2, 1, true);
        if (this == op1 || this == op2) {
            if (resultBuffer == null) {
                resultBuffer = (ValueArray) result.getType().newValue();
            }
            apply(resultBuffer, op1Array, op2Array);
            result.set(resultBuffer);
            
        }
        Value entryAcc1 = TypeArray.asArray(result.getType()).getEntryType().newValue();
        Value entryAcc2 = TypeArray.asArray(result.getType()).getEntryType().newValue();
        Value entryAcc3 = TypeArray.asArray(result.getType()).getEntryType().newValue();
        int[] dim = {op1Array.getLength(0) * op2Array.getLength(0),
                op1Array.getLength(1) * op2Array.getLength(1)};
        ValueArray resultArray = ValueArray.asArray(result);
        resultArray.setDimensions(dim);
        for (int row = 0; row < resultArray.getLength(0); row++) {
            for (int column = 0; column < resultArray.getLength(1); column++) {
                op1Array.get(entryAcc1, row / op2Array.getLength(0), column / op2Array.getLength(1));
                op2Array.get(entryAcc2, row % op2Array.getLength(0), column % op2Array.getLength(1));
                ValueAlgebra.asAlgebra(entryAcc3).multiply(entryAcc1, entryAcc2);
                resultArray.set(entryAcc3, row, column);
            }
        }
    }
    
    @Override
    public Type resultType(Type... types) {
        if (!TypeUnknown.isUnknown(types[0]) && !TypeArray.isArray(types[0])) {
            return null;
        }
        if (!TypeUnknown.isUnknown(types[1]) && !TypeArray.isArray(types[1])) {
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
