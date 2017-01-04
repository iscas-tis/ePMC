package epmc.qmc.value;

import epmc.error.EPMCException;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArray;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInteger;

public final class UtilValueQMC {
    public static void identityMatrix(ValueArrayAlgebra result, int dimension)
            throws EPMCException {
        int[] dimensions = {dimension, dimension};
        result.setDimensions(dimensions);
        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                Value entry;
                if (row == col) {
                    entry = result.getType().getEntryType().getOne();
                } else {
                    entry = result.getType().getEntryType().getZero();
                }
                result.set(entry, row, col);
            }
        }
    }

    static ValueArray castOrImport(ValueArray orig, Value operand, int number, boolean multiply) {
        if (ValueArray.isArray(operand)) {
            return ValueArray.asArray(operand);
        } else if (orig.getType().getEntryType().canImport(operand.getType())) {
            ValueArray result = orig.getType().newValue();
            result.setDimensions(orig.getDimensions());
            if (multiply) {
                for (int entryNr = 0; entryNr < orig.getLength(0); entryNr++) {
                    result.set(operand, entryNr, entryNr);
                }
            } else {
                for (int entryNr = 0; entryNr < orig.getTotalSize(); entryNr++) {
                    result.set(operand, entryNr);
                }                
            }
            return result;
        } else {
            assert false : orig + " " + operand;
            return null;
        }
    }

    public static void vectorToBra(ValueArray result, ValueArray value, Value dim) throws EPMCException {
        assert !result.isImmutable();
        assert value != null;
        assert dim != null;
        assert value.getNumDimensions() == 1;
        int length = value.getLength(0);
        int newDim[] = {1,length};
        result.setDimensions(newDim);
        Value entryAcc1 = result.getType().getEntryType().newValue();
        Value entryAcc2 = result.getType().getEntryType().newValue();

        for (int entryNr = 0; entryNr < length; entryNr++) {
            value.get(entryAcc1, entryNr);
            conjugate(entryAcc2, entryAcc1);
            result.set(entryAcc2, entryNr);
        }
    }

    public static void toBaseBra(ValueArrayAlgebra result, int number, int dimensions) throws EPMCException {
        assert !result.isImmutable();
        TypeAlgebra typeEntry = result.getType().getEntryType();
        int[] dim = {1, dimensions};
        result.setDimensions(dim);
        for (int entry = 0; entry < dimensions; entry++) {
            result.set(typeEntry.getZero(), entry);
        }
        result.set(typeEntry.getOne(), number);
    }
    
    public static void toBaseBra(ValueArrayAlgebra result, Value number, Value dimensions) throws EPMCException {
        assert !result.isImmutable();
        toBaseBra(result, ValueInteger.asInteger(number).getInt(), ValueInteger.asInteger(dimensions).getInt());
    }

    public static void vectorToKet(ValueArray result, ValueArray value, Value dim) throws EPMCException {
        assert !result.isImmutable();
        assert value != null;
        assert dim != null;
        assert value.getNumDimensions() == 1;
        int length = value.getLength(0);
        int newDim[] = {length, 1};
        result.setDimensions(newDim);
        Value entryAcc1 = result.getType().getEntryType().newValue();
        for (int entryNr = 0; entryNr < length; entryNr++) {
            value.get(entryAcc1, entryNr);
            result.set(entryAcc1, entryNr);
        }
    }

    public static void toBaseKet(ValueArrayAlgebra result, int number, int dimensions) throws EPMCException {
        assert !result.isImmutable();
        TypeAlgebra typeEntry = result.getType().getEntryType();
        int[] dim = {dimensions, 1};
        result.setDimensions(dim);
        for (int entry = 0; entry < dimensions; entry++) {
            result.set(typeEntry.getZero(), entry);
        }
        result.set(typeEntry.getOne(), number);
    }
    
    public static void toBaseKet(ValueArrayAlgebra result, Value number, Value dimensions) throws EPMCException {
        toBaseKet(result, ValueInteger.asInteger(number).getInt(), ValueInteger.asInteger(dimensions).getInt());
    }

    public static void conjugate(Value result, Value op) throws EPMCException {
        assert !result.isImmutable();
        if (ValueArray.isArray(result)) {
            ValueArray opArray = castOrImport(ValueArray.asArray(result), op, 0, false);
            ValueArray resultArray = ValueArray.asArray(result);
            resultArray.setDimensions(opArray.getDimensions());
            Value entryAcc1 = TypeArray.asArray(result.getType()).getEntryType().newValue();
            Value entryAcc3 = TypeArray.asArray(result.getType()).getEntryType().newValue();
            for (int index = 0; index < opArray.getTotalSize(); index++) {
                opArray.get(entryAcc1, index);
                conjugate(entryAcc3, entryAcc1);
                resultArray.set(entryAcc3, index);
            }
        } else if (result instanceof ValueComplex) {
            ValueComplex opComplex = (ValueComplex) UtilValue.clone(op);
            ((ValueComplex) result).getRealPart().set(opComplex.getRealPart());
            ((ValueComplex) result).getImagPart().addInverse(opComplex.getImagPart());
        } else {
            result.set(op);
        }
    }

    private UtilValueQMC() {
    }
}
