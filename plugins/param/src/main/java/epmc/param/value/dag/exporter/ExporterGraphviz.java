package epmc.param.value.dag.exporter;

import java.io.IOException;

import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.dag.UtilDag;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class ExporterGraphviz implements DagExporter {
    public final static String IDENTIFIER = "graphviz";

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
            return new ExporterGraphviz(this);
        }
    }

    private final static String DIGRAPH_BEGIN = "digraph G {\n";
    private final static String DIGRAPH_END = "}\n";
    private final static String BOX = "box";
    private final static String CIRCLE = "circle";
    private final static String ADD_INVERSE = "-";
    private final static String MULTIPLY_INVERSE = "1/";
    private final static String ADD = "+";
    private final static String MULTIPLY = "*";

    private final Dag dag;
    private final int[] start;

    private ExporterGraphviz(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        dag = builder.dag;
        start = builder.nodes.toIntArray();
    }

    @Override
    public void export(Appendable result) throws IOException {
        appendBeginGraph(result);
        int[] depending = UtilDag.findDepending(dag, start);
        appendStartMarkerNodes(result);
        appendRegularNodes(result, depending);
        appendStartMarkerEdges(result);
        appendRegularEdges(result, depending);
        appendEndGraph(result);
    }

    private void appendBeginGraph(Appendable result) throws IOException {
        result.append(DIGRAPH_BEGIN);
    }

    private void appendStartMarkerNodes(Appendable result) throws IOException {
        assert result != null;
        for (int index = 0; index < start.length; index++) {
            result.append("  node[shape=point] start")
            .append(Integer.toString(start[index]))
            .append(";\n");
        }
    }
    
    private void appendRegularNodes(Appendable result, int[] depending) throws IOException {
        assert result != null;
        for (int index = 0; index < depending.length; index++) {
            int number = depending[index];
            result.append("  node[shape=")
            .append(getShape(number))
            .append(",label=\"")
            .append(getLabel(number))
            .append("\"] v")
            .append(Integer.toString(number))
            .append(";\n");
        }
    }

    private String getLabel(int number) {
        OperatorType type = dag.getOperatorType(number);
        switch (type) {
        case NUMBER:
            return dag.getNumberNumerator(number)
                    + "/"
                    + dag.getNumberDenominator(number);
        case PARAMETER:
            return dag.getParameter(number).toString();
        case ADD_INVERSE:
            return ADD_INVERSE;
        case MULTIPLY_INVERSE:
            return MULTIPLY_INVERSE;
        case ADD:
            return ADD;
        case MULTIPLY:
            return MULTIPLY;
        default:
            assert false;
            return null;
        }
    }

    private String getShape(int number) {
        OperatorType type = dag.getOperatorType(number);
        switch (type) {
        case NUMBER:
            return BOX;
        case PARAMETER:
            return BOX;
        case ADD_INVERSE:
            return CIRCLE;
        case MULTIPLY_INVERSE:
            return CIRCLE;
        case ADD:
            return CIRCLE;
        case MULTIPLY:
            return CIRCLE;
        default:
            assert false;
            return null;
        }
    }

    private void appendStartMarkerEdges(Appendable result) throws IOException {
        assert result != null;
        for (int index = 0; index < start.length; index++) {
            String startString = Integer.toString(start[index]);
            result.append("  start")
            .append(startString)
            .append(" -> v")
            .append(startString)
            .append(";\n");
        }
    }
    
    private void appendRegularEdges(Appendable result, int[] depending) throws IOException {
        assert result != null;
        for (int index = 0; index < depending.length; index++) {
            int number = depending[index];
            OperatorType type = dag.getOperatorType(number);
            switch (type) {
            case ADD_INVERSE:
                appendEdge(result, number, dag.getOperand(number));
                break;
            case MULTIPLY_INVERSE:
                appendEdge(result, number, dag.getOperand(number));
                break;
            case ADD:
                appendEdge(result, number, dag.getOperandLeft(number));
                appendEdge(result, number, dag.getOperandRight(number));
                break;
            case MULTIPLY:
                appendEdge(result, number, dag.getOperandLeft(number));
                appendEdge(result, number, dag.getOperandRight(number));
                break;
            default:
                break;
            }
        }
    }

    private void appendEdge(Appendable result, int from, int to) throws IOException {
        result.append("  v")
        .append(Integer.toString(from))
        .append(" -> v")
        .append(Integer.toString(to))
        .append(";\n");
    }

    private void appendEndGraph(Appendable result) throws IOException {
        result.append(DIGRAPH_END);
    }
}
