package epmc.param.value.dag;

final class EntryUtil {
    private final static int NUM_OPERATORS = OperatorType.values().length;
    private final static int NUM_OPERATOR_BITS;
    static {
        int numOperatorBits = ceilLogInt(NUM_OPERATORS);
        numOperatorBits += numOperatorBits % 2 == 1 ? 1 : 0;
        NUM_OPERATOR_BITS = numOperatorBits;
    }
    private final static int NUM_OPERAND_BITS = (Long.SIZE - NUM_OPERATOR_BITS) / 2;
    private final static long TYPE_MASK = makeBitMask(0, NUM_OPERATOR_BITS);
    private final static int LEFT_OPERAND_BEGIN = NUM_OPERATOR_BITS;
    private final static int LEFT_OPERAND_END = NUM_OPERATOR_BITS + NUM_OPERAND_BITS;
    private final static long LEFT_OPERAND_MASK
    = makeBitMask(LEFT_OPERAND_BEGIN, LEFT_OPERAND_END);
    private final static int RIGHT_OPERAND_BEGIN = NUM_OPERATOR_BITS + NUM_OPERAND_BITS;
    private final static int RIGHT_OPERAND_END = RIGHT_OPERAND_BEGIN + NUM_OPERAND_BITS;
    private final static long RIGHT_OPERAND_MASK
    = makeBitMask(RIGHT_OPERAND_BEGIN, RIGHT_OPERAND_END);
    private final static String SPACE = " ";
    
    private static long makeBitMask(int from, int to) {
        assert from >= 0 : from;
        assert to >= 0 : to;
        assert from <= Long.SIZE;
        assert to <= Long.SIZE;
        assert from <= to : from + SPACE + to;
        if (from == to) {
            return 0L;
        }
        long result = 1L << (Long.SIZE - 1);
        result >>= to - from - 1;
        result >>>= Long.SIZE - to;
        return result;
    }
    
    private static int ceilLogInt(int value) {
        assert value >= 1 : value;
        return Integer.SIZE - Integer.numberOfLeadingZeros(value - 1);
    }

    static OperatorType getType(long entry) {
        entry &= TYPE_MASK;
        return OperatorType.values()[(int) entry];
    }
    
    static int getParameter(long entry) {
        entry &= LEFT_OPERAND_MASK;
        entry >>>= LEFT_OPERAND_BEGIN;
        return (int) entry;
    }

    static int getNumerator(long entry) {
        entry &= LEFT_OPERAND_MASK;
        entry >>>= LEFT_OPERAND_BEGIN;
        return (int) entry;
    }

    static int getDenominator(long entry) {
        entry &= RIGHT_OPERAND_MASK;
        entry >>>= RIGHT_OPERAND_BEGIN;
        return (int) entry;
    }

    static int getOperand(long entry) {
        entry &= LEFT_OPERAND_MASK;
        entry >>>= LEFT_OPERAND_BEGIN;
        return (int) entry;
    }

    static int getOperandLeft(long entry) {
        entry &= LEFT_OPERAND_MASK;
        entry >>>= LEFT_OPERAND_BEGIN;
        return (int) entry;
    }
    
    static int getOperandRight(long entry) {
        entry &= RIGHT_OPERAND_MASK;
        entry >>>= RIGHT_OPERAND_BEGIN;
        return (int) entry;
    }
    
    static long makeEntry(OperatorType type, int operandLeft, int operandRight) {
        if (type == OperatorType.ADD || type == OperatorType.MULTIPLY) {
            if (operandRight > operandLeft) {
                int tmp = operandLeft;
                operandLeft = operandRight;
                operandRight = tmp;
            }
        }
        long result = (long) type.ordinal();
        result |= ((long) operandLeft) << LEFT_OPERAND_BEGIN;
        result |= ((long) operandRight) << RIGHT_OPERAND_BEGIN;
        return result;        
    }
    
    private EntryUtil() {
    }
}
