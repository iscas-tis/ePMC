package epmc.rddl.value;

import epmc.error.EPMCException;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeWeight;
import epmc.value.Value;
import epmc.value.ValueAlgebra;

public class OperatorDistributionBernoulli implements Operator {
    public final static String IDENTIFIER = "rddl-distribution-bernoulli";
    private ContextValue context;
    private ContextValueRDDL contextValueRDDL;
    private TypeBoolean typeBoolean;
    private TypeAlgebra typeWeight;
    private Value zero;
    private Value valueTrue;
    private Value valueFalse;
    private ValueAlgebra helper;

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
        this.typeBoolean = TypeBoolean.get(context);
        this.valueTrue = typeBoolean.getTrue();
        this.valueFalse = typeBoolean.getFalse();
        this.helper = typeWeight.newValue();
        this.zero = typeWeight.getZero();
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
        resultD.setWeight(operands[0], 0);
        helper.subtract(zero, operands[0]);
        resultD.setWeight(helper, 1);
        resultD.setSupport(valueTrue, 0);
        resultD.setSupport(valueFalse, 1);
    }

    @Override
    public Type resultType(Type... types) {
    	assert contextValueRDDL != null;
        return contextValueRDDL.getTypeDistributionFinite(2, typeBoolean);
    }
    
    @Override
    public String toString() {
        return IDENTIFIER;
    }
}
