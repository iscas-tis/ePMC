package epmc.param.value.dag.exporter;

import java.io.IOException;
import java.math.BigInteger;

import epmc.param.value.ParameterSet;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.dag.microcode.Microcode;
import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * Exports DAG into a C program.
 * The program reads parameter values from {@code stdin} and prints values for
 * the relevant nodes to {@code stdout}.
 * The program generated is just a linearisation of the DAG. For any node we
 * basically have one instruction. Because of this, the size of the code might
 * become quite large. Thus, for compilation it is usually necessary to switch
 * off optimisation. The resulting executable should still be fast enough
 * without any optimisations anyway.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ExporterC implements DagExporter {
    /* Note: Do not use String.format() or Formatter, because performance is
     * insufficient.
     * */
    
    public final static String IDENTIFIER = "c";
    
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
            return new ExporterC(this);
        }
    }

    private final Microcode microcode;
    
    private ExporterC(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        microcode = new Microcode(builder.dag, builder.nodes.toIntArray());
    }

    @Override
    public void export(Appendable result) throws IOException {
        appendIncludes(result);
        int stackSize = Double.BYTES * (microcode.getNumVariables() + 2 * microcode.getParameters().getNumParameters() + 2) + 4000;
        // TODO not sure about exact size needed
        UtilExporter.appendStackFixFunction(result, stackSize);
        appendEvaluateFunction(result, microcode);
        appendStartMain(result);
        appendCheckCall(result);
        appendReadParameters(result);
        UtilExporter.appendStackFixCall(result);
        appendCallEvaluate(result, microcode);
        appendOutputResult(result, microcode);
        appendEndMain(result);
    }

    private void appendEvaluateFunction(Appendable result, Microcode microcode) throws IOException {
        assert result != null;
        appendEvaluateFunctionHeader(result, microcode);
        appendEvaluateFunctionLocalVariables(result, microcode);
        appendEvaluateFunctionCompute(result, microcode);
        appendEvaluateFunctionReturn(result, microcode);
    }

    private void appendEvaluateFunctionHeader(Appendable result, Microcode microcode) throws IOException {
        result.append("void evaluate(");
        ParameterSet parameters = microcode.getParameters();
        int numParameters = parameters.getNumParameters();
        for (int paramNr = 0; paramNr < numParameters; paramNr++) {
            result.append("double ")
            .append(parameters.getParameter(paramNr).toString())
            .append(", ");
        }
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result.append("double *r")
            .append(Integer.toString(microcode.getResultVariable(index)));
            if (index < microcode.getNumResultVariables() - 1) {
                result.append(", ");
            }
        }
        result.append(") {\n");
    }
    
    private void appendEvaluateFunctionLocalVariables(Appendable result, Microcode microcode) throws IOException {
        for (int variable = 0; variable < microcode.getNumVariables(); variable++) {
            result.append("  double v")
            .append(Integer.toString(variable))
            .append(";\n");
        }
    }
    
    private void appendEvaluateFunctionCompute(Appendable result, Microcode microcode) throws IOException {
        for (int index = 0; index < microcode.getNumStatements(); index++) {
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
        result.append("  v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = ")
        .append(UtilExporter.fractionToHex(num, den))
        .append(";\n");
    }

    private void appendParameter(Appendable result, int number, Microcode microcode) throws IOException {
        ParameterSet parameters = microcode.getParameters();
        Object parameter = parameters.getParameter(microcode.getParameter(number));
        result.append("  v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = ")
        .append(parameter.toString())
        .append(";\n");
    }

    private void appendAddInverse(Appendable result, int number, Microcode microcode) throws IOException {
        result.append("  v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = -v")
        .append(Integer.toString(microcode.getOperand(number)))
        .append(";\n");
    }

    private void appendMultiplyInverse(Appendable result, int number, Microcode microcode) throws IOException {
        result.append("  v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = 1.0/v")
        .append(Integer.toString(microcode.getOperand(number)))
        .append(";\n");
    }

    private void appendAdd(Appendable result, int number, Microcode microcode) throws IOException {
        result.append("  v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = v")
        .append(Integer.toString(microcode.getOperandLeft(number)))
        .append(" + v")
        .append(Integer.toString(microcode.getOperandRight(number)))
        .append(";\n");
    }

    private void appendMultiply(Appendable result, int number, Microcode microcode) throws IOException {
        result.append("  v")
        .append(Integer.toString(microcode.getAssignedTo(number)))
        .append(" = v")
        .append(Integer.toString(microcode.getOperandLeft(number)))
        .append(" * v")
        .append(Integer.toString(microcode.getOperandRight(number)))
        .append(";\n");
    }

    private void appendEvaluateFunctionReturn(Appendable result, Microcode microcode) throws IOException {
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result.append("  *r")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(" = v")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(";\n");
        }
        result.append("}\n\n");
    }

    private void appendIncludes(Appendable result) throws IOException {
        assert result != null;
        UtilExporter.appendGlobalCIncludes(result,
                "stdio", "float", "stdlib", "string");
        UtilExporter.appendStackFixToIncludes(result);
        result.append("\n");
    }
    
    private void appendStartMain(Appendable result) throws IOException {
        result.append("int main(int argc, char *argv[]) {\n");
    }
    
    private void appendCheckCall(Appendable result) throws IOException {
        int numParameters = microcode.getParameters().getNumParameters();
        result.append("  if (argc != " + (numParameters + 1) + ") {\n")
        .append("    fprintf(stderr, \"Incorrect number of parameters.\\n\");\n")
        .append("    fprintf(stderr, \"usage: %s ");
        for (int param = 0; param < numParameters; param++) {
            result.append("<")
            .append(microcode.getParameters().getParameter(param).toString())
            .append(">");
            if (param < numParameters - 1) {
                result.append(" ");
            }
        }
        result.append("\\n\", argv[0]);\n");
        result.append("    exit(EXIT_FAILURE);\n");
        result.append("  }\n\n");
    }

    private void appendReadParameters(Appendable result) throws IOException {
        assert result != null;
        int numParameters = microcode.getParameters().getNumParameters();
        for (int param = 0; param < numParameters; param++) {
            result.append("  double ")
            .append(microcode.getParameters().getParameter(param).toString())
            .append(";\n");
        }
        result.append("  char *check_end;\n");
        for (int param = 0; param < numParameters; param++) {
            result.append("  ")
            .append(microcode.getParameters().getParameter(param).toString())
            .append(" = strtod(argv[")
            .append(Integer.toString(param + 1))
            .append("], &check_end);\n")
            .append("  if (check_end != argv[")
            .append(Integer.toString(param + 1))
            .append("] + strlen(argv[")
            .append(Integer.toString(param + 1))
            .append("])) {\n")
            .append("    fprintf(stderr, \"Could not parse parameter %d value ")
            .append("\\\"%s\\\".\\n\", ")
            .append(Integer.toString(param + 1))
            .append(", argv[")
            .append(Integer.toString(param + 1))
            .append("]);\n")
            .append("    exit(EXIT_FAILURE);\n")
            .append("  }\n");
        }
        result.append("\n");
    }
    
    private void appendCallEvaluate(Appendable result, Microcode microcode) throws IOException {
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result.append("  double r")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(";\n");
        }
        result.append("  evaluate(");
        ParameterSet parameters = microcode.getParameters();
        int numParameters = parameters.getNumParameters();
        for (int paramNr = 0; paramNr < numParameters; paramNr++) {
            result.append(parameters.getParameter(paramNr).toString());
            result.append(", ");
        }
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result.append("&r")
            .append(Integer.toString(microcode.getResultVariable(index)));
            if (index < microcode.getNumResultVariables() - 1) {
                result.append(", ");
            }
        }
        result.append(");\n");
    }
    
    private void appendOutputResult(Appendable result, Microcode microcode) throws IOException {        
//      result.append("  printf(\"%.*e\\n\", DECIMAL_DIG, result);\n");
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result.append("  printf(\"%lf\\n\", r")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(");\n");
        }
    }

    private void appendEndMain(Appendable result) throws IOException {
        assert result != null;
        result.append("  exit(EXIT_SUCCESS);\n");
        result.append("}\n");
    }
}
