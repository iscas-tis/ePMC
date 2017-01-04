package epmc.rddl.model;

import java.util.Arrays;

import epmc.graph.explorer.Explorer;
import epmc.graph.explorer.ExplorerNode;
import epmc.util.BitStream;
import epmc.value.Type;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBitStoreable;

final class ExplorerNodeRDDL implements ExplorerNode {
    private final ExplorerRDDL explorer;
    private final ValueBitStoreable[] values;

    ExplorerNodeRDDL(ExplorerRDDL explorer) {
        assert explorer != null;
        this.explorer = explorer;
        Type[] types = explorer.getStateFluentTypes();
        this.values = new ValueBitStoreable[types.length];
        for (int i = 0; i < types.length; i++) {
            this.values[i] = ValueBitStoreable.asBitStoreable(types[i].newValue());
        }
    }
    
    ExplorerNodeRDDL(ExplorerRDDL explorer, Value[] values) {
        this.explorer = explorer;
        this.values = new ValueBitStoreable[values.length];
        for (int i = 0; i < values.length; i++) {
            this.values[i] = UtilValue.clone(ValueBitStoreable.asBitStoreable(values[i]));
        }
    }
    
    @Override
    public ExplorerNode clone() {
        return new ExplorerNodeRDDL(explorer, values);
    }

    @Override
    public void read(BitStream readerWriter) {
        for (int i = 0; i < values.length; i++) {
            this.values[i].read(readerWriter);
        }
    }

    @Override
    public void write(BitStream readerWriter) {
        for (int i = 0; i < values.length; i++) {
            this.values[i].write(readerWriter);
        }
    }

    @Override
    public void set(ExplorerNode explorerNode) {
        assert explorerNode != null;
        assert explorerNode instanceof ExplorerNodeRDDL;
        ExplorerNodeRDDL explorerNodeRDDL = (ExplorerNodeRDDL) explorerNode;
        assert this.explorer == explorerNodeRDDL.explorer;
        for (int i = 0; i < values.length; i++) {
            this.values[i].set(explorerNodeRDDL.values[i]);
        }
    }

    @Override
    public int getNumBits() {
        return explorer.getNumNodeBits();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int varNr = 0; varNr < values.length; varNr++) {
            builder.append(values[varNr]);
            if (varNr < values.length - 1) {
                builder.append(",");
            }
        }
        builder.append(")");
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ExplorerNodeRDDL)) {
            return false;
        }
        ExplorerNodeRDDL other = (ExplorerNodeRDDL) obj;
        return Arrays.equals(values, other.values);
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public Explorer getExplorer() {
        return explorer;
    }
    
    Value[] getValues() {
        return values;
    }
}
