package epmc.param.value.dag.exporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import epmc.param.points.PointResultsExporter;
import epmc.param.points.UtilPoints;
import epmc.param.value.TypeFunction;
import epmc.param.value.ValueFunction;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import gnu.trove.list.array.TIntArrayList;

public final class ExporterPoints implements DagExporter {
    public final static String IDENTIFIER = "points";
    
    public final static class Builder implements DagExporter.Builder {
        private final TIntArrayList nodes = new TIntArrayList();
        
        @Override
        public Builder setDag(Dag dag) {
            return this;
        }

        @Override
        public Builder addRelevantNode(int node) {
            nodes.add(node);
            return this;
        }

        @Override
        public DagExporter build() {
            return new ExporterPoints(this);
        }
    }

    private final PointResultsExporter exporter;

    private ExporterPoints(Builder builder) {
        assert builder != null;
        List<ValueFunction> functions = new ArrayList<>();
        for (int node : builder.nodes.toArray()) {
            ValueDag valueDag = TypeDag.as(TypeFunction.get()).newValue();
            valueDag.setNumber(node);
            functions.add(valueDag);
        }
        exporter = UtilPoints.getExporter(functions);
    }

    @Override
    public void export(Appendable result) throws IOException {
        exporter.export(result);
    }
}
