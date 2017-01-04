package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeInteger;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueInteger;

public class OperatorArray implements Operator {
    public final static String IDENTIFIER = "array";
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
    public void apply(Value result, Value... values) throws EPMCException {
        assert !result.isImmutable();
        assert values.length > 0;
        int numDimensions = ValueInteger.asInteger(values[0]).getInt();
        int[] dimensions = new int[numDimensions];
        int numEntries = 1;
        for (int dim = 0; dim < numDimensions; dim++) {
            int dimSize = ValueInteger.asInteger(values[1 + dim]).getInt();
            dimensions[dim] = dimSize;
            numEntries *= dimSize;
        }
        ValueArray resultArray = ValueArray.asArray(result);
        resultArray.setDimensions(dimensions);
        Value entryAcc1 = TypeArray.asArray(result.getType()).getEntryType().newValue();
        for (int entryNr = 0; entryNr < numEntries; entryNr++) {
            Value entry = values[1 + numDimensions + entryNr];
            entryAcc1.set(entry);
            resultArray.set(entryAcc1, entryNr);
        }
    }

    @Override
    public Type resultType(Type... types) {
    	assert types != null;
    	assert types[0] != null;
    		/* Try to skip dimension information when computing entry type.
    		 * Not doing so and just using the method Value.upper() on all
    		 * entries of the types parameter would not work when having boolean
    		 * etc. arrays for which upper() does not work. If we are working
    		 * with an integer array, we can also not detect where the dimension
    		 * information ends. In this case, this is not a problem however.
    		 * */
    		int whereStart = -1;
    		for (int index = 1; index < types.length; index++) {
    			if (!TypeInteger.isInteger(types[index])) {
    				whereStart = index;
    				break;
    			}
    		}
    		if (whereStart == -1) {
    			whereStart = 1;
    		}
    		Type entryUpper = types[whereStart];
    		for (int index = whereStart; index < types.length; index++) {
    			Type nextUpper = UtilValue.upper(entryUpper, types[index]);
    			if (nextUpper != null) {
    				entryUpper = nextUpper;
    			} else {
    				break;
    			}
    		}
    		Type result = entryUpper.getTypeArray();
    		return result;
    }

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
