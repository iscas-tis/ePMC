package epmc.param.value.polynomial.evaluator;

import java.util.ArrayList;

import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorSet;
import epmc.param.value.FunctionEvaluator;
import epmc.param.value.ValueFunction;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.param.value.rational.TypeRational;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInterval;
import epmc.value.ValueSetString;

public final class EvaluatorPolynomialSimple implements FunctionEvaluator {
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
        public EvaluatorPolynomialSimple build() {
            for (ValueFunction function : functions) {
                assert function != null;
            }
            assert resultType != null;
            for (ValueFunction function : functions) {
                if (!ValuePolynomial.is(function)) {
                    return null;
                }
            }
            if (functions.size() == 0) {
                return null;
            }
            return new EvaluatorPolynomialSimple(this);
        }
    }

    private final TypeAlgebra resultType;
    private final OperatorEvaluator powReal;
    private final OperatorEvaluator multiplyReal;
    private final OperatorEvaluator addReal;
    private final OperatorEvaluator setPointValue;
    private final ValueAlgebra currentParamPow;
    private final ValueAlgebra termValue;
    private final ValueAlgebra exponent;
    private final ValueAlgebra result;
    private final ValueInterval paramValueRat;
    private final ValueAlgebra paramValueRes;

    private final int numParameters;
    private final int numTerms[];
    private final ValueArrayAlgebra coefficients[];
    private final int[][] exponents;
    private final int numFunctions;

    private EvaluatorPolynomialSimple(Builder builder) {
        assert builder != null;
        resultType = builder.resultType;
        TypeInteger typeInteger = TypeInteger.get();
        TypeInterval typeInterval = ContextValue.get().makeUnique(new TypeInterval(TypeRational.get()));
        exponent = typeInteger.newValue();
        result = resultType.newValue();
        powReal = ContextValue.get().getEvaluator(OperatorPow.POW,
                resultType, typeInteger);
        multiplyReal = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY,
                resultType, resultType);
        addReal = ContextValue.get()
                .getEvaluator(OperatorAdd.ADD, resultType, resultType);
        currentParamPow = resultType.newValue();
        termValue = resultType.newValue();
        numParameters = builder.functions.get(0).getType().getParameterSet().getNumParameters();
        numFunctions = builder.functions.size();
        numTerms = new int[builder.functions.size()];
        ValueAlgebra coefficient = resultType.newValue();
        exponents = new int[builder.functions.size()][];
        coefficients = new ValueArrayAlgebra[builder.functions.size()];
        paramValueRat = typeInterval.newValue();
        paramValueRes = resultType.newValue();
        if (TypeInterval.is(resultType)) {
            setPointValue = ContextValue.get().getEvaluator(OperatorSet.SET, typeInterval, resultType);
        } else {
            setPointValue = ContextValue.get().getEvaluator(OperatorSet.SET, typeInterval.getEntryType(), resultType);
        }
        for (int functionNr = 0; functionNr < numFunctions; functionNr++) {
            ValuePolynomial function = ValuePolynomial.as(builder.functions.get(functionNr));
            numTerms[functionNr] = function.getNumTerms();
            coefficients[functionNr] = resultType.getTypeArray().newValue();
            coefficients[functionNr].setSize(numTerms[functionNr]);
            exponents[functionNr] = new int[numTerms[functionNr] * numParameters];
            for (int termNr = 0; termNr < numTerms[functionNr]; termNr++) {
                ValueSetString.as(coefficient).set(function.getCoefficient(termNr).toString());
                coefficients[functionNr].set(coefficient, termNr);
                for (int paramNr = 0; paramNr < numParameters; paramNr++) {
                    exponents[functionNr][numParameters * termNr + paramNr] = 
                            function.getExponent(paramNr, termNr);
                }
            }
        }
    }

    @Override
    public TypeAlgebra getResultType() {
        return resultType;
    }

    @Override
    public int getResultDimensions() {
        return numFunctions;
    }

    @Override
    public void evaluate(ValueArrayAlgebra results, ValueArrayAlgebra point) {
        for (int functionNr = 0; functionNr < numFunctions; functionNr++) {
            result.set(0);
            for (int termNr = 0; termNr < numTerms[functionNr]; termNr++) {
                coefficients[functionNr].get(termValue, termNr);
                for (int paramNr = 0; paramNr < numParameters; paramNr++) {
                    point.get(paramValueRat, paramNr);
                    setValueFromPointValue(paramValueRes, paramValueRat);
                    exponent.set(exponents[functionNr][numParameters * termNr + paramNr]);
                    powReal.apply(currentParamPow, paramValueRes, exponent);
                    multiplyReal.apply(termValue, termValue, currentParamPow);
                }
                addReal.apply(result, result, termValue);
            }
            results.set(result, functionNr);
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
}
