package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.qmc.options.OptionsQMC;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.Value;

public class OperatorSuperOperator implements Operator {
    public final static String IDENTIFIER = "superoperator";
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
        ValueSuperOperator resultSuperOperator = (ValueSuperOperator) result;
        resultSuperOperator.superoperator(operands[0]);
    }

    @Override
    public Type resultType(Type... types) {
        int hilbert = TypeSuperOperator.DIMENSIONS_UNSPECIFIED;
        ContextValueQMC contextValueQMC = getContext().getOptions().get(OptionsQMC.CONTEXT_VALUE_QMC);
        assert contextValueQMC != null;
        Type result = contextValueQMC.getTypeSuperOperator(hilbert);
        return result;
    }

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
