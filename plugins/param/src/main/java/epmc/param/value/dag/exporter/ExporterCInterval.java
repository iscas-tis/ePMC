package epmc.param.value.dag.exporter;

import java.io.IOException;
import java.math.BigInteger;

import epmc.param.value.ParameterSet;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.dag.microcode.Microcode;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class ExporterCInterval implements DagExporter {
    // TODO get rid of format instructions because they are too slow
    public final static String IDENTIFIER = "c-interval";

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
            return new ExporterCInterval(this);
        }
    }

    /* no rounding needed when loading constants, already rounded */
    private final static String APPEND_NUMBER = 
              "  l%1$d = %2$s;\n"
            + "  u%1$d = %3$s;\n";
    /* no rounding needed when loading parameter values, already rounded */
    private final static String APPEND_PARAMETER =
              "  l%1$d = l%2$s;\n"
            + "  u%1$d = u%2$s;\n";
    /* additive inverse independent of rounding mode */
    private final static String APPEND_ADD_INVERSE =
              "  ltmp = -u%2$d;\n"
            + "  utmp = -l%2$d;\n"
            + "  l%1$d = ltmp;\n"
            + "  u%1$d = utmp;\n"
            ;
    private final static String APPEND_MULTIPLY_INVERSE =
              "  fesetround(FE_DOWNWARD);\n"
            + "  ltmp = u%2$d == 0.0 || (l%2$d < 0.0 && u%2$d > 0.0) ? -INFINITY : 1.0/u%2$d;\n"
            + "  fesetround(FE_UPWARD);\n"
            + "  utmp = l%2$d == 0.0 || (l%2$d < 0.0 && u%2$d) > 0.0 ? +INFINITY : 1.0/l%2$d;\n"
            + "  l%1$d = ltmp;\n"
            + "  u%1$d = utmp;\n"
            ;
    private final static String APPEND_ADD =
              "  fesetround(FE_DOWNWARD);\n"
            + "  ltmp = l%2$d + l%3$d;\n"
            + "  fesetround(FE_UPWARD);\n"
            + "  utmp = u%2$d + u%3$d;\n"
            + "  l%1$d = ltmp;\n"
            + "  u%1$d = utmp;\n";
    private final static String APPEND_MULTIPLY =
              "  fesetround(FE_DOWNWARD);\n"
            + "  ltmp = fmin(fmin(l%2$d * l%3$d, l%2$d * u%3$d), fmin(u%2$d * l%3$d, u%2$d * u%3$d));\n"
            + "  fesetround(FE_UPWARD);\n"
            + "  utmp = fmax(fmax(l%2$d * l%3$d, l%2$d * u%3$d), fmax(u%2$d * l%3$d, u%2$d * u%3$d));\n"
            + "  l%1$d = ltmp;\n"
            + "  u%1$d = utmp;\n";
    private final Microcode microcode;
    private final static String NEWLINE = "\n";
    
    
    private ExporterCInterval(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        microcode = new Microcode(builder.dag, builder.nodes.toIntArray());
    }

    @Override
    public void export(Appendable result) throws IOException {
        appendIncludes(result);
        // TODO not sure about exact size needed
        int stackSize = Double.BYTES * (2* microcode.getNumVariables() + 4 * microcode.getParameters().getNumParameters() + 2) + 4000;
        UtilExporter.appendStackFixFunction(result, stackSize);
        appendStructInterval(result);
        appendEvaluateFunction(result, microcode);
        appendStartMain(result);
        appendCheckCall(result);
        appendReadParameters(result);
        UtilExporter.appendStackFixCall(result);
        appendCallEvaluate(result);
        appendOutputResult(result);
        appendEndMain(result);
    }

    private void appendStructInterval(Appendable result) throws IOException {
        result.append("struct interval {\n");
        result.append("  double l;\n");
        result.append("  double u;\n");
        result.append("};\n");
    }

    private void appendEvaluateFunction(Appendable result, Microcode microcode) throws IOException {
        assert result != null;
        appendEvaluateFunctionHeader(result);
        appendEvaluateFunctionLocalVariables(result, microcode);
        appendEvaluateFunctionCompute(result, microcode);
        appendEvaluateFunctionReturn(result, microcode);
    }

    private void appendEvaluateFunctionReturn(Appendable result, Microcode microcode) throws IOException {
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result.append("  *rl")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(" = l")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(";\n  *ru")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(" = u")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(";\n");
        }
        result.append("}\n\n");
    }

    private void appendEvaluateFunctionCompute(Appendable result, Microcode microcode) throws IOException {
        for (int index = 0; index < microcode.getNumStatements(); index++) {
            appendOperation(result, index, microcode);
        }
    }

    private void appendEvaluateFunctionHeader(Appendable result) throws IOException {
        result.append("void evaluate(");
        ParameterSet parameters = microcode.getParameters();
        int numParameters = parameters.getNumParameters();
        for (int paramNr = 0; paramNr < numParameters; paramNr++) {
            result.append("double l")
            .append(parameters.getParameter(paramNr).toString())
            .append(", ")
            .append("double u")
            .append(parameters.getParameter(paramNr).toString())
            .append(", ");
        }
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result.append("double *rl")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(", double *ru")
            .append(Integer.toString(microcode.getResultVariable(index)));
            if (index < microcode.getNumResultVariables() - 1) {
                result.append(", ");
            }
        }
        result.append(") {\n");
    }

    private void appendEvaluateFunctionLocalVariables(Appendable result, Microcode microcode) throws IOException {
        result.append("  double ltmp;\n  double utmp;\n");
        for (int variable = 0; variable < microcode.getNumVariables(); variable++) {
            result.append("  double l")
            .append(Integer.toString(variable))
            .append(";\n  double u")
            .append(Integer.toString(variable))
            .append(";\n");
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
        appendFormat(result, APPEND_NUMBER, microcode.getAssignedTo(number),
                UtilExporter.fractionToHexDown(num, den),
                UtilExporter.fractionToHexUp(num, den));
    }

    private void appendParameter(Appendable result, int number, Microcode microcode) throws IOException {
        Object parameter = microcode.getParameters().getParameter(microcode.getParameter(number));
        appendFormat(result, APPEND_PARAMETER, microcode.getAssignedTo(number), parameter);
    }

    private void appendAddInverse(Appendable result, int number, Microcode microcode) throws IOException {
        appendFormat(result, APPEND_ADD_INVERSE, microcode.getAssignedTo(number), microcode.getOperand(number));
    }

    private void appendMultiplyInverse(Appendable result, int number, Microcode microcode) throws IOException {
        appendFormat(result, APPEND_MULTIPLY_INVERSE, microcode.getAssignedTo(number), microcode.getOperand(number));
    }

    private void appendAdd(Appendable result, int number, Microcode microcode) throws IOException {
        int operandLeft = microcode.getOperandLeft(number);
        int operandRight = microcode.getOperandRight(number);
        appendFormat(result, APPEND_ADD, microcode.getAssignedTo(number),
                operandLeft, operandRight);
    }

    private void appendMultiply(Appendable result, int number, Microcode microcode) throws IOException {
        int operandLeft = microcode.getOperandLeft(number);
        int operandRight = microcode.getOperandRight(number);
        appendFormat(result, APPEND_MULTIPLY, microcode.getAssignedTo(number),
                operandLeft, operandRight);
    }

    private void appendIncludes(Appendable result) throws IOException {
        assert result != null;
        UtilExporter.appendGlobalCIncludes(result,
                "stdio", "float", "stdlib", "fenv", "math");
        UtilExporter.appendStackFixToIncludes(result);
        result.append(NEWLINE);
    }
    
    private void appendStartMain(Appendable result) throws IOException {
        result.append("int main(int argc, char *argv[]) {\n");
    }
    
    private void appendCheckCall(Appendable result) throws IOException {
        int numParameters = microcode.getParameters().getNumParameters();
        result.append("  if (argc != " + (numParameters + 1) + ") {\n");
        result.append("    fprintf(stderr, \"usage: %s ");
        for (int param = 0; param < numParameters; param++) {
            result.append("<");
            result.append(microcode.getParameters().getParameter(param).toString());
            result.append(">");
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
            appendFormat(result, "  double l%s;\n",
                    microcode.getParameters().getParameter(param));
            appendFormat(result, "  double u%s;\n",
                    microcode.getParameters().getParameter(param));
        }
        for (int param = 0; param < numParameters; param++) {
            appendFormat(result, "  fesetround(FE_DOWNWARD);\n");
            appendFormat(result, "  sscanf(argv[%d], \"%%lg\", &l%s);\n",
                    param + 1, microcode.getParameters().getParameter(param));
            appendFormat(result, "  fesetround(FE_UPWARD);\n");
            appendFormat(result, "  sscanf(argv[%d], \"%%lg\", &u%s);\n",
                    param + 1, microcode.getParameters().getParameter(param));
        }
        result.append("\n");
    }
    
    private void appendCallEvaluate(Appendable result) throws IOException {
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result.append("  double rl")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(";\n  double ru")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(";\n");
        }
        result.append("  evaluate(");
        ParameterSet parameters = microcode.getParameters();
        int numParameters = parameters.getNumParameters();
        for (int paramNr = 0; paramNr < numParameters; paramNr++) {
            result.append("l" + parameters.getParameter(paramNr));
            result.append(", ");
            result.append("u" + parameters.getParameter(paramNr));
            result.append(", ");
        }
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result.append("&rl")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(", ")
            .append("&ru")
            .append(Integer.toString(microcode.getResultVariable(index)));            
            if (index < microcode.getNumResultVariables() - 1) {
                result.append(", ");
            }            
        }
        result.append(");\n");
    }
    
    private void appendOutputResult(Appendable result) throws IOException {
        result.append("  double error;\n");
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            result
            .append("  fesetround(FE_UPWARD);\n")
            .append("  error = ru")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(" - rl")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(";\n")
            .append("  fesetround(FE_DOWNWARD);\n")
            .append("  printf(\"[%lg\", rl")
            .append(Integer.toString(microcode.getResultVariable(index)))
            .append(");\n")
            .append("  printf(\",\");\n")
            .append("  fesetround(FE_UPWARD);\n")
            .append("  printf(\"%lg], \", ru")
            .append(Integer.toString(microcode.getResultVariable(0)))
            .append(");\n")
            .append("  fesetround(FE_UPWARD);\n")
            .append("  printf(\"error <= %lg\\n\", error);\n");
        }
    }

    private void appendEndMain(Appendable result) throws IOException {
        assert result != null;
        result.append("  exit(EXIT_SUCCESS);\n");
        result.append("}\n");
    }
    
    private static Appendable appendFormat(Appendable builder, String format, Object... args) throws IOException {
        return builder.append(String.format(format, args));
    }
}
