package epmc.param.value.dag.microcode;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import epmc.param.points.Side;
import epmc.param.points.UtilPoints;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import epmc.param.value.rational.TypeRational;
import epmc.param.value.rational.ValueArrayRational;
import epmc.param.value.rational.ValueRational;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedIntArray;
import epmc.value.UtilValue;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class UtilMicrocode {
    public final static int OP_PARAMETER = 0;
    public final static int OP_NUMBER = 1;
    public final static int OP_ADD_INVERSE = 2;
    public final static int OP_MULTIPLY_INVERSE = 3;
    public final static int OP_ADD = 4;
    public final static int OP_MULTIPLY = 5;
    public final static Map<OperatorType,Integer> OPERATOR_TYPE_TO_NUMBER;
    static {
        Map<OperatorType,Integer> operatorTypeToInteger = new HashMap<>();
        operatorTypeToInteger.put(OperatorType.PARAMETER, OP_PARAMETER);
        operatorTypeToInteger.put(OperatorType.NUMBER, OP_NUMBER);
        operatorTypeToInteger.put(OperatorType.ADD_INVERSE, OP_ADD_INVERSE);
        operatorTypeToInteger.put(OperatorType.MULTIPLY_INVERSE, OP_MULTIPLY_INVERSE);
        operatorTypeToInteger.put(OperatorType.ADD, OP_ADD);
        operatorTypeToInteger.put(OperatorType.MULTIPLY, OP_MULTIPLY);
        OPERATOR_TYPE_TO_NUMBER = Collections.unmodifiableMap(operatorTypeToInteger);
    }

    public static int[] microcodeToInteger(Microcode microcode) {
        assert microcode != null;
        int[] program = new int[microcode.getNumStatements() * 4];
        DoubleArrayList numbersList = new DoubleArrayList();
        for (int index = 0; index < microcode.getNumStatements(); index++) {
            program[index * 4] = OPERATOR_TYPE_TO_NUMBER.get(microcode.getOperator(index));
            program[index * 4 + 1] = microcode.getAssignedTo(index);
            switch (microcode.getOperator(index)) {
            case ADD_INVERSE:
            case MULTIPLY_INVERSE:
                program[index * 4 + 2] = microcode.getOperand(index);
                break;
            case PARAMETER:
                program[index * 4 + 2] = microcode.getParameter(index);
                break;
            case NUMBER:
                BigInteger num = microcode.getNumberDenominator(index);
                BigInteger den = microcode.getNumberDenominator(index);
                program[index * 4 + 2] = numbersList.size();
                numbersList.add(UtilPoints.fractionToDouble(num, den, Side.MIDDLE));
                break;
            case ADD:
            case MULTIPLY:
                program[index * 4 + 2] = microcode.getOperandLeft(index);
                program[index * 4 + 3] = microcode.getOperandRight(index);
                break;
            default:
                break;
            
            }
        }
        return program;
    }

    public static double[] microcodeToNumbersListDouble(Microcode microcode) {
        assert microcode != null;
        DoubleArrayList numbersList = new DoubleArrayList();
        for (int index = 0; index < microcode.getNumStatements(); index++) {
            switch (microcode.getOperator(index)) {
            case NUMBER:
                BigInteger num = microcode.getNumberNumerator(index);
                BigInteger den = microcode.getNumberDenominator(index);
                numbersList.add(UtilPoints.fractionToDouble(num, den, Side.MIDDLE));
                break;
            default:
                break;            
            }
        }
        return numbersList.toDoubleArray();
    }

    public static double[] microcodeToNumbersListDoubleInterval(Microcode microcode) {
        assert microcode != null;
        DoubleArrayList numbersList = new DoubleArrayList();
        for (int index = 0; index < microcode.getNumStatements(); index++) {
            switch (microcode.getOperator(index)) {
            case NUMBER:
                BigInteger num = microcode.getNumberNumerator(index);
                BigInteger den = microcode.getNumberDenominator(index);
                numbersList.add(UtilPoints.fractionToDouble(num, den, Side.LEFT));
                numbersList.add(UtilPoints.fractionToDouble(num, den, Side.RIGHT));
                break;
            default:
                break;            
            }
        }
        return numbersList.toDoubleArray();
    }

    public static ValueArrayRational microcodeToNumbersListRational(Microcode microcode) {
        assert microcode != null;
        int size = 0;
        for (int index = 0; index < microcode.getNumStatements(); index++) {
            switch (microcode.getOperator(index)) {
            case NUMBER:
                size++;
                break;
            default:
                break;
            }
        }
        ValueRational value = TypeRational.get().newValue();
        ValueArrayRational numbersList = UtilValue.newArray(TypeRational.get().getTypeArray(), size);
        int numberIndex = 0;
        for (int index = 0; index < microcode.getNumStatements(); index++) {
            switch (microcode.getOperator(index)) {
            case NUMBER:
                BigInteger num = microcode.getNumberNumerator(index);
                BigInteger den = microcode.getNumberDenominator(index);
                value.set(num, den);
                numbersList.set(value, numberIndex);
                numberIndex++;
                break;
            default:
                break;            
            }
        }
        return numbersList;
    }

    public static int getOperator(int[] program, int index) {
        return program[index * 4];
    }

    public static int getAssignedTo(int[] program, int index) {
        return program[index * 4 + 1];
    }

    public static int getOperandLeft(int[] program, int index) {
        return program[index * 4 + 2];
    }

    public static int getOperandRight(int[] program, int index) {
        return program[index * 4 + 3];
    }

    static VariableAssignment assignVariables(Dag dag, int[] relevant, int[] start) {
        int[] nodeToIndex = UtilMicrocode.prepareNumberToIndex(relevant);
        int[] lastRead = UtilMicrocode.prepareLastRead(dag, relevant, nodeToIndex);
        for (int node : start) {
            lastRead[nodeToIndex[node]] = lastRead.length;
        }
        IntArrayList[] readingDone = UtilMicrocode.prepareReadingDone(lastRead);
        int numVariables = UtilMicrocode.countRequiredVariables(readingDone);
        int[] assignment = new int[relevant.length];
        Arrays.fill(assignment, -1);
        IntArrayList available = new IntArrayList();
        for (int variable = 0; variable < numVariables; variable++) {
            available.push(numVariables - variable - 1);
        }
        BitSet variablesAssigned = new BitSetUnboundedIntArray();
        for (int index = 0; index < readingDone.length; index++) {
            IntArrayList nowDoneList = readingDone[index];
            for (int j = 0; j < nowDoneList.size(); j++) {
                int nowDone = nowDoneList.getInt(j);
                int variableDone = assignment[nowDone];
                assert variableDone >= 0 : variableDone;
                assert variablesAssigned.get(variableDone) : index;
                assert variableDone != -1 : index;
                variablesAssigned.clear(variableDone);
                available.push(variableDone);
            }
            int assignedVariable = available.popInt();
            assert !variablesAssigned.get(assignedVariable) : index;
            variablesAssigned.set(assignedVariable);
            assignment[index] = assignedVariable;
        }
        return new VariableAssignment(numVariables, assignment, nodeToIndex);
    }

    private static int countRequiredVariables(IntArrayList[] readingDone) {
        int numActive = 0;
        int maxActive = 0;
        for (int index = 0; index < readingDone.length; index++) {
            numActive++;
            numActive -= readingDone[index].size();
            maxActive = Math.max(maxActive, numActive);
        }
        return maxActive;
    }

    private static int[] prepareLastRead(Dag dag, int[] relevant, int[] nodeToIndex) {
        int[] lastRead = new int[relevant.length];
        for (int index = 0; index < relevant.length; index++) {
            int node = relevant[index];
            switch (dag.getOperatorType(node)) {
            case NUMBER: case PARAMETER:
                break;
            case ADD_INVERSE: {
                int operand = dag.getOperand(node);
                int operandIndex = nodeToIndex[operand];
                lastRead[operandIndex] = index;
                break;
            }
            case MULTIPLY_INVERSE: {
                int operand = dag.getOperand(node);
                int operandIndex = nodeToIndex[operand];
                lastRead[operandIndex] = index;
                break;
            }
            case ADD: {
                int operandLeft = dag.getOperandLeft(node);
                int operandLeftIndex = nodeToIndex[operandLeft];
                int operandRight = dag.getOperandRight(node);
                int operandRightIndex = nodeToIndex[operandRight];
                lastRead[operandLeftIndex] = index;
                lastRead[operandRightIndex] = index;
                break;
            }
            case MULTIPLY: {
                int operandLeft = dag.getOperandLeft(node);
                int operandLeftIndex = nodeToIndex[operandLeft];
                int operandRight = dag.getOperandRight(node);
                int operandRightIndex = nodeToIndex[operandRight];
                lastRead[operandLeftIndex] = index;
                lastRead[operandRightIndex] = index;
                break;
            }
            default:
                assert false;
                break;            
            }
        }
        return lastRead;
    }

    private static IntArrayList[] prepareReadingDone(int[] lastRead) {
        IntArrayList[] whenDone = new IntArrayList[lastRead.length];
        for (int index = 0; index < lastRead.length; index++) {
            int whenLastRead = lastRead[index];
            if (whenLastRead >= whenDone.length) {
                continue;
            }
            IntArrayList list = whenDone[whenLastRead];
            if (list == null) {
                list = new IntArrayList();
                whenDone[whenLastRead] = list;
            }
            list.add(index);
        }
        for (int index = 0; index < whenDone.length; index++) {
            if (whenDone[index] == null) {
                whenDone[index] = new IntArrayList();
            }
        }
        return whenDone;
    }

    private static int[] prepareNumberToIndex(int[] depending) {
        int max = 0;
        for (int index = 0; index < depending.length; index++) {
            max = Math.max(max, depending[index]);
        }
        int[] numberToIndex = new int[max + 1];        
        for (int index = 0; index < depending.length; index++) {
            numberToIndex[depending[index]] = index;
        }
        return numberToIndex;
    }
    
    private UtilMicrocode() {
    }
}
