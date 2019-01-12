package epmc.param.value.dag;

import java.math.BigInteger;

import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public final class UtilDag {
    // http://sergebg.blogspot.co.uk/2014/11/non-recursive-dfs-topological-sort.html
    public static int[] findDepending(Dag dag, int number) {
        assert dag != null;
        assert number >= 0;
        IntArrayList result = new IntArrayList();
        BitSet visited = new BitSetUnboundedLongArray();
        IntArrayList todo = new IntArrayList();
        todo.push(number);
        visited.set(number);
        while (todo.size() > 0) {
            int next = todo.popInt();
            if (next >= 0) {
                todo.push(-next - 1);
                OperatorType type = dag.getOperatorType(next);
                switch (type) {
                case NUMBER:
                case PARAMETER:
                    break;
                case ADD_INVERSE:
                    int addInverseOperand = dag.getOperand(next);
                    if (!visited.get(addInverseOperand)) {
                        todo.push(addInverseOperand);
                        visited.set(addInverseOperand);
                    }
                    break;
                case MULTIPLY_INVERSE:
                    int multiplyInverseOperand = dag.getOperand(next);
                    if (!visited.get(multiplyInverseOperand)) {
                        todo.push(multiplyInverseOperand);
                        visited.set(multiplyInverseOperand);
                    }
                    break;
                case ADD:
                    int addOperandLeft = dag.getOperandLeft(next);
                    int addOperandRight = dag.getOperandRight(next);
                    if (!visited.get(addOperandLeft)) {
                        todo.push(addOperandLeft);
                        visited.set(addOperandLeft);
                    }
                    if (!visited.get(addOperandRight)) {
                        todo.push(addOperandRight);
                        visited.set(addOperandRight);
                    }
                    break;
                case MULTIPLY:
                    int multiplyOperandLeft = dag.getOperandLeft(next);
                    int multiplyOperandRight = dag.getOperandRight(next);
                    if (!visited.get(multiplyOperandLeft)) {
                        todo.push(multiplyOperandLeft);
                        visited.set(multiplyOperandLeft);
                    }
                    if (!visited.get(multiplyOperandRight)) {
                        todo.push(multiplyOperandRight);
                        visited.set(multiplyOperandRight);
                    }
                    break;
                default:
                    assert false;
                    break;
                }
            } else {
                result.add(-next - 1);
            }
        }
        return result.toIntArray();
    }

    public static int[] findDepending(Dag dag, int[] numbers) {
        assert dag != null;
        assert numbers != null;
        IntArrayList result = new IntArrayList();
        BitSet visited = new BitSetUnboundedLongArray();
        IntArrayList todo = new IntArrayList();
        for (int number : numbers) {
            if (visited.get(number)) {
                continue;
            }
            todo.push(number);
            visited.set(number);
            while (todo.size() > 0) {
                int next = todo.popInt();
                if (next >= 0) {
                    todo.push(-next - 1);
                    OperatorType type = dag.getOperatorType(next);
                    switch (type) {
                    case NUMBER:
                    case PARAMETER:
                        break;
                    case ADD_INVERSE:
                        int addInverseOperand = dag.getOperand(next);
                        if (!visited.get(addInverseOperand)) {
                            todo.push(addInverseOperand);
                            visited.set(addInverseOperand);
                        }
                        break;
                    case MULTIPLY_INVERSE:
                        int multiplyInverseOperand = dag.getOperand(next);
                        if (!visited.get(multiplyInverseOperand)) {
                            todo.push(multiplyInverseOperand);
                            visited.set(multiplyInverseOperand);
                        }
                        break;
                    case ADD:
                        int addOperandLeft = dag.getOperandLeft(next);
                        int addOperandRight = dag.getOperandRight(next);
                        if (!visited.get(addOperandLeft)) {
                            todo.push(addOperandLeft);
                            visited.set(addOperandLeft);
                        }
                        if (!visited.get(addOperandRight)) {
                            todo.push(addOperandRight);
                            visited.set(addOperandRight);
                        }
                        break;
                    case MULTIPLY:
                        int multiplyOperandLeft = dag.getOperandLeft(next);
                        int multiplyOperandRight = dag.getOperandRight(next);
                        if (!visited.get(multiplyOperandLeft)) {
                            todo.push(multiplyOperandLeft);
                            visited.set(multiplyOperandLeft);
                        }
                        if (!visited.get(multiplyOperandRight)) {
                            todo.push(multiplyOperandRight);
                            visited.set(multiplyOperandRight);
                        }
                        break;
                    default:
                        assert false;
                        break;
                    }
                } else {
                    result.add(-next - 1);
                }
            }
        }
        return result.toIntArray();
    }

    public static ValueDag derive(ValueDag dag, String parameter) {
        int number = derive(dag.getType().getDag(), dag.getNumber(), parameter);
        ValueDag result = dag.getType().newValue();
        result.setNumber(number);
        return result;
    }
    
    public static int derive(Dag dag, int start, String parameter) {
        assert dag != null;
        assert start >= 0 : start;
        assert parameter != null;
        assert dag.getParameters().isParameter(parameter) : parameter + " "
                + dag.getParameters().getParameters();
        int[] depending = findDepending(dag, start);
        int[] numberToIndex = prepareNumberToIndex(depending);
        int[] derived = new int[depending.length];
        for (int index = 0; index < depending.length; index++) {
            int number = depending[index];
            derived[index] = derive(dag, depending, derived, numberToIndex, number, parameter);
        }
        return derived[derived.length - 1];
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

    private static int derive(Dag dag, int[] depending, int[] derived, int[] numberToIndex, int function,
            Object parameter) {
        switch (dag.getOperatorType(function)) {
        case NUMBER:
            return deriveNumber(dag);
        case PARAMETER:
            return deriveParameter(dag, function, parameter);
        case ADD_INVERSE:
            return deriveAddInverse(dag, derived, numberToIndex, function);
        case MULTIPLY_INVERSE:
            return deriveMultiplyInverse(dag, derived, numberToIndex, function);
        case ADD:
            return deriveAdd(dag, derived, numberToIndex, function);
        case MULTIPLY:
            return deriveMultiply(dag, derived, numberToIndex, function);
        default:
            assert false;
            return Integer.MIN_VALUE;
        }
    }

    private static int deriveNumber(Dag dag) {
        return dag.getNumber(BigInteger.ZERO, BigInteger.ONE);
    }

    private static int deriveParameter(Dag dag, int number, Object parameter) {
        if (dag.getParameter(number).equals(parameter)) {
            return dag.getNumber(BigInteger.ONE, BigInteger.ONE);
        } else {
            return dag.getNumber(BigInteger.ZERO, BigInteger.ONE);
        }
    }

    private static int deriveAddInverse(Dag dag, int[] derived, int[] numberToIndex, int number) {
        int operand = dag.getOperand(number);
        int operandIndex = numberToIndex[operand];
        return dag.apply(OperatorType.ADD_INVERSE, derived[operandIndex]);
    }

    private static int deriveMultiplyInverse(Dag dag, int[] derived, int[] numberToIndex, int number) {
        int operand = dag.getOperand(number);
        int operandIndex = numberToIndex[operand];
        
        return dag.apply(OperatorType.ADD_INVERSE,
                dag.apply(OperatorType.MULTIPLY, derived[operandIndex],
                        dag.apply(OperatorType.MULTIPLY_INVERSE, dag.apply(OperatorType.MULTIPLY, operand, operand))));
    }

    private static int deriveAdd(Dag dag, int[] derived, int[] numberToIndex, int number) {
        int operandLeft = dag.getOperandLeft(number);
        int operandRight = dag.getOperandRight(number);
        int operandLeftIndex = numberToIndex[operandLeft];
        int operandRightIndex = numberToIndex[operandRight];
        return dag.apply(OperatorType.ADD, derived[operandLeftIndex], derived[operandRightIndex]);
    }

    private static int deriveMultiply(Dag dag, int[] derived, int[] numberToIndex, int number) {
        int operandLeft = dag.getOperandLeft(number);
        int operandRight = dag.getOperandRight(number);
        int operandLeftIndex = numberToIndex[operandLeft];
        int operandRightIndex = numberToIndex[operandRight];
        return dag.apply(OperatorType.ADD, dag.apply(OperatorType.MULTIPLY, derived[operandLeftIndex], operandRight),
                dag.apply(OperatorType.MULTIPLY, operandLeft, derived[operandRightIndex]));
    }

    private UtilDag() {
    }
}
