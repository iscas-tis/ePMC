package epmc.qmc.value;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueInteger;
import epmc.value.ValueRange;

public final class ValueMatrix implements ValueAlgebra, ValueRange {
    private static final int NUM_IMPORT_VALUES = 2;
    private static final long serialVersionUID = 1L;
    private final TypeMatrix type;
    private final ValueArray values;
    private int numRows;
    private int numColumns;
    private boolean immutable;
    private ValueAlgebra[] entryAccs;
    private final ValueMatrix importArrays[]
            = new ValueMatrix[NUM_IMPORT_VALUES];
    private ValueMatrix resultBuffer;

    public ValueMatrix(TypeMatrix type) {
        assert type != null;
        this.type = type;
        this.numRows = type.getNumRows();
        this.numColumns = type.getNumColumns();
        this.values = UtilValue.newArray(type.getArrayType(), numRows * numColumns);
    }
    
    private ValueMatrix(ValueMatrix original) {
        assert original != null;
        this.type = original.type;
        this.values = UtilValue.clone(original.values);
        this.numRows = original.numRows;
        this.numColumns = original.numColumns;
    }
    
    @Override
    public ValueMatrix clone() {
        return new ValueMatrix(this);
    }

    @Override
    public TypeMatrix getType() {
        return type;
    }

    @Override
    public void setImmutable() {
        this.immutable = true;
    }

    @Override
    public boolean isImmutable() {
        return immutable;
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
            try {
                get(getEntryAcc(0), entry);
                other.get(getEntryAcc(1), entry);
                if (!getEntryAcc(0).isEq(getEntryAcc(1))) {
                    return false;
                }
            } catch (EPMCException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
    
    @Override
    public final String toString() {
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
        builder.append("\n");
        for (int row = 0; row < numRows; row++) {
            builder.append("    ");
            for (int column = 0; column < numColumns; column++) {
            	get(entry, row, column);
            	String entryString = entry.toString();
                int missing = maxEntrySize - entryString.length();
                String fill = new String(new char[missing]).replace("\0", " ");
                entryString = fill + entryString;
                builder.append(entryString);
                if (column < numColumns - 1) {
                    builder.append("  ");
                }
            }
            if (row < numRows - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private ValueAlgebra getEntryAcc(int number) {
        assert number >= 0;
        int numEntryAccs = this.entryAccs.length;
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
    
    public final void set(int entry, int row, int column) {
        assert !isImmutable();
        getEntryAcc(0).set(entry);
        set(getEntryAcc(0), rowColumnToIndex(row, column));
    }

    public final void set(String entry, int row, int column) throws EPMCException {
        assert !isImmutable();
        assert entry != null;
        getEntryAcc(0).set(entry);
        set(getEntryAcc(0), rowColumnToIndex(row, column));
    }
    
    public final void setDimensions(int numRows, int numColumns) {
        assert !isImmutable();
        this.numRows = numRows;
        this.numColumns = numColumns;
        this.values.resize(numRows * numColumns);
    }
    
    public int getInt(int row, int column) throws EPMCException {
        values.get(getEntryAcc(0), rowColumnToIndex(row, column));
        return ValueInteger.asInteger(getEntryAcc(0)).getInt();
    }
    
    @Override
    public final int hashCode() {
        int hash = 0;
        hash = numRows + (hash << 6) + (hash << 16) - hash;
        hash = numColumns + (hash << 6) + (hash << 16) - hash;
        hash = values.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }
    
    public final int getTotalSize() {
        return numRows * numColumns;
    }
    
    @Override
    public void set(Value op) {
        assert !isImmutable();
        ValueMatrix opMatrix = castOrImport(op, 0, false);
        this.numRows = opMatrix.numRows;
        this.numColumns = opMatrix.numColumns;
        this.values.resize(numRows * numColumns);
        
        for (int index = 0; index < opMatrix.getTotalSize(); index++) {
            opMatrix.values.get(getEntryAcc(0), index);
            values.set(getEntryAcc(0), index);
        }
    }    
    
    @Override
    public void add(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        ValueMatrix op1Matrix = castOrImport(op1, 0, false);
        ValueMatrix op2Matrix = castOrImport(op2, 1, false);
        setDimensions(op1Matrix.getNumRows(), op1Matrix.getNumRows());
        for (int index = 0; index < op1Matrix.getTotalSize(); index++) {
            op1Matrix.get(getEntryAcc(0), index);
            op2Matrix.get(getEntryAcc(1), index);
            getEntryAcc(2).add(getEntryAcc(0), getEntryAcc(1));
            set(getEntryAcc(2), index);
        }
        
    }

    @Override
    public void addInverse(Value op) throws EPMCException {
        assert !isImmutable();
        ValueMatrix opMatrix = castOrImport(op, 0, false);
        setDimensions(opMatrix.getNumRows(), opMatrix.getNumRows());
        for (int index = 0; index < opMatrix.getTotalSize(); index++) {
            opMatrix.values.get(getEntryAcc(0), index);
            getEntryAcc(2).addInverse(getEntryAcc(0));
            values.set(getEntryAcc(2), index);
        }
    }

    @Override
    public void subtract(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        ValueMatrix op1Array = castOrImport(op1, 0, false);
        ValueMatrix op2Array = castOrImport(op2, 0, false);
        setDimensions(op1Array.getNumRows(), op1Array.getNumColumns());
        for (int index = 0; index < op1Array.getTotalSize(); index++) {
            op1Array.values.get(getEntryAcc(0), index);
            op2Array.values.get(getEntryAcc(1), index);
            getEntryAcc(2).subtract(getEntryAcc(0), getEntryAcc(1));
            values.set(getEntryAcc(2), index);
        }
    }

    @Override
    public void multiply(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        assert op1 != null;
        assert op2 != null;
        if (!(op2 instanceof ValueMatrix)) {
            multiplyByConstant(op1, op2);
        } else {
            matrixMultiply(castOrImport(op1, 0, true), castOrImport(op2, 1, true));
        }
    }
    
    @Override
    public void multInverse(Value op) throws EPMCException {
        assert !isImmutable();
        matrixMultInverse(castOrImport(op, 0, true));
        
    }

    @Override
    public void divide(Value op1, Value op2) throws EPMCException {
        assert !isImmutable();
        if (!ValueArray.isArray(op2)) {
            divideByConstant(op1, op2);
        } else {
            assert false;
        }
    }

    private ValueMatrix divideByConstant(Value op1, Value op2) throws EPMCException {
        ValueMatrix op1Array = castOrImport(op1, 0, false);
        setDimensions(op1Array.getNumRows(), op1Array.getNumColumns());
        for (int index = 0; index < op1Array.getTotalSize(); index++) {
            op1Array.get(getEntryAcc(0), index);
            getEntryAcc(1).divide(getEntryAcc(0), op2);
            set(getEntryAcc(1), index);
        }
        return this;
    }

    private ValueMatrix multiplyByConstant(Value op1, Value op2) throws EPMCException {
        ValueMatrix op1Array = castOrImport(op1, 0, false);
        setDimensions(op1Array.getNumRows(), op1Array.getNumColumns());
        for (int index = 0; index < op1Array.getTotalSize(); index++) {
            op1Array.get(getEntryAcc(0), index);
            getEntryAcc(1).divide(getEntryAcc(0), op2);
            set(getEntryAcc(1), index);
        }
        return this;
    }

    @Override
    public double norm() throws EPMCException {
        assert !isImmutable();
        double result = 0.0;
        for (int index = 0; index < getTotalSize(); index++) {
            get(getEntryAcc(0), index);
            Math.max(result, getEntryAcc(0).norm());
        }
        return result;
    }

    public ValueMatrix matrixMultiply(ValueMatrix op1, ValueMatrix op2) throws EPMCException {
        assert !isImmutable();
        if (this == op1 || this == op2) {
            if (resultBuffer == null) {
                resultBuffer = getType().newValue();
            }
            resultBuffer.matrixMultiply(op1, op2);
            set(resultBuffer);
        }
        setDimensions(op1.getNumRows(), op2.getNumColumns());
        for (int row = 0; row < op1.getNumRows(); row++) {
            for (int column = 0; column < op2.getNumColumns(); column++) {
                getEntryAcc(3).set(getType().getEntryType().getZero());
                for (int inner = 0; inner < op1.getNumColumns(); inner++) {
                    op1.get(getEntryAcc(0), row, inner);
                    op2.get(getEntryAcc(1), inner, column);
                    getEntryAcc(2).multiply(getEntryAcc(0), getEntryAcc(1));
                    getEntryAcc(0).add(getEntryAcc(2), getEntryAcc(3));
                    getEntryAcc(3).set(getEntryAcc(0));
                }
                set(getEntryAcc(3), row, column);
            }            
        }
        return this;
    }
    
    public ValueMatrix matrixMultInverse(ValueMatrix op) {
        assert !isImmutable();
        assert false;
        return this;
    }

    @Override
    public boolean isEq(Value other) throws EPMCException {
        ValueMatrix otherArray = castOrImport(other, 0, true);
        if (getNumRows() != otherArray.getNumRows()) {
        	return false;
        }
        if (getNumColumns() != otherArray.getNumColumns()) {
        	return false;
        }
        for (int entry = 0; entry < getTotalSize(); entry++) {
            get(getEntryAcc(0), entry);
            otherArray.get(otherArray.getEntryAcc(0), entry);
            if (!getEntryAcc(0).isEq(otherArray.getEntryAcc(0))) {
                return false;
            }
        }
        return true;
    }
    
    private ValueMatrix castOrImport(Value operand, int number, boolean multiply) {
        assert operand != null;
        assert number >= 0;
        assert number < NUM_IMPORT_VALUES;
        if (ValueArray.isArray(operand)) {
            return (ValueMatrix) operand;
        } else if (getType().getEntryType().canImport(operand.getType())) {
            if (importArrays[number] == null) {
                importArrays[number] = getType().newValue();
            }
            importArrays[number].setDimensions(getNumRows(), getNumColumns());
            if (multiply) {
                for (int entryNr = 0; entryNr < numRows; entryNr++) {
                    set(operand, entryNr, entryNr);
                }
            } else {
                for (int entryNr = 0; entryNr < this.getTotalSize(); entryNr++) {
                    set(operand, entryNr);
                }                
            }
            return importArrays[number];
        } else {
            assert false : this + " " + operand;
            return null;
        }
    }
    
    @Override
    public boolean checkRange() throws EPMCException {
        for (int index = 0; index < getTotalSize(); index++) {
            get(getEntryAcc(0), index);
            if (!ValueRange.checkRange(getEntryAcc(0))) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int compareTo(Value other) {
        assert !isImmutable();
        ValueMatrix opMatrix = castOrImport(other, 0, false);
        int cmpRows = Integer.compare(this.numRows, opMatrix.numRows);
        if (cmpRows != 0) {
            return cmpRows;
        }
        int cmpColumns = Integer.compare(this.numColumns, opMatrix.numRows);
        if (cmpColumns != 0) {
            return cmpColumns;
        }
        for (int entry = 0; entry < getTotalSize(); entry++) {
            get(getEntryAcc(0), entry);
            opMatrix.get(opMatrix.getEntryAcc(0), entry);
            int cmpEntry = getEntryAcc(0).compareTo(opMatrix.getEntryAcc(0));
            if (cmpEntry != 0) {
                return cmpEntry;
            }
        }
        return 0;
    }
    
    @Override
    public double distance(Value other) throws EPMCException {
        assert other != null;
        if (!(other instanceof ValueMatrix)) {
            return Double.POSITIVE_INFINITY;
        }
        ValueMatrix otherMatrix = (ValueMatrix) other;
        if (this.numRows != otherMatrix.numRows) {
            return Double.POSITIVE_INFINITY;
        }
        if (this.numColumns != otherMatrix.getNumColumns()) {
            return Double.POSITIVE_INFINITY;
        }
        double maxDistance = 0.0;
        for (int entry = 0; entry < getTotalSize(); entry++) {
            values.get(getEntryAcc(0), entry);
            otherMatrix.values.get(otherMatrix.getEntryAcc(0), entry);
            double entryDistance = getEntryAcc(0).distance(otherMatrix.getEntryAcc(0));
            Math.max(maxDistance, entryDistance);
        }
        return maxDistance;
    }

    private int rowColumnToIndex(int row, int column) {
        assert row >= 0;
        assert row < numRows;
        assert column >= 0;
        assert column < numColumns;
        return numColumns * row + column;
    }

    /*
    private int indexToRow(int index) {
        assert index >= 0;
        assert index < numRows * numColumns;
        return index / numColumns;
    }
    
    private int indexToColumn(int index) {
        assert index >= 0;
        assert index < numRows * numColumns;
        return index % numColumns;
    }
    */
    
    public void get(Value value, int index) {
        values.get(value, index);
    }

    private void set(Value value, int index) {
        values.set(value, index);
    }
    
    public void set(Value value, int row, int column) {
    	values.set(value, rowColumnToIndex(row, column));
    }

    public void get(Value value, int row, int column) {
    	values.get(value, rowColumnToIndex(row, column));
    }

    public int getNumRows() {
        return numRows;
    }
    
    public int getNumColumns() {
        return numColumns;
    }

	@Override
	public void set(int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isZero() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOne() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPosInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isNegInf() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void set(String value) throws EPMCException {
		// TODO Auto-generated method stub
		
	}
}
