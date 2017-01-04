package epmc.reporting;

import java.io.OutputStream;
import java.io.PrintStream;

import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

final class TableWriterLaTeX extends TableWriter {
    private final Table table;
    private final PrintStream out;
    private boolean writeDocument;
    private final int numColumns;
    private final BitSet occupiedCells = new BitSetUnboundedLongArray();
    private int currentRow;

    TableWriterLaTeX(Table table, OutputStream out) {
        assert table != null;
        assert out != null;
        assert table.isFixed();
        assert table.isOutputTypeValid(OutputType.LATEX);
        this.numColumns = table.getNumColumns();
        this.table = table;
        this.out = new PrintStream(out);
    }

    @Override
    void write() {
        if (writeDocument) {
            writeHeader();
        }
        writeTable();
        if (writeDocument) {
            writeFooter();
        }
    }

    private void writeHeader() {
        out.println("\\documentclass{report}");
        out.println("\\usepackage{multirow}");
        out.println("\\begin{document}");
    }

    private void writeFooter() {
        out.println("\\end{document}");
    }

    private void writeTable() {
        out.print("\\begin{tabular}{");
        for (int column = 0; column < numColumns; column++) {
            out.print("l");
        }
        out.println("}");
        out.println("\\toprule");
        for (Row row : table.getRows()) {
            skipCompletelyOccupiedRows();
            int currentCol = 0;
            for (Cell cell : row.getCells()) {
                for (int cellCol = 0; cellCol < cell.getSpanCols(); cellCol++) {
                    while (isCellSet(currentRow, currentCol)) {
                        currentCol++;
                    }
                }
                if (cell.isHeader()) {
                } else {
                }
                if (cell.getSpanCols() > 1) {
                }
                if (cell.getSpanRows() > 1) {
                }
                if (cell.isMarked()) {
                }
            }
        }
        
        out.println("\\bottomrule");
        out.println("\\end{tabular}");
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
    
    @Override
    void setWriteDocument(boolean writeDocument) {
        this.writeDocument = writeDocument;
    }
}
