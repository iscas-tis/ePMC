package epmc.param.value.dag.evaluator;

import java.util.ArrayList;

import epmc.param.value.FunctionEvaluator;
import epmc.param.value.ValueFunction;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.param.value.dag.microcode.Microcode;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.ContextValue;
import epmc.value.TypeAlgebra;
import epmc.value.TypeDouble;
import epmc.value.TypeInterval;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueDouble;
import epmc.value.ValueInterval;
import static epmc.param.value.dag.microcode.UtilMicrocode.microcodeToInteger;
import static epmc.param.value.dag.microcode.UtilMicrocode.microcodeToNumbersListDouble;
import static epmc.param.value.dag.microcode.UtilMicrocode.getAssignedTo;
import static epmc.param.value.dag.microcode.UtilMicrocode.getOperator;
import static epmc.param.value.dag.microcode.UtilMicrocode.getOperandLeft;
import static epmc.param.value.dag.microcode.UtilMicrocode.getOperandRight;
import static epmc.param.value.dag.microcode.UtilMicrocode.OP_ADD;
import static epmc.param.value.dag.microcode.UtilMicrocode.OP_ADD_INVERSE;
import static epmc.param.value.dag.microcode.UtilMicrocode.OP_MULTIPLY;
import static epmc.param.value.dag.microcode.UtilMicrocode.OP_MULTIPLY_INVERSE;
import static epmc.param.value.dag.microcode.UtilMicrocode.OP_NUMBER;
import static epmc.param.value.dag.microcode.UtilMicrocode.OP_PARAMETER;

/**
 * Evaluator for points to double values implemented in Java.
 * Should be reasonable fast.
 * 
 * @author Ernst Moritz Hahn
 *
 */
public final class EvaluatorDagDoubleJava implements FunctionEvaluator {
    public final static String IDENTIFIER = "double-java";

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
        public EvaluatorDagDoubleJava build() {
            for (ValueFunction function : functions) {
                if (!ValueDag.is(function)) {
                    return null;
                }
            }
            if (useIntervals) {
                return null;
            }
            if (!TypeDouble.is(resultType)) {
                return null;
            }
            return new EvaluatorDagDoubleJava(this);
        }
    }

    private final TypeDouble resultType;
    private final int resultDimensions;
    private final double[] variables;
    private final ValueInterval pointValue;
    private final ValueDouble valueDouble;
    private final int[] program;
    private final double[] numbers;
    private final int numStatements;
    private final int numResultVariables;
    private final int[] resultVariables;
    
    private EvaluatorDagDoubleJava(Builder builder) {
        assert builder != null;
        assert builder.functions != null;
        for (ValueFunction function : builder.functions) {
            assert function != null;
        }
        assert builder.resultType != null;
        resultType = TypeDouble.as(builder.resultType);
        int[] start = new int[builder.functions.size()];
        int index = 0;
        for (ValueFunction function : builder.functions) {
            start[index] = ValueDag.as(function).getNumber();
            index++;
        }
        resultDimensions = builder.functions.size();
        TypeDag typeDag = ValueDag.as(builder.functions.get(0)).getType();
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(TypeRational.get()));
        pointValue = typeInterval.newValue();
        valueDouble = resultType.newValue();
        Microcode microcode = new Microcode(typeDag, start);
        program = microcodeToInteger(microcode);
        variables = new double[microcode.getNumVariables()];
        numbers = microcodeToNumbersListDouble(microcode);
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
        double operand;
        double operandLeft;
        double operandRight;
        double value;
        for (int index = 0; index < numStatements; index++) {
            int operator = getOperator(program, index);
            switch (operator) {
            case OP_NUMBER: {
                value = numbers[getOperandLeft(program, index)];
                break;
            }
            case OP_PARAMETER:
                point.get(pointValue, getOperandLeft(program, index));
                value = getValueFromPointValue(pointValue);
                break;
            case OP_ADD_INVERSE:
                operand = variables[getOperandLeft(program, index)];
                value = -operand;
                break;
            case OP_MULTIPLY_INVERSE:
                operand = variables[getOperandLeft(program, index)];
                value = 1.0/operand;
                break;
            case OP_ADD:
                operandLeft = variables[getOperandLeft(program, index)];
                operandRight = variables[getOperandRight(program, index)];
                value = operandLeft + operandRight;
                break;
            case OP_MULTIPLY:
                operandLeft = variables[getOperandLeft(program, index)];
                operandRight = variables[getOperandRight(program, index)];
                value = operandLeft * operandRight;
                break;
            default:
                value = -1;
                assert false;
                break;
            }
            variables[getAssignedTo(program, index)] = value;
        }        
        for (int index = 0; index < numResultVariables; index++) {
            int resultVariable = resultVariables[index];
            valueDouble.set(variables[resultVariable]);
            result.set(valueDouble, index);
        }
    }

    private double getValueFromPointValue(ValueInterval pointValue) {
        assert pointValue != null;
        return ValueRational.as(pointValue.getIntervalLower()).getDouble();
    }

    @Override
    public int getResultDimensions() {
        return resultDimensions;
    }
}
