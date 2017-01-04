package epmc.reporting;

import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

public final class Table {
    private boolean fixed;
    private final Set<Row> assertRows = new TCustomHashSet<>(
            new IdentityHashingStrategy<Row>());
    private final List<Row> rows = new ArrayList<>();
    private final List<Row> publicRows = Collections.unmodifiableList(rows);
    private int numColumns;
    private final BitSet occupiedCells = new BitSetUnboundedLongArray();
    private int currentRow;
    private final boolean[] outputTypesValid =
            new boolean[OutputType.values().length];
    
    public void add(Row row) {
        assert !fixed;
        assert assertRow(row);
        rows.add(row);
    }
    
    private boolean assertRow(Row row) {
        assert row != null;
        assert !assertRows.contains(row) :
            "rows must not be added multiple times";
        assertRows.add(row);
        initNumColsIfNecessary(row);
        skipCompletelyOccupiedRows();
        int currentCol = 0;
        for (Cell cell : row.getCells()) {
            for (int cellCol = 0; cellCol < cell.getSpanCols(); cellCol++) {
                while (isCellSet(currentRow, currentCol)) {
                    currentCol++;
                }
                for (int cellRow = 0; cellRow < cell.getSpanRows(); cellRow++) {
                    assert !isCellSet(currentRow + cellRow, currentCol);                   
                    setCellSet(currentRow + cellRow, currentCol);                    
                }
                currentCol++;
            }
            assert currentCol <= numColumns;
        }
        assert currentCol == numColumns
                : "Row has " + currentCol + " rows but should have "
                + numColumns;
        return true;
    }

    private void initNumColsIfNecessary(Row row) {
        if (numColumns == 0) {
            for (Cell cell : row.getCells()) {
                numColumns += cell.getSpanCols();
            }
        }
    }

    private void skipCompletelyOccupiedRows() {
        boolean rowCompletelyOccupied = true;
        while (rowCompletelyOccupied) {
            for (int col = 0; col < numColumns; col++) {
                if (!isCellSet(currentRow, col)) {
                    rowCompletelyOccupied = false;
                }
            }
            if (rowCompletelyOccupied) {
                currentRow++;
            }
        }
    }

    private void setCellSet(int row, int column) {
        assert numColumns > 0;
        assert row >= 0;
        assert column >= 0;
        assert column < numColumns;
        occupiedCells.set(row * numColumns + column);
    }

    private boolean isCellSet(int row, int column) {
        assert numColumns > 0;
        assert row >= 0;
        assert column >= 0;
        assert column < numColumns : column;
        return occupiedCells.get(row * numColumns + column);
    }

    public void fix() {
        assert !fixed;
        this.fixed = true;
        fixAllRows();
        checkDanglingRows();
        computeValidOutputTypes();
    }
    
    private void checkDanglingRows() {
        skipCompletelyOccupiedRows();
        for (int col = 0; col < numColumns; col++) {
            assert !isCellSet(currentRow, col) :
                "table contains incomplete rows:\n" + setCellsToString();
        }
    }

    private void computeValidOutputTypes() {
        for (int index = 0; index < outputTypesValid.length; index++) {
            OutputType outputType = OutputType.values()[index];
            boolean valid = true;
            for (Row row : rows) {
                valid &= row.isOutputTypeValid(outputType);
            }
            outputTypesValid[index] = valid;
        }
    }

    private void fixAllRows() {
        for (Row row : rows) {
            row.fix();
        }
    }

    boolean isFixed() {
        return fixed;
    }
    
    boolean isOutputTypeValid(OutputType outputType) {
        assert outputType != null;
        return outputTypesValid[outputType.ordinal()];
    }

    public void print(OutputType outputType, OutputStream out) {
        assert fixed;
        assert outputType != null;
        assert out != null;
        assert outputTypesValid[outputType.ordinal()];
        TableWriter tableWriter;
        switch (outputType) {
        case HTML:
            tableWriter = new TableWriterHTML(this, out);
            break;
        case LATEX:
            tableWriter = new TableWriterLaTeX(this, out);
            break;
        default:
            assert false;
            tableWriter = null;
            break;
        }
        tableWriter.write();
    }
    
    String setCellsToString() {
        assert numColumns > 0;
        StringBuilder builder = new StringBuilder();
        int numRows = occupiedCells.length() / numColumns;
        if (occupiedCells.length() % numColumns > 0) {
            numRows++;
        }
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                builder.append(isCellSet(row, col) ? "1" : "0");
            }
            if (row < numRows - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }
    
    List<Row> getRows() {
        return publicRows;
    }
    
    int getNumColumns() {
        return numColumns;
    }
}
