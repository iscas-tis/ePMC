package epmc.param.value.dag.simplifier;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;

import epmc.options.UtilOptions;
import epmc.param.options.OptionsParam;
import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

public final class EvaluatorDouble implements Evaluator {
    public final static String IDENTIFIER = "double";
    
    public final static class Builder implements Evaluator.Builder {

        private Dag dag;

        @Override
        public Evaluator.Builder setDag(Dag dag) {
            this.dag = dag;
            return this;
        }

        @Override
        public Evaluator build() {
            return new EvaluatorDouble(this);
        }
        
    }
    
    private final DoubleArrayList randomNumbersDouble = new DoubleArrayList();
    private final DoubleStore store;
    private final DoubleLookup lookup;
    private double lastValueDouble;
    
    private final Dag dag;
    private final Random random = new Random();

    private EvaluatorDouble(Builder builder) {
        assert builder != null;
        assert builder.dag != null;
        this.dag = builder.dag;
        DoubleStore.Builder storeBuilder = UtilOptions.getInstance(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_STORAGE);
        store = storeBuilder.build();
        DoubleLookup.Builder lookupBuilder = UtilOptions.getInstance(OptionsParam.PARAM_DAG_PROB_SIMPLIFIER_DOUBLE_PROB_LOOKUP);
        lookup = lookupBuilder.setStore(store).build();
    }

    @Override
    public int evaluate(OperatorType type, int operandLeft, int operandRight) {
        int resultNode;
        double value = evaluateDouble(type, operandLeft, operandRight);
        lastValueDouble = value;
        assert !Double.isNaN(value);
        resultNode = lookupEntry(value);
        return resultNode;
    }
    
    private double evaluateDouble(OperatorType type, int operandLeft, int operandRight) {
        adjustNumParameters();
        switch (type) {
        case NUMBER:
            return evaluateDoubleNumber(operandLeft, operandRight);
        case PARAMETER:
            return evaluateDoubleParameter(operandLeft, operandRight);
        case ADD_INVERSE:
            return evaluateDoubleAddInverse(operandLeft, operandRight);
        case MULTIPLY_INVERSE:
            return evaluateDoubleMultiplyInverse(operandLeft, operandRight);       
        case ADD:
            return evaluateDoubleAdd(operandLeft, operandRight);
        case MULTIPLY:
            return evaluateDoubleMultiply(operandLeft, operandRight);
        default:
            assert false;
            return Double.NaN;
        }
    }

    private double evaluateDoubleNumber(int operandLeft, int operandRight) {
        if (operandRight == 0) {
            if (operandLeft > 0) {
                return Double.POSITIVE_INFINITY;
            } else if (operandLeft < 0) {
                return Double.NEGATIVE_INFINITY;
            } else {
                System.out.println("HHHHH");
                assert false;
                return Double.NaN;
            }
        }
        BigDecimal numDecimal = new BigDecimal(dag.getValueFromNumber(operandLeft));
        BigDecimal denDecimal = new BigDecimal(dag.getValueFromNumber(operandRight));
        BigDecimal resDecimal = numDecimal.divide(denDecimal, MathContext.DECIMAL128);
        double val = resDecimal.doubleValue();
        assert !Double.isNaN(val) : "ASS " + val + " " + dag.getValueFromNumber(operandLeft) + " " + dag.getValueFromNumber(operandRight);
        return val;
    }

    private double evaluateDoubleParameter(int operandLeft, int operandRight) {
        return randomNumbersDouble.getDouble(operandLeft);
    }

    private double evaluateDoubleAddInverse(int operandLeft, int operandRight) {
        return -getEntry(operandLeft);
    }

    private double evaluateDoubleMultiplyInverse(int operandLeft, int operandRight) {
        return 1.0 / getEntry(operandLeft);
    }

    private double evaluateDoubleAdd(int operandLeft, int operandRight) {
        return getEntry(operandLeft) + getEntry(operandRight);
    }

    private double evaluateDoubleMultiply(int operandLeft, int operandRight) {
        return getEntry(operandLeft) * getEntry(operandRight);
    }
    
    private void adjustNumParameters() {
        while (randomNumbersDouble.size() < dag.getParameters().getNumParameters()) {
            randomNumbersDouble.add(random.nextDouble());
        }
    }

    @Override
    public void commitResult() {
        int number = getNumEntries();
        addEntry(lastValueDouble);
        putLookup(lastValueDouble, number);
    }
    
    private void putLookup(double value, int number) {
        lookup.put(value, number);
    }

    private int lookupEntry(double value) {
        return lookup.get(value);
    }
    
    private void addEntry(double entry) {
        store.add(entry);
    }
    
    private int getNumEntries() {
        return store.size();
    }
    
    private double getEntry(int number) {
        return store.get(number);
    }
}
