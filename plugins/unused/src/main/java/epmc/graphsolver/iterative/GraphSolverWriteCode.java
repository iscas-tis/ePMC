package epmc.graphsolver.iterative;

import java.io.PrintStream;
import java.util.List;

final class GraphSolverWriteCode {
    private enum ConfigurationPart {
        VALUE_TYPE,
        MODEL_TYPE,
        BOUNDED_UNBOUNDED,
        ANALYSIS_TYPE
    }
    
    private enum Language {
        JAVA,
        C
    }
    
    private enum WeightType {
        GENERIC,
        DOUBLE
    }
    
    private enum ParameterType {
        INT,
        WEIGHT,
        ARRAY_INT,
        ARRAY_WEIGHT;
        
        String toString(Language language, WeightType weightType) {
            return null;
        }
    }
    
    private final PrintStream outC;
    private int indentation;
    GraphSolverWriteCode(PrintStream out) {
        assert out != null;
        this.outC = out;
    }
    
    private void cStartFile() {
        outC.println("#include <stdlib.h>");
        outC.println("#include \"epmc_error.h\"");
    }
    
    private void cStartFunction(List<String> name, List<String> parameterNames,
            List<ParameterType> parameterTypes, WeightType weightType) {
        assert name != null;
        for (String part : name) {
            assert part != null;
        }
        outC.println("__attribute__ ((visibility(\"default\")))");
        outC.print("epmc_error_t ");
        for (int i = 0; i < name.size(); i++) {
            outC.print(name.get(i));
            if (i < name.size() - 1) {
                outC.print("_");
            }
        }

        for (int i = 0; i < parameterNames.size(); i++) {
            outC.print(parameterTypes.get(i).toString(Language.C, weightType));
            outC.print(" ");
            outC.print(parameterNames.get(i));
            if (i < parameterNames.size() - 1) {
                outC.print(", ");
            }
        }
        
        outC.println(") {");
        /*
        double_ctmc_bounded(double *fg, int left, int right,
                int numStates, int *stateBounds, int *targets, double *weights,
                double *values) {
         */
    }

    private void cEndBlock() {
        indentation--;
    }
    
    private void enumerateConfigurations() {
        
    }
    
    public static void main(String[] args) {
        System.out.println();
    }
}

