package epmc.param.value.dag.evaluator;

import java.math.BigInteger;
import java.util.ArrayList;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorMultiplyInverse;
import epmc.operator.OperatorSet;
import epmc.param.value.FunctionEvaluator;
import epmc.param.value.ValueFunction;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.param.value.dag.microcode.Microcode;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueRational;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeInterval;
import epmc.value.UtilValue;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInterval;

/**
 * General evalulator for points.
 * This class is intended to be used as a reference, it will be quite slow
 * for most purposes.
 * 
 * @author Ernst Moritz Hahn
 *
 */
public final class EvaluatorDagSimpleGeneral implements FunctionEvaluator {
    public final static String IDENTIFIER = "general-dag";
    
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
        public EvaluatorDagSimpleGeneral build() {
            for (ValueFunction function : functions) {
                if (!ValueDag.is(function)) {
                    return null;
                }
            }
            return new EvaluatorDagSimpleGeneral(this);
        }
    }

    private final TypeAlgebra resultType;
    private final Microcode microcode;
    private final int resultDimensions;
    private final ValueArrayAlgebra variables;
    private final ValueAlgebra value;
    private final ValueRational valueRational;
    private final ValueInterval pointValue;
    private final OperatorEvaluator addInverse;
    private final OperatorEvaluator multiplyInverse;
    private final OperatorEvaluator add;
    private final OperatorEvaluator multiply;
    private final ValueAlgebra operand;
    private final ValueAlgebra operandLeft;
    private final ValueAlgebra operandRight;
    private final OperatorEvaluator setPointValue;
    private final OperatorEvaluator set;
    
    private EvaluatorDagSimpleGeneral(Builder builder) {
        assert builder != null;
        resultType = builder.resultType;
        int[] start = new int[builder.functions.size()];
        int index = 0;
        for (ValueFunction f : builder.functions) {
            ValueDag function = ValueDag.as(f);
            start[index] = function.getNumber();
            index++;
        }
        resultDimensions = builder.functions.size();
        TypeDag typeDag = ValueDag.as(builder.functions.get(0)).getType();
        microcode = new Microcode(typeDag, start);
        variables = UtilValue.newArray(resultType.getTypeArray(), microcode.getNumVariables());
        value = resultType.newValue();
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(TypeRational.get()));
        pointValue = typeInterval.newValue();
        addInverse = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, resultType);
        multiplyInverse = ContextValue.get().getEvaluator(OperatorMultiplyInverse.MULTIPLY_INVERSE, resultType);
        add = ContextValue.get().getEvaluator(OperatorAdd.ADD, resultType, resultType);
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, resultType, resultType);
        operand = resultType.newValue();
        operandLeft = resultType.newValue();
        operandRight = resultType.newValue();
        if (TypeInterval.is(resultType)) {
            setPointValue = ContextValue.get().getEvaluator(OperatorSet.SET, typeInterval, resultType);
        } else {
            setPointValue = ContextValue.get().getEvaluator(OperatorSet.SET, typeInterval.getEntryType(), resultType);
        }
        set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeRational.get(), resultType);
        valueRational = TypeRational.get().newValue();
    }

    @Override
    public TypeAlgebra getResultType() {
        return resultType;
    }

    @Override
    public void evaluate(ValueArrayAlgebra result, ValueArrayAlgebra point) {
        assert result != null;
        assert point != null;
        for (int index = 0; index < microcode.getNumStatements(); index++) {
            OperatorType operator = microcode.getOperator(index);
            switch (operator) {
            case NUMBER: {
                BigInteger num = microcode.getNumberNumerator(index);
                BigInteger den = microcode.getNumberDenominator(index);
                valueRational.set(num, den);
                set.apply(value, valueRational);
                break;
            }
            case PARAMETER:
                point.get(pointValue, microcode.getParameter(index));
                setValueFromPointValue(value, pointValue);
                break;
            case ADD_INVERSE:
                variables.get(operand, microcode.getOperand(index));
                addInverse.apply(value, operand);
                break;
            case MULTIPLY_INVERSE:
                variables.get(operand, microcode.getOperand(index));
                multiplyInverse.apply(value, operand);
                break;
            case ADD:
                variables.get(operandLeft, microcode.getOperandLeft(index));
                variables.get(operandRight, microcode.getOperandRight(index));
                add.apply(value, operandLeft, operandRight);
                break;
            case MULTIPLY:
                variables.get(operandLeft, microcode.getOperandLeft(index));
                variables.get(operandRight, microcode.getOperandRight(index));
                multiply.apply(value, operandLeft, operandRight);
                break;
            default:
                assert false;
                break;
            }
            variables.set(value, microcode.getAssignedTo(index));
        }
        for (int index = 0; index < microcode.getNumResultVariables(); index++) {
            int resultVariable = microcode.getResultVariable(index);
            variables.get(value, resultVariable);
            result.set(value, index);
        }
    }

    private void setValueFromPointValue(ValueAlgebra value, ValueInterval pointValue) {
        if (ValueInterval.is(value)) {
            setPointValue.apply(value, pointValue);
        } else if (pointValue.getIntervalLower().equals(pointValue.getIntervalUpper())) {
            setPointValue.apply(value, pointValue.getIntervalLower());
        } else {
            assert false;
        }
    }

    @Override
    public int getResultDimensions() {
        return resultDimensions;
    }

}
