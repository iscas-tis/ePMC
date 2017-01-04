package epmc.rddl.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeEnumerable;
import epmc.value.UtilValue;
import epmc.value.Value;

public class OperatorSwitch implements Operator {
    public final static String IDENTIFIER = "rddl-switch";
    private ContextValue context;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setContext(ContextValue context) {
        assert context != null;
        assert this.context == null;
        this.context = context;
    }
    
    public void setContextValueRDDL(ContextValueRDDL contextValueRDDL) {
    }

    @Override
    public ContextValue getContext() {
        return this.context;
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
    	Value compare = operands[0];
    	int numAlternatives = TypeEnumerable.asEnumerable(compare.getType()).getNumValues();
    	for (int alternative = 0; alternative < numAlternatives; alternative++) {
    		Value compareWith = operands[1 + alternative * 2];
    		if (compare.isEq(compareWith)) {
    			result.set(operands[1 + alternative * 2 + 1]);
    			return;
    		}
    	}
    	assert false : Arrays.toString(operands);
    }

    @Override
    public Type resultType(Type... types) {
    	Type type = types[2];
    	for (int alternative = 0; 1 + alternative * 2 + 1 < types.length; alternative++) {
    		type = UtilValue.upper(type, types[1 + alternative * 2 + 1]);
    	}
    	assert type != null;
        return type;
    }
    
    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
