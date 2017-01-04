package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.Value;
import epmc.value.ValueArray;

public class OperatorTranspose implements Operator {
    public final static String IDENTIFIER = "transpose";
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
        Value operand = operands[0];
        assert !result.isImmutable();
        ValueArray operandArray = UtilValueQMC.castOrImport(ValueArray.asArray(result), operand, 0, false);
        assert operandArray.getNumDimensions() == 2;
        Value resultBuffer = null;
        if (result == operandArray) {
            if (resultBuffer == null) {
                resultBuffer = result.getType().newValue();
            }
            apply(resultBuffer, operand);
            result.set(resultBuffer);
        }
        ValueArray resultArray = ValueArray.asArray(result);
        Value entryAcc1 = TypeArray.asArray(resultArray.getType()).getEntryType().newValue();
        int[] dims = {operandArray.getLength(1), operandArray.getLength(0)};
        resultArray.setDimensions(dims);
        for (int row = 0; row < operandArray.getLength(0); row++) {
            for (int col = 0; col < operandArray.getLength(1); col++) {
                operandArray.get(entryAcc1, row, col);
                resultArray.set(entryAcc1, col, row);
            }
        }
    }

    @Override
    public Type resultType(Type... types) {
        Type result;
        if (!TypeArray.isArray(types[0])) {
            return null;
        } else {
            result = types[0];
        }
        return result;
    }

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
