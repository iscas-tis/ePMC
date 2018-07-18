package epmc.param.value.dag.evaluator;

import java.util.ArrayList;

import epmc.operator.OperatorSet;
import epmc.param.value.FunctionEvaluator;
import epmc.param.value.ValueFunction;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.UtilDag;
import epmc.param.value.dag.ValueDag;
import epmc.param.value.dag.microcode.Microcode;
import epmc.param.value.rational.TypeRational;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeDouble;
import epmc.value.TypeInterval;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueDouble;
import epmc.value.ValueInterval;
import static epmc.param.value.dag.microcode.UtilMicrocode.microcodeToInteger;
import static epmc.param.value.dag.microcode.UtilMicrocode.microcodeToNumbersListDoubleInterval;
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
 * Evaluator for points to double interval values implemented in Java.
 * Should be reasonable fast.
 * 
 * @author Ernst Moritz Hahn
 *
 */
public final class EvaluatorDagDoubleIntervalJava implements FunctionEvaluator {
    public final static String IDENTIFIER = "double-interval-java";

    public final static class Builder implements FunctionEvaluator.Builder {
        private final ArrayList<ValueFunction> functions = new ArrayList<>();
        private TypeAlgebra resultType;

        @Override
        public Builder addFunction(ValueFunction function) {
            assert function != null;
            functions.add(function);
            return this;
        }

        @Override
        public Builder setPointsUseIntervals(boolean useIntervals) {
            return this;
        }
        
        @Override
        public Builder setResultType(TypeAlgebra type) {
            this.resultType = type;
            return this;
        }

        @Override
        public EvaluatorDagDoubleIntervalJava build() {
            for (ValueFunction function : functions) {
                if (!ValueDag.is(function)) {
                    return null;
                }
            }
            if (!TypeInterval.is(resultType)) {
                return null;
            }
            if (!TypeDouble.is(TypeInterval.as(resultType).getEntryType())) {
                return null;
            }
            return new EvaluatorDagDoubleIntervalJava(this);
        }
    }

    private final TypeInterval resultType;
    private final int resultDimensions;
    private final double[] variables;
    private final ValueInterval pointValue;
    private final ValueInterval valueInterval;
    private final ValueDouble valueIntervalLower;
    private final ValueDouble valueIntervalUpper;
    private final int[] program;
    private final double[] numbers;
    private final int numStatements;
    private final int numResultVariables;
    private final int[] resultVariables;
    private final OperatorEvaluator setFromPoint;
    
    private EvaluatorDagDoubleIntervalJava(Builder builder) {
        assert builder != null;
        assert builder.functions != null;
        for (ValueFunction function : builder.functions) {
            assert function != null;
        }
        assert builder.resultType != null;
        resultType = TypeInterval.as(builder.resultType);
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
        valueInterval = resultType.newValue();
        valueIntervalLower = ValueDouble.as(valueInterval.getIntervalLower());
        valueIntervalUpper = ValueDouble.as(valueInterval.getIntervalUpper());
        Microcode microcode = new Microcode(typeDag, start);
        program = microcodeToInteger(microcode);
        variables = new double[microcode.getNumVariables() * 2];
        numbers = microcodeToNumbersListDoubleInterval(microcode);
        numStatements = microcode.getNumStatements();
        numResultVariables = microcode.getNumResultVariables();
        resultVariables = new int[numResultVariables];
        for (index = 0; index < numResultVariables; index++) {
            resultVariables[index] = microcode.getResultVariable(index);
        }
        setFromPoint = ContextValue.get().getEvaluator(OperatorSet.SET, typeInterval, resultType);
    }

    @Override
    public TypeAlgebra getResultType() {
        return resultType;
    }

    @Override
    public void evaluate(ValueArrayAlgebra result, ValueArrayAlgebra point) {
        assert result != null;
        assert point != null;
        double operandLower;
        double operandUpper;
        double operandLeftLower;
        double operandLeftUpper;
        double operandRightLower;
        double operandRightUpper;
        double valueLower;
        double valueUpper;
        for (int index = 0; index < numStatements; index++) {
            int operator = getOperator(program, index);
            switch (operator) {
            case OP_NUMBER: {
                valueLower = numbers[getOperandLeft(program, index) * 2];
                valueUpper = numbers[getOperandLeft(program, index) * 2 + 1];
                break;
            }
            case OP_PARAMETER:
                point.get(pointValue, getOperandLeft(program, index));
                setFromPoint.apply(valueInterval, pointValue);
                valueLower = valueIntervalLower.getDouble();
                valueUpper = valueIntervalUpper.getDouble();
                break;
            case OP_ADD_INVERSE:
                operandLower = variables[getOperandLeft(program, index) * 2];
                operandUpper = variables[getOperandLeft(program, index) * 2 + 1];
                valueLower = -operandUpper;
                valueUpper = -operandLower;
                break;
            case OP_MULTIPLY_INVERSE:
                operandLower = variables[getOperandLeft(program, index) * 2];
                operandUpper = variables[getOperandLeft(program, index) * 2 + 1];
                if (operandLower == 0.0 || operandUpper == 0.0
                        || operandLower < 0.0 && operandUpper > 0.0) {
                    valueLower = Double.NEGATIVE_INFINITY;
                    valueUpper = Double.POSITIVE_INFINITY;
                } else {
                    valueLower = Math.nextDown(1.0/operandUpper);
                    valueUpper = Math.nextUp(1.0/operandLower);
                }
                break;
            case OP_ADD:
                operandLeftLower = variables[getOperandLeft(program, index) * 2];
                operandLeftUpper = variables[getOperandLeft(program, index) * 2 + 1];
                operandRightLower = variables[getOperandRight(program, index) * 2];
                operandRightUpper = variables[getOperandRight(program, index) * 2 + 1];
                valueLower = Math.nextDown(operandLeftLower + operandRightLower);
                valueUpper = Math.nextUp(operandLeftUpper + operandRightUpper);
                break;
            case OP_MULTIPLY:
                operandLeftLower = variables[getOperandLeft(program, index) * 2];
                operandLeftUpper = variables[getOperandLeft(program, index) * 2 + 1];
                operandRightLower = variables[getOperandRight(program, index) * 2];
                operandRightUpper = variables[getOperandRight(program, index) * 2 + 1];
                double a = operandLeftLower * operandRightLower;
                double b = operandLeftLower * operandRightUpper;
                double c = operandLeftUpper * operandRightLower;
                double d = operandLeftUpper * operandRightUpper;
                valueLower = Math.nextDown(Math.min(Math.min(a, b), Math.min(c, d)));
                valueUpper = Math.nextUp(Math.max(Math.max(a, b), Math.max(c, d)));
                break;
            default:
                valueLower = Double.NaN;
                valueUpper = Double.NaN;
                assert false;
                break;
            }
            variables[getAssignedTo(program, index) * 2] = valueLower;
            variables[getAssignedTo(program, index) * 2 + 1] = valueUpper;
        }        
        for (int index = 0; index < numResultVariables; index++) {
            int resultVariable = resultVariables[index];
            valueIntervalLower.set(variables[resultVariable * 2]);
            valueIntervalUpper.set(variables[resultVariable * 2 + 1]);
            result.set(valueInterval, index);
        }
    }

    @Override
    public int getResultDimensions() {
        return resultDimensions;
    }
}
