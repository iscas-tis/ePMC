package epmc.rddl.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.ValueInteger;

public class OperatorDistributionDiscrete implements Operator {
    public final static String IDENTIFIER = "rddl-distribution-discrete";
    private ContextValue context;
    private ContextValueRDDL contextValueRDDL;

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
        this.contextValueRDDL = contextValueRDDL;
    }

    @Override
    public ContextValue getContext() {
        return this.context;
    }

    @Override
    public void apply(Value result, Value... operands) throws EPMCException {
        ValueDistributionFiniteExplicit resultD = (ValueDistributionFiniteExplicit) result;
        int supportSize = ValueInteger.asInteger(operands[1]).getInt();
        for (int i = 0; i < supportSize; i++) {
            resultD.setSupport(operands[2 + i * 2], i);
            resultD.setWeight(operands[2 + i * 2 + 1], i);
        }
    }

    @Override
    public Type resultType(Type... types) {
        return contextValueRDDL.getTypeDistributionFinite(types.length / 2, types[0]);
    }

    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
