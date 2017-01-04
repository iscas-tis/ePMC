package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.qmc.options.OptionsQMC;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.TypeUnknown;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public class OperatorQeval implements Operator {
    public final static String IDENTIFIER = "qeval";
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
        Value sop = operands[0];
        Value op = operands[1];
        assert !result.isImmutable();
        ValueArray resultBuffer = null;
        if (result == sop || result == op) {
            if (resultBuffer == null) {
                resultBuffer = (ValueArray) result.getType().newValue();
            }
            apply(resultBuffer, sop, op);
            result.set(resultBuffer);
            
        }
        Type typeSuperOperator;
        if (sop instanceof ValueSuperOperator) {
            typeSuperOperator = null;
        } else {
            int hilbertDim;
            if (ValueArray.isArray(op)) {
                ValueArray operatorArray = ValueArray.asArray(op);
                hilbertDim = operatorArray.getLength(0);
            } else {
                hilbertDim = 2;
            }
            ContextValueQMC contextValueQMC = getContext().getOptions().get(OptionsQMC.CONTEXT_VALUE_QMC);
            typeSuperOperator = contextValueQMC.getTypeSuperOperator(hilbertDim);
        }
        ContextValueQMC contextValueQMC = getContext().getOptions().get(OptionsQMC.CONTEXT_VALUE_QMC);
        TypeComplex typeComplex = contextValueQMC.getTypeComplex();
        TypeArrayComplex typeArrayComplex = typeComplex.getTypeArray();
        Value qevalSuperOperator = null;
        if (typeSuperOperator != null) {
            qevalSuperOperator = typeSuperOperator.newValue();            
        }
        ValueArray qevalOperator = typeArrayComplex.newValue();
        ValueArray qevalMaxEntangled = typeArrayComplex.newValue();
        ValueArrayAlgebra qevalId = typeArrayComplex.newValue();
        ValueArray qevalOpKronId = typeArrayComplex.newValue();
        ValueArrayComplex qevalOpKronIdMultMaxEnt = typeArrayComplex.newValue();
        ValueArray qevalMultLeft = typeArrayComplex.newValue();
        ValueArrayComplex qevalMultRight = typeArrayComplex.newValue();
        ValueArrayAlgebra qevalRowBra = typeArrayComplex.newValue();
        ValueArrayAlgebra qevalColBra = typeArrayComplex.newValue();
        ValueArrayComplex qevalEntryArr = typeArrayComplex.newValue();
        Value qevalEntry = typeComplex.newValue();

        Value superOperator;
        ValueArray operator;
        if (sop instanceof ValueSuperOperator) {
            superOperator = sop;
        } else {
            qevalSuperOperator.set(sop);
            superOperator = qevalSuperOperator;
        }
        if (!ValueArray.isArray(op)) {
            int hilbert = ((ValueSuperOperator) superOperator).getSuperoperatorDimensions();
            int dim[] = {hilbert, hilbert};
            qevalOperator.setDimensions(dim);
            for (int row = 0; row < hilbert; row++) {
                for (int col = 0; col < hilbert; col++) {
                    if (row == col) {
                        qevalOperator.set(op, row, col);                        
                    } else {
                        qevalOperator.set(typeComplex.getZero(), row, col);
                    }
                }
            }
            operator = qevalOperator;
        } else {
            qevalOperator.set(op);
            operator = qevalOperator;
        }
        int dimensions = ((ValueSuperOperator) superOperator).getSuperoperatorDimensions();        
        int[] maxEntangledDim = {dimensions * dimensions, 1};
        qevalMaxEntangled.setDimensions(maxEntangledDim);
        for (int entry = 0; entry < dimensions * dimensions; entry++) {
            qevalMaxEntangled.set(typeComplex.getZero(), entry, 0);
        }
        for (int entry = 0; entry < dimensions; entry++) {
            qevalMaxEntangled.set(typeComplex.getOne(), entry + dimensions * entry, 0);
        }
        
        UtilValueQMC.identityMatrix(qevalId, dimensions);
        getContext().getOperator(OperatorKronecker.IDENTIFIER).apply(qevalOpKronId, operator, qevalId);
        qevalOpKronIdMultMaxEnt.multiply(qevalOpKronId, qevalMaxEntangled);
        qevalMultRight.multiply(((ValueSuperOperator) superOperator).getMatrix(), qevalOpKronIdMultMaxEnt);
        int[] dim = {dimensions, dimensions};
        ValueArray resultArray = ValueArray.asArray(result);
        resultArray.setDimensions(dim);
        for (int row = 0; row < dimensions; row++) {
            UtilValueQMC.toBaseBra(qevalRowBra, row, dimensions);
            for (int col = 0; col < dimensions; col++) {
                UtilValueQMC.toBaseBra(qevalColBra, col, dimensions);
                getContext().getOperator(OperatorKronecker.IDENTIFIER).apply(qevalMultLeft, qevalRowBra, qevalColBra);
                qevalEntryArr.multiply(qevalMultLeft, qevalMultRight);
                qevalEntryArr.get(qevalEntry, 0, 0);
                resultArray.set(qevalEntry, row, col);
            }
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
        ContextValueQMC contextValueQMC = getContext().getOptions().get(OptionsQMC.CONTEXT_VALUE_QMC);
        Type result = contextValueQMC.getTypeComplex().getTypeArray();
        return result;
    }

    @Override
    public String toString() {
    	return IDENTIFIER;
    }
}
