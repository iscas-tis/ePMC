package epmc.param.value.dag.evaluator;

import java.util.ArrayList;

import epmc.param.value.FunctionEvaluator;
import epmc.param.value.ValueFunction;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.param.value.dag.microcode.Microcode;
import epmc.param.value.gmp.GMP;
import epmc.param.value.gmp.TypeMPQ;
import epmc.param.value.gmp.ValueArrayMPQ;
import epmc.param.value.gmp.ValueMPQ;
import epmc.value.TypeAlgebra;
import epmc.value.UtilValue;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueArrayInterval;

import static epmc.param.value.dag.microcode.UtilMicrocode.microcodeToInteger;
import static epmc.param.value.dag.microcode.UtilMicrocode.microcodeToNumbersListRational;

public final class EvaluatorDagGMPNative implements FunctionEvaluator {
    public final static String IDENTIFIER = "gmp-native";

    public final static class Builder implements FunctionEvaluator.Builder {
        private final ArrayList<ValueFunction> functions = new ArrayList<>();
        private boolean useIntervals;
        private TypeAlgebra resultType;

        @Override
        public Builder addFunction(ValueFunction function) {
            assert function != null;
            functions.add(function);
            return this;
        }

        @Override
        public Builder setPointsUseIntervals(boolean useIntervals) {
            this.useIntervals = useIntervals;
            return this;
        }
        
        @Override
        public Builder setResultType(TypeAlgebra type) {
            this.resultType = type;
            return this;
        }

        @Override
        public EvaluatorDagGMPNative build() {
            for (ValueFunction function : functions) {
                if (!ValueDag.is(function)) {
                    return null;
                }
            }
            if (useIntervals) {
                return null;
            }
            if (!TypeMPQ.is(resultType)) {
                return null;
            }
            return new EvaluatorDagGMPNative(this);
        }
    }

    private final TypeMPQ resultType;
    private final int resultDimensions;
    private final ValueArrayMPQ variables;
    private final ValueMPQ valueMPQ;
    private final int[] program;
    private final ValueArrayMPQ numbers;
    private final int numStatements;
    private final int numResultVariables;
    private final int[] resultVariables;
    
    private EvaluatorDagGMPNative(Builder builder) {
        assert builder != null;
        assert builder.functions != null;
        for (ValueFunction function : builder.functions) {
            assert function != null;
        }
        assert builder.resultType != null;
        resultType = TypeMPQ.as(builder.resultType);
        int[] start = new int[builder.functions.size()];
        int index = 0;
        for (ValueFunction function : builder.functions) {
            start[index] = ValueDag.as(function).getNumber();
            index++;
        }
        resultDimensions = builder.functions.size();
        TypeDag typeDag = ValueDag.as(builder.functions.get(0)).getType();
        valueMPQ = resultType.newValue();
        Microcode microcode = new Microcode(typeDag, start);
        program = microcodeToInteger(microcode);
        variables = UtilValue.newArray(resultType.getTypeArray(), microcode.getNumVariables());
        numbers = ValueArrayMPQ.as(microcodeToNumbersListRational(microcode));
        numStatements = microcode.getNumStatements();
        numResultVariables = microcode.getNumResultVariables();
        resultVariables = new int[numResultVariables];
        for (index = 0; index < numResultVariables; index++) {
            resultVariables[index] = microcode.getResultVariable(index);
        }
    }

    @Override
    public TypeAlgebra getResultType() {
        return resultType;
    }

    @Override
    public void evaluate(ValueArrayAlgebra result, ValueArrayAlgebra point) {
        assert result != null;
        assert point != null;
        GMP.evaluate_gmp(variables.getContent(), program, numStatements, numbers.getContent(), ValueArrayMPQ.as(ValueArrayInterval.as(point).getContent()).getContent());
        for (int index = 0; index < numResultVariables; index++) {
            int resultVariable = resultVariables[index];
            variables.get(valueMPQ, resultVariable);
            result.set(valueMPQ, index);
        }
    }

    @Override
    public int getResultDimensions() {
        return resultDimensions;
    }
}
