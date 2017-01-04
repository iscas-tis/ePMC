package epmc.rddl.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public class OperatorDistributionNormal implements Operator {
    public final static String IDENTIFIER = "rddl-distribution-normal";
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
        ValueDistributionNormal resultD = (ValueDistributionNormal) result;
        resultD.setMean(operands[0]);
        resultD.setVariance(operands[1]);
    }

    @Override
    public Type resultType(Type... types) {
    	assert contextValueRDDL != null;
        return contextValueRDDL.getTypeNormal();
    }
    
    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
