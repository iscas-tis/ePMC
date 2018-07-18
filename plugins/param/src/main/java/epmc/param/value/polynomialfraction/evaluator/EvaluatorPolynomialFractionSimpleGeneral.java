package epmc.param.value.polynomialfraction.evaluator;

import java.util.ArrayList;

import epmc.operator.OperatorDivide;
import epmc.param.value.FunctionEvaluator;
import epmc.param.value.ValueFunction;
import epmc.param.value.polynomial.evaluator.EvaluatorPolynomialSimple;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;

public final class EvaluatorPolynomialFractionSimpleGeneral implements FunctionEvaluator {
    public final static String IDENTIFIER = "general-fraction";
    
    public final static class Builder implements FunctionEvaluator.Builder {
        private final ArrayList<ValueFunction> functions = new ArrayList<>();
        private boolean useIntevals;
        private TypeAlgebra resultType;

        @Override
        public Builder addFunction(ValueFunction function) {
            assert function != null;
            functions.add(function);
            return this;
        }

        @Override
        public Builder setPointsUseIntervals(boolean useIntervals) {
            this.useIntevals = useIntervals;
            return this;
        }

        @Override
        public Builder setResultType(TypeAlgebra type) {
            this.resultType = type;
            return this;
        }

        @Override
        public FunctionEvaluator build() {
            for (ValueFunction function : functions) {
                assert function != null;
            }
            assert resultType != null;
            for (ValueFunction function : functions) {
                if (!ValuePolynomialFraction.is(function)) {
                    return null;
                }
            }
            return new EvaluatorPolynomialFractionSimpleGeneral(this);
        }
    }

    private final TypeAlgebra resultType;
    private final int numFunctions;
    private final EvaluatorPolynomialSimple evaluatorNum;
    private final EvaluatorPolynomialSimple evaluatorDen;
    private final ValueArrayAlgebra resultsNum;
    private final ValueArrayAlgebra resultsDen;
    private final ValueAlgebra resultNum;
    private final ValueAlgebra resultDen;
    private final ValueAlgebra result;
    private final OperatorEvaluator divide;

    private EvaluatorPolynomialFractionSimpleGeneral(Builder builder) {
        assert builder != null;
        resultType = builder.resultType;
        numFunctions = builder.functions.size();
        epmc.param.value.polynomial.evaluator.EvaluatorPolynomialSimple.Builder
            numBuilder = new EvaluatorPolynomialSimple.Builder()
            .setPointsUseIntervals(builder.useIntevals)
            .setResultType(builder.resultType);
        epmc.param.value.polynomial.evaluator.EvaluatorPolynomialSimple.Builder
            denBuilder = new EvaluatorPolynomialSimple.Builder()
            .setPointsUseIntervals(builder.useIntevals)
            .setResultType(builder.resultType);
        for (ValueFunction function : builder.functions) {
            ValuePolynomialFraction fraction = ValuePolynomialFraction.as(function);
            numBuilder.addFunction(fraction.getNumerator());
            denBuilder.addFunction(fraction.getDenominator());
        }
        evaluatorNum = numBuilder.build();
        evaluatorDen = denBuilder.build();
        resultsNum = resultType.getTypeArray().newValue();
        resultsNum.setSize(numFunctions);
        resultsDen = resultType.getTypeArray().newValue();
        resultsDen.setSize(numFunctions);
        resultNum = resultType.newValue();
        resultDen = resultType.newValue();
        result = resultType.newValue();
        divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE,
                resultType, resultType);
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
        assert results != null;
        assert point != null;
        evaluatorNum.evaluate(resultsNum, point);
        evaluatorDen.evaluate(resultsDen, point);
        for (int function = 0; function < numFunctions; function++) {
            resultsNum.get(resultNum, function);
            resultsDen.get(resultDen, function);
            divide.apply(result, resultNum, resultDen);
            results.set(result, function);
        }
    }
}
