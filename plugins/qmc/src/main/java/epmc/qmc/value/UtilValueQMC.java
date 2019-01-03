package epmc.qmc.value;

import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueInteger;

public final class UtilValueQMC {
    public static void identityMatrix(ValueMatrix result, int dimension) {
        assert result != null;
        assert dimension >= 0;
        result.setDimensions(dimension, dimension);
        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                Value entry;
                if (row == col) {
                    entry = UtilValue.newValue(result.getType().getEntryType(), 1);
                } else {
                    entry = UtilValue.newValue(result.getType().getEntryType(), 0);
                }
                result.set(entry, row, col);
            }
        }
    }

    static ValueMatrix castOrImport(ValueMatrix orig, Value operand, int number, boolean multiply) {
        if (ValueMatrix.is(operand)) {
            return ValueMatrix.as(operand);
        } else {
            ValueMatrix result = orig.getType().newValue();
            result.setDimensions(orig.getNumRows(), orig.getNumColumns());
            if (multiply) {
                for (int entryNr = 0; entryNr < orig.getNumRows(); entryNr++) {
                    result.set(operand, entryNr, entryNr);
                }
            } else {
                for (int entryNr = 0; entryNr < orig.getTotalSize(); entryNr++) {
                    result.getValues().set(operand, entryNr);
                }
            }
            return result;
        }
    }

    public static void vectorToBra(ValueMatrix result, ValueArray value, Value dim) {
        assert value != null;
        assert dim != null;
        int length = value.size();
        result.setDimensions(1,length);
        Value entryAcc1 = result.getType().getEntryType().newValue();
        Value entryAcc2 = result.getType().getEntryType().newValue();

        for (int entryNr = 0; entryNr < length; entryNr++) {
            value.get(entryAcc1, entryNr);
            conjugate(entryAcc2, entryAcc1);
            result.getValues().set(entryAcc2, entryNr);
        }
    }

    public static void toBaseBra(ValueMatrix result, int number, int dimensions) {
        TypeAlgebra typeEntry = result.getType().getEntryType();
        result.setDimensions(1, dimensions);
        for (int entry = 0; entry < dimensions; entry++) {
            result.getValues().set(UtilValue.newValue(typeEntry, 0), entry);
        }
        result.getValues().set(UtilValue.newValue(typeEntry, 1), number);
    }

    public static void toBaseBra(ValueMatrix result, Value number, Value dimensions) {
        assert result != null;
        assert number != null;
        assert dimensions != null;
        toBaseBra(result, ValueInteger.as(number).getInt(), ValueInteger.as(dimensions).getInt());
    }

    public static void vectorToKet(ValueMatrix result, ValueArray value, Value dim) {
        assert value != null;
        assert dim != null;
        int length = value.size();
        result.setDimensions(length, 1);
        Value entryAcc1 = result.getType().getEntryType().newValue();
        for (int entryNr = 0; entryNr < length; entryNr++) {
            value.get(entryAcc1, entryNr);
            result.getValues().set(entryAcc1, entryNr);
        }
    }

    public static void toBaseKet(ValueMatrix result, int number, int dimensions) {
        TypeAlgebra typeEntry = result.getType().getEntryType();
        result.setDimensions(dimensions, 1);
        for (int entry = 0; entry < dimensions; entry++) {
            result.getValues().set(UtilValue.newValue(typeEntry, 0), entry);
        }
        result.getValues().set(UtilValue.newValue(typeEntry, 1), number);
    }

    public static void toBaseKet(ValueMatrix result, Value number, Value dimensions) {
        assert result != null;
        assert number != null;
        assert dimensions != null;
        assert ValueInteger.as(number) != null : number;
        toBaseKet(result, ValueInteger.as(number).getInt(), ValueInteger.as(dimensions).getInt());
    }

    public static void conjugate(Value result, Value op) {
        if (ValueMatrix.is(result)) {
            ValueMatrix opMatrix = castOrImport(ValueMatrix.as(result), op, 0, false);
            ValueMatrix resultMatrix = ValueMatrix.as(result);
            resultMatrix.setDimensions(opMatrix.getNumRows(), opMatrix.getNumColumns());
            Value entryAcc1 = TypeMatrix.as(result.getType()).getEntryType().newValue();
            Value entryAcc3 = TypeMatrix.as(result.getType()).getEntryType().newValue();
            for (int index = 0; index < opMatrix.getTotalSize(); index++) {
                opMatrix.getValues().get(entryAcc1, index);
                conjugate(entryAcc3, entryAcc1);
                resultMatrix.getValues().set(entryAcc3, index);
            }
        } else if (ValueComplex.is(result)) {
            ValueComplex resultComplex = ValueComplex.as(result);
            OperatorEvaluator addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, TypeReal.get());
            ValueComplex opComplex = UtilValue.clone(ValueComplex.as(op));
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
            set.apply(resultComplex.getRealPart(), opComplex.getRealPart());
            addInverse.apply(resultComplex.getImagPart(), opComplex.getImagPart());
        } else {
            OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, op.getType(), result.getType());
            set.apply(result, op);
        }
    }

    private UtilValueQMC() {
    }
}
