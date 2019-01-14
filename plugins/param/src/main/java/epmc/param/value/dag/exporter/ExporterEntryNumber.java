package epmc.param.value.dag.exporter;

import java.io.IOException;

import epmc.param.value.dag.Dag;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class ExporterEntryNumber implements DagExporter {
    public final static String IDENTIFIER = "entry-number";
    
    public final static class Builder implements DagExporter.Builder {
        private Dag dag;
        private final IntArrayList nodes = new IntArrayList();

        @Override
        public Builder setDag(Dag dag) {
            this.dag = dag;
            return this;
        }

        @Override
        public DagExporter build() {
            return new ExporterEntryNumber(this);
        }

        @Override
        public Builder addRelevantNode(int node) {
            nodes.add(node);
            return this;
        }
    }

    private final IntArrayList nodes;

    private ExporterEntryNumber(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        nodes = builder.nodes;
    }

    @Override
    public void export(Appendable result) throws IOException {
        if (nodes.size() == 1) {
            result.append(Integer.toString(nodes.getInt(0)));
        } else {
            result.append(nodes.toString());
        }
    }
}
