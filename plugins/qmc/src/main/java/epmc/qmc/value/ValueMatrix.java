package epmc.qmc.value;

import java.util.Arrays;

import epmc.operator.OperatorDivide;
import epmc.operator.OperatorMultiply;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;

//TODO most functionality still here to be moved to OperatorEvaluators
public final class ValueMatrix implements ValueAlgebra {
    private final static String SPACE = " ";
    private final static String ENDL = "\n";
    private final static String UNSPEC_STR = "matrix-unspec-dim(%s)";
    /**
     * If number of rows and columns is set to {@link #UNSPECIFIED_DIMENSIONS},
     * then the dimensions of the matrix are not specified. In this case, the
     * array representing the matrix should contain a single element, and the
     * matrix should behave like a number of its field.
     */
    private final static int UNSPECIFIED_DIMENSIONS = -1;

    private final TypeMatrix type;
    private ValueArrayAlgebra values;
    private int numRows;
    private int numColumns;
    private ValueAlgebra[] entryAccs = new ValueAlgebra[0];

    public final static boolean is(Value value) {
        return value instanceof ValueMatrix;
    }

    public static ValueMatrix as(Value result) {
        if (is(result)) {
            return (ValueMatrix) result;
        } else {
            return null;
        }
    }

    public ValueMatrix(TypeMatrix type) {
        assert type != null;
        this.type = type;
        this.values = UtilValue.newArray(type.getArrayType(), 1);
        setDimensionsUnspecified();
    }

    @Override
    public TypeMatrix getType() {
        return type;
    }

    private ValueAlgebra getEntryAcc(int number) {
        assert number >= 0;
        int numEntryAccs = this.entryAccs.length;
        if (numEntryAccs == 0) {
            numEntryAccs = 1;
        }
        while (number >= numEntryAccs) {
            numEntryAccs *= 2;
        }
        if (numEntryAccs != this.entryAccs.length) {
            this.entryAccs = Arrays.copyOf(this.entryAccs, numEntryAccs);
        }
        if (this.entryAccs[number] == null) {
            this.entryAccs[number] = getType().getEntryType().newValue();
        }
        return this.entryAccs[number];
    }

    @Override
    public void set(int value) {
        setDimensionsUnspecified();
        values.set(value, 0);
    }

    public void set(Value value, int row, int column) {
        assert value != null;
        values.set(value, rowColumnToIndex(row, column));
    }

    public final void setDimensions(int numRows, int numColumns) {
        assert numRows >= 0 : numRows;
        assert numColumns >= 0 : numColumns;
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.values.setSize(numRows * numColumns);
    }

    public final void setDimensionsUnspecified() {
        this.numRows = UNSPECIFIED_DIMENSIONS;
        this.numColumns = UNSPECIFIED_DIMENSIONS;
        this.values.setSize(1);
    }

    public void divide(Value op1, Value op2) {
        if (is(op1) && !is(op2)) {
            divideByConstant(as(op1), op2);
        } else {
            assert false;
        }
    }

    private ValueMatrix divideByConstant(ValueMatrix op1, Value op2) {
        setDimensions(op1.getNumRows(), op1.getNumColumns());
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, getEntryAcc(0).getType(), op2.getType());
        for (int index = 0; index < op1.values.size(); index++) {
            op1.getValues().get(getEntryAcc(0), index);
            divide.apply(getEntryAcc(1), getEntryAcc(0), op2);
            getValues().set(getEntryAcc(1), index);
        }
        return this;
    }

    public void multiplyMatrixFactor(ValueMatrix matrix, ValueAlgebra factor) {
        assert matrix != null;
        assert factor != null;
        ValueAlgebra entry = getEntryAcc(0);
        if (this != matrix) {
            setDimensions(matrix.getNumRows(), matrix.getNumColumns());
        }
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, type.getEntryType(), factor.getType());
        for (int index = 0; index < values.size(); index++) {
            matrix.values.get(entry, index);
            multiply.apply(entry, entry, factor);
            values.set(entry, index);
        }
    }

    private int rowColumnToIndex(int row, int column) {
        assert row >= 0 : row;
        assert row < numRows : row + SPACE + numRows;
        assert column >= 0 + column;
        assert column < numColumns : column + SPACE + numColumns;
        return numColumns * row + column;
    }

    public void get(Value value, int row, int column) {
        assert value != null;
        values.get(value, rowColumnToIndex(row, column));
    }

    public int getNumRows() {
        assert !isDimensionsUnspecified() : this;
        return numRows;
    }

    public int getNumColumns() {
        assert !isDimensionsUnspecified();
        return numColumns;
    }

    public final int getTotalSize() {
        assert !isDimensionsUnspecified();
        return numRows * numColumns;
    }

    public boolean isDimensionsUnspecified() {
        return numRows == UNSPECIFIED_DIMENSIONS;
    }

    @Override
    public final String toString() {
        if (isDimensionsUnspecified()) {
            return toStringUnspecifiedDimensions();
        } else {
            return toStringSpecifiedDimensions();
        }
    }

    private String toStringSpecifiedDimensions() {
        StringBuilder builder = new StringBuilder();
        Value entry;
        entry = getType().getEntryType().newValue();
        int maxEntrySize = 0;
        for (int row = 0; row < numRows; row++) {
            for (int column = 0; column < numColumns; column++) {
                get(entry, row, column);
                String entryString = entry.toString();
                maxEntrySize = Math.max(maxEntrySize, entryString.length());
            }
        }
        builder.append(ENDL);
        for (int row = 0; row < numRows; row++) {
            builder.append("    ");
            for (int column = 0; column < numColumns; column++) {
                get(entry, row, column);
                String entryString = entry.toString();
                int missing = maxEntrySize - entryString.length();
                String fill = new String(new char[missing]).replace("\0", SPACE);
                entryString = fill + entryString;
                builder.append(entryString);
                if (column < numColumns - 1) {
                    builder.append("  ");
                }
            }
            if (row < numRows - 1) {
                builder.append(ENDL);
            }
        }
        return builder.toString();
    }

    private String toStringUnspecifiedDimensions() {
        values.get(getEntryAcc(0), 0);
        return String.format(UNSPEC_STR, entryAccs[0]);
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof ValueMatrix)) {
            return false;
        }
        ValueMatrix other = (ValueMatrix) obj;
        if (this.numRows != other.numRows) {
            return false;
        }
        if (this.numColumns != other.numColumns) {
            return false;
        }
        for (int entry = 0; entry < getTotalSize(); entry++) {
            getValues().get(getEntryAcc(0), entry);
            other.getValues().get(getEntryAcc(1), entry);
            if (!getEntryAcc(0).equals(getEntryAcc(1))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final int hashCode() {
        int hash = 0;
        hash = numRows + (hash << 6) + (hash << 16) - hash;
        hash = numColumns + (hash << 6) + (hash << 16) - hash;
        hash = values.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    public ValueArrayAlgebra getValues() {
        return values;
    }
}
