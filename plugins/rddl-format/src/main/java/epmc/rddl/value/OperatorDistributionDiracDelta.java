package epmc.rddl.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeWeight;
import epmc.value.Value;

public class OperatorDistributionDiracDelta implements Operator {
    public final static String IDENTIFIER = "rddl-distribution-dirac-delta";
    private ContextValue context;
    private ContextValueRDDL contextValueRDDL;
    private TypeAlgebra typeWeight;
    private Value one;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setContext(ContextValue context) {
        assert context != null;
        assert this.context == null;
        this.context = context;
        this.typeWeight = TypeWeight.get(context);
        this.one = typeWeight.getOne();
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
        resultD.setWeight(one, 0);
        resultD.setSupport(operands[0], 0);
    }

    @Override
    public Type resultType(Type... types) {
        return contextValueRDDL.getTypeDistributionFinite(1, types[0]);
    }
    
    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
