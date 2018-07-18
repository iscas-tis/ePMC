package epmc.param.operatorevaluator.polynomial;

import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorPow;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInteger;
import epmc.value.ValueReal;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorEvaluatePolynomialReal implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            this.types = types;
        }

        @Override
        public EvaluatorEvaluatePolynomialReal build() {
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            if (!(operator instanceof ValuePolynomial)) {
                return null;
            }
            ValuePolynomial operator = (ValuePolynomial) this.operator;
            int numParameters = operator.getType().getParameterSet().getNumParameters();
            if (types.length != numParameters) {
                return null;
            }
            for (Type type : types) {
                if (!TypeReal.is(type)) {
                    return null;
                }
            }
            return new EvaluatorEvaluatePolynomialReal(this);
        }
        
    }
    
    private final OperatorEvaluator powReal;
    private final OperatorEvaluator multiplyReal;
    private final OperatorEvaluator addReal;
    private final ValueInteger exponent = TypeInteger.get().newValue();
    private final ValueReal currentParamPow;
    private final ValueReal termValue;
    private final TypeReal resultType;
    private final int numTerms;
    private final int numParameters;
    private final ValueArrayAlgebra coefficients;
    private final int[] exponents;
    
    private EvaluatorEvaluatePolynomialReal(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.operator instanceof ValuePolynomial;
        ValuePolynomial operator = (ValuePolynomial) builder.operator;
        operator.adjustNumParameters();
        resultType = TypeReal.as(UtilValue.upper(builder.types));
        powReal = ContextValue.get().getEvaluator(OperatorPow.POW,
                resultType, resultType);
        multiplyReal = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY,
                resultType, resultType);
        addReal = ContextValue.get()
                .getEvaluator(OperatorAdd.ADD, resultType, resultType);
        currentParamPow = resultType.newValue();
        termValue = resultType.newValue();
        numTerms = operator.getNumTerms();
        numParameters = operator.getNumParameters();
        coefficients = resultType.getTypeArray().newValue();
        coefficients.setSize(numTerms);
        ValueReal coefficient = resultType.newValue();
        exponents = new int[numTerms * numParameters];
        for (int termNr = 0; termNr < numTerms; termNr++) {
            coefficient.set(operator.getCoefficient(termNr).toString());
            coefficients.set(coefficient, termNr);
            for (int paramNr = 0; paramNr < numParameters; paramNr++) {
                exponents[numParameters * termNr + paramNr] = 
                        operator.getExponent(paramNr, termNr);
            }
        }
    }

    @Override
    public Type resultType() {
        return resultType();
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;

        ValueReal paramValue;
        ValueReal.as(result).set(0);
        for (int termNr = 0; termNr < numTerms; termNr++) {
            coefficients.get(termValue, termNr);
            for (int paramNr = 0; paramNr < numParameters; paramNr++) {
                paramValue = ValueReal.as(operands[paramNr]);
                exponent.set(exponents[numParameters * termNr + paramNr]);
                powReal.apply(currentParamPow, paramValue, exponent);
                multiplyReal.apply(termValue, termValue, currentParamPow);
            }
            addReal.apply(result, result, termValue);
        }
    }
}
