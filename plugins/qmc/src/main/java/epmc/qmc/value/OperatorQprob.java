package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.qmc.options.OptionsQMC;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.TypeReal;
import epmc.value.TypeUnknown;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueReal;

public class OperatorQprob implements Operator {
    public final static String IDENTIFIER = "qprob";
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
        Value superoperator = operands[0];
        Value operator = operands[1];
        ValueArray resultBuffer = null;
        if (result == superoperator || result == operator) {
            if (resultBuffer == null) {
                resultBuffer = (ValueArray) result.getType().newValue();
            }
            apply(resultBuffer, superoperator, operator);
            result.set(resultBuffer);
            
        }
        TypeReal typeReal = TypeReal.get(getContext());
        ContextValueQMC contextValueQMC = getContext().getOptions().get(OptionsQMC.CONTEXT_VALUE_QMC);
        Type typeArrayComplex = contextValueQMC.getTypeComplex().getTypeArray();
        ValueArray qprobQevalResult = (ValueArray) typeArrayComplex.newValue();
        ValueReal qprobAcc = typeReal.newValue();

        getContext().getOperator(OperatorQeval.IDENTIFIER).apply(qprobQevalResult, superoperator, operator);
        Value[] eigen = Eigen.eigenvalues(qprobQevalResult);
        result.set(typeReal.getZero());
        // TODO check!
        for (Value entry : eigen) {
            qprobAcc.add(result, entry);
            result.set(qprobAcc);
        }
        
    }

    @Override
    public Type resultType(Type... types) {
        if (!TypeUnknown.isUnknown(types[0]) && !(types[0] instanceof TypeSuperOperator)
                && !TypeAlgebra.isAlgebra(types[0])) {
            return null;
        }
        if (!TypeUnknown.isUnknown(types[1]) && !TypeArray.isArray(types[1])
                && !TypeAlgebra.isAlgebra(types[1])) {
            return null;
        }
        Type result = TypeReal.get(getContext());
        return result;
    }

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
