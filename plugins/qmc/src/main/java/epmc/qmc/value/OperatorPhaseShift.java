package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.qmc.options.OptionsQMC;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.TypeUnknown;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueTrigonometric;

public class OperatorPhaseShift implements Operator {
    public final static String IDENTIFIER = "phase-shift";
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
        Value phase = operands[0];
        assert !result.isImmutable();
        assert phase != null;
        ContextValueQMC contextValueQMC = getContext().getOptions().get(OptionsQMC.CONTEXT_VALUE_QMC);
        TypeComplex typeComplex = contextValueQMC.getTypeComplex();
        Value phaseReal = TypeReal.get(getContext()).newValue();
        phaseReal.set(phase);
        
        Value one = typeComplex.getOne();
        ValueArray resultArray = ValueArray.asArray(result);
        resultArray.setDimensions(2, 2);
        resultArray.set(one, 0, 0);
        Value cos = TypeReal.get(getContext()).newValue();
        ValueTrigonometric.asTrigonometric(cos).cos(phaseReal);
        Value sin = TypeReal.get(getContext()).newValue();
        ValueTrigonometric.asTrigonometric(sin).sin(phaseReal);
        ValueComplex complexAcc = contextValueQMC.getTypeComplex().newValue();
        complexAcc.getRealPart().set(cos);
        complexAcc.getImagPart().set(sin);
        resultArray.set(complexAcc, 1, 1);
        resultArray.set(typeComplex.getZero(), 1, 0);
        resultArray.set(typeComplex.getZero(), 0, 1);
    }

    @Override
    public Type resultType(Type... types) {
        if (!TypeUnknown.isUnknown(types[0]) && !TypeReal.isReal(types[0])) {
            return null;
        }
        ContextValueQMC contextValueQMC = getContext().getOptions().get(OptionsQMC.CONTEXT_VALUE_QMC);
        Type result = contextValueQMC.getTypeComplex().getTypeArray();
        return result;
    }

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
