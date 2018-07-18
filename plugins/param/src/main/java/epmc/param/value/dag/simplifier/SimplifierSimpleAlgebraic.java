package epmc.param.value.dag.simplifier;

import java.math.BigInteger;

import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;

public final class SimplifierSimpleAlgebraic implements Simplifier {
    public static class Builder implements Simplifier.Builder {
        private Dag dag;

        @Override
        public Builder setDag(Dag dag) {
            this.dag = dag;
            return this;
        }

        @Override
        public Simplifier build() {
            return new SimplifierSimpleAlgebraic(this);
        }
        
    }

    private final Dag dag;
    private OperatorType type;
    private int operandLeft;
    private int operandRight;
    private OperatorType resultType;
    private int resultLeftOperand;
    private int resultRightOperand;

    private SimplifierSimpleAlgebraic(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        this.dag = builder.dag;
    }

    @Override
    public void setType(OperatorType type) {
        this.type = type;
    }

    @Override
    public void setOperandLeft(int operandLeft) {
        this.operandLeft = operandLeft;
    }

    @Override
    public void setOperandRight(int operandRight) {
        this.operandRight = operandRight;
    }
    
    @Override
    public boolean simplify() {
        switch (type) {
        case ADD_INVERSE:
            return simplifyAddInverse();
        case MULTIPLY_INVERSE:
            return simplifyMultiplyInverse();
        case ADD:
            return simplifyAdd();
        case MULTIPLY:
            return simplifyMultiply();
        default:
            return false;
        }
    }

    private boolean simplifyAddInverse() {
        OperatorType opType = dag.getOperatorType(operandLeft);
        if (opType != OperatorType.ADD_INVERSE) {
            return false;
        }
        int innerOperand = dag.getOperand(operandLeft);
        setResultToOperand(innerOperand);
        return true;
    }

    private boolean simplifyMultiplyInverse() {
        OperatorType opType = dag.getOperatorType(operandLeft);
        if (opType != OperatorType.MULTIPLY_INVERSE) {
            return false;
        }
        int innerOperand = dag.getOperand(operandLeft);
        setResultToOperand(innerOperand);
        return true;
    }
    
    private boolean simplifyAdd() {
        OperatorType opTypeLeft = dag.getOperatorType(operandLeft);
        OperatorType opTypeRight = dag.getOperatorType(operandRight);
        if (opTypeLeft == OperatorType.NUMBER
                && dag.getNumberNumerator(operandLeft).equals(BigInteger.ZERO)) {
            setResultToOperand(operandRight);
            return true;
        } else if (opTypeRight == OperatorType.NUMBER
                && dag.getNumberNumerator(operandRight).equals(BigInteger.ZERO)) {
            setResultToOperand(operandLeft);
            return true;
        } else {
            return false;
        }
    }

    private boolean simplifyMultiply() {
        OperatorType opTypeLeft = dag.getOperatorType(operandLeft);
        OperatorType opTypeRight = dag.getOperatorType(operandRight);
        if (opTypeLeft == OperatorType.NUMBER
                && dag.getNumberNumerator(operandLeft).equals(BigInteger.ONE)
                && dag.getNumberDenominator(operandLeft).equals(BigInteger.ONE)) {
            setResultToOperand(operandRight);
            return true;
        } else if (opTypeRight == OperatorType.NUMBER
                && dag.getNumberNumerator(operandRight).equals(BigInteger.ONE)
                && dag.getNumberDenominator(operandRight).equals(BigInteger.ONE)) {
            setResultToOperand(operandLeft);
            return true;
        } else if (opTypeLeft == OperatorType.NUMBER
                && dag.getNumberNumerator(operandLeft).equals(BigInteger.ZERO)) {
            setResultToZero();
            return true;
        } else if (opTypeRight == OperatorType.NUMBER
                && dag.getNumberNumerator(operandRight).equals(BigInteger.ZERO)) {
            setResultToZero();
            return true;
        } else {
            return false;
        }
    }

    private void setResultToOperand(int resultNumber) {
        resultType = dag.getOperatorType(resultNumber);
        switch (resultType) {
        case ADD_INVERSE:
            resultLeftOperand = dag.getOperand(resultNumber);
            resultRightOperand = 0;
            break;
        case MULTIPLY_INVERSE:
            resultLeftOperand = dag.getOperand(resultNumber);
            resultRightOperand = 0;
            break;
        case ADD:
            resultLeftOperand = dag.getOperandLeft(resultNumber);
            resultRightOperand = dag.getOperandRight(resultNumber);
            break;
        case MULTIPLY:
            resultLeftOperand = dag.getOperandLeft(resultNumber);
            resultRightOperand = dag.getOperandRight(resultNumber);
            break;
        case NUMBER:
            resultLeftOperand = dag.getNumberOfValue(dag.getNumberNumerator(resultNumber));
            resultRightOperand = dag.getNumberOfValue(dag.getNumberDenominator(resultNumber));
            break;
        case PARAMETER:
            resultLeftOperand = dag.getParameterNumber(resultNumber);
            resultRightOperand = 0;
            break;
        default:
            break;
        }
    }

    private void setResultToZero() {
        resultType = OperatorType.NUMBER;
        resultLeftOperand = dag.getNumberOfValue(BigInteger.ZERO);
        resultRightOperand = dag.getNumberOfValue(BigInteger.ONE);
    }

    @Override
    public OperatorType getResultType() {
        return resultType;
    }

    @Override
    public int getResultOperandLeft() {
        return resultLeftOperand;
    }

    @Override
    public int getResultOperandRight() {
        return resultRightOperand;
    }
}
