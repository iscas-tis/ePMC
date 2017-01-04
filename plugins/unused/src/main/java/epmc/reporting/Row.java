package epmc.reporting;

import gnu.trove.set.hash.TCustomHashSet;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class Row {
    private boolean fixed;
    private final Set<Cell> assertCells = new TCustomHashSet<>(
            new IdentityHashingStrategy<Cell>());
    private final List<Cell> cells = new ArrayList<>();
    private final List<Cell> publicCells = Collections.unmodifiableList(cells);
    private final boolean[] outputTypesValid =
            new boolean[OutputType.values().length];
    
    public Row add(Cell cell) {
        assert !fixed;
        assert assertCell(cell);
        cells.add(cell);
        return this;
    }

    public Row add(String text) {
        assert text != null;
        add(new Cell(text));
        return this;
    }

    public Row add(int rowSpan, int colSpan, String text) {
        assert rowSpan >= 1;
        assert colSpan >= 1;
        assert text != null;
        add(new Cell(rowSpan, colSpan, text));
        return this;
    }
    
    private boolean assertCell(Cell cell) {
        assert cell != null;
        assert !assertCells.contains(cell);
        assertCells.add(cell);
        return true;
    }

    void fix() {
        assert !fixed;
        fixed = true;
        for (Cell cell : cells) {
            cell.fix();
        }
        for (int index = 0; index < outputTypesValid.length; index++) {
            OutputType outputType = OutputType.values()[index];
            boolean valid = true;
            for (Cell cell : cells) {
                valid &= cell.isTextSet(outputType);
            }
            outputTypesValid[index] = valid;
        }
        boolean oneValid = false;
        for (boolean valid : outputTypesValid) {
            oneValid |= valid;
        }
        assert oneValid;
    }
    
    List<Cell> getCells() {
        return publicCells;
    }
    
    boolean isOutputTypeValid(OutputType outputType) {
        assert outputType != null;
        return outputTypesValid[outputType.ordinal()];
    }
}
