package epmc.param.value.dag.exporter;

import java.io.IOException;
import java.math.BigInteger;

import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.dag.microcode.Microcode;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class ExporterGinsh implements DagExporter {
    /* Note: Do not use String.format() or Formatter, because performance is
     * insufficient.
     * */
    
    public final static String IDENTIFIER = "ginsh";
    
    public final static class Builder implements DagExporter.Builder {
        private Dag dag;
        private IntArrayList nodes = new IntArrayList();

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
            return new ExporterGinsh(this);
        }
    }

    private final String surroundFunction = "normal";
    private final Microcode microcode;
    
    private ExporterGinsh(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        microcode = new Microcode(builder.dag, builder.nodes.toIntArray());
    }

    @Override
    public void export(Appendable result) throws IOException {
        appendCompute(result, microcode);
        appendOutputResults(result, microcode);
    }

    private void appendCompute(Appendable result, Microcode microcode) throws IOException {
        for (int index = 0; index < microcode.getNumStatements(); index++) {
            double percent = ((double) index + 1) / microcode.getNumStatements();
            String percentString = String.format("%.2f", percent * 100);
            result.append("!echo \"");
            result.append(percentString);
            result.append("% done\"\n");
            appendOperation(result, index, microcode);
        }
    }

    private void appendOperation(Appendable result, int number, Microcode microcode) throws IOException {
        OperatorType type = microcode.getOperator(number);
        switch (type) {
        case NUMBER:
            appendNumber(result, number, microcode);
            break;
        case PARAMETER:
            appendParameter(result, number, microcode);
            break;
        case ADD_INVERSE:
            appendAddInverse(result, number, microcode);
            break;
        case MULTIPLY_INVERSE:
            appendMultiplyInverse(result, number, microcode);
            break;
        case ADD:
            appendAdd(result, number, microcode);
            break;
        case MULTIPLY:
            appendMultiply(result, number, microcode);
            break;
        default:
            break;
        }
    }

    private void appendNumber(Appendable result, int number, Microcode microcode) throws IOException {
        BigInteger num = microcode.getNumberNumerator(number);
        BigInteger den = microcode.getNumberDenominator(number);
        result.append("v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = ")
        .append(surroundFunction)
        .append("(")
        .append(num.toString())
        .append("/")
        .append(den.toString())
        .append("):\n");
    }

    private void appendParameter(Appendable result, int number, Microcode microcode) throws IOException {
        Object parameter = microcode.getParameters().getParameter(microcode.getParameter(number));
        result.append("v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = ")
        .append(surroundFunction)
        .append("(")
        .append(parameter.toString())
        .append("):\n");
    }

    private void appendAddInverse(Appendable result, int number, Microcode microcode) throws IOException {
        int operand = microcode.getOperand(number);
        result.append("v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = ")
        .append(surroundFunction)
        .append("(-v")
        .append(Integer.toString(operand))
        .append("):\n");
    }

    private void appendMultiplyInverse(Appendable result, int number, Microcode microcode) throws IOException {
        int operand = microcode.getOperand(number);
        result.append("v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = ")
        .append(surroundFunction)
        .append("(1/v")
        .append(Integer.toString(operand))
        .append("):\n");
    }

    private void appendAdd(Appendable result, int number, Microcode microcode) throws IOException {
        int operandLeft = microcode.getOperandLeft(number);
        int operandRight = microcode.getOperandRight(number);
        result.append("v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = ")
        .append(surroundFunction)
        .append("(v")
        .append(Integer.toString(operandLeft))
        .append(" + v")
        .append(Integer.toString(operandRight))
        .append("):\n");
    }

    private void appendMultiply(Appendable result, int number, Microcode microcode) throws IOException {
        int operandLeft = microcode.getOperandLeft(number);
        int operandRight = microcode.getOperandRight(number);
        result.append("v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = ")
        .append(surroundFunction)
        .append("(v")
        .append(Integer.toString(operandLeft))
        .append(" * v")
        .append(Integer.toString(operandRight))
        .append("):\n");
    }

    private void appendOutputResults(Appendable result, Microcode microcode) throws IOException {
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result
            .append("v")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(";\n");
        }
    }
}
