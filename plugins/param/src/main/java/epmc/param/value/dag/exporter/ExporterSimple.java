package epmc.param.value.dag.exporter;

import java.io.IOException;

import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class ExporterSimple implements DagExporter {
    public final static String IDENTIFIER = "simple";

    public final static class Builder implements DagExporter.Builder {
        private Dag dag;
        private final IntArrayList nodes = new IntArrayList();
        
        @Override
        public Builder setDag(Dag dag) {
            this.dag = dag;
            return this;
        }

        @Override
        public Builder addRelevantNode(int node) {
            nodes.add(node);
            return this;
        }

        @Override
        public DagExporter build() {
            return new ExporterSimple(this);
        }
    }

    private final Dag dag;
    private final int[] start;
    
    private ExporterSimple(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        dag = builder.dag;
        start = builder.nodes.toIntArray();
    }

    @Override
    public void export(Appendable result) throws IOException {
        for (int index = 0; index < start.length; index++) {
            result.append(doExport(start[index]));
            if (index < start.length - 1) {
                result.append(", ");
            }
        }
    }

    private String doExport(int number) {
//        if (true) return "bla";
        OperatorType type = dag.getOperatorType(number);
        switch (type) {
        case NUMBER:
            return dag.getNumberNumerator(number) + "/"
                    + dag.getNumberDenominator(number);
        case PARAMETER:
            return dag.getParameter(number).toString();
        case ADD_INVERSE:
            return "-(" + doExport(dag.getOperand(number)) + ")";
        case MULTIPLY_INVERSE:
            return "(" + doExport(dag.getOperand(number)) + ")^(-1)";
        case ADD:
            return "(" + doExport(dag.getOperandLeft(number))
                    + ")+(" + doExport(dag.getOperandRight(number)) + ")";
        case MULTIPLY:
            return "(" + doExport(dag.getOperandLeft(number))
            + ")*(" + doExport(dag.getOperandRight(number)) + ")";
        default:
            assert false;
            return null;
        }
    }
}
