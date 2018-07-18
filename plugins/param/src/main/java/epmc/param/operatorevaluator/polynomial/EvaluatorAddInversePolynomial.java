package epmc.param.operatorevaluator.polynomial;

import java.math.BigInteger;

import epmc.operator.Operator;
import epmc.operator.OperatorAddInverse;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddInversePolynomial implements OperatorEvaluator {
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
        public OperatorEvaluator build() {
            assert types != null;
            assert operator != null;
            if (operator != OperatorAddInverse.ADD_INVERSE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            for (Type type : types) {
                if (!TypePolynomial.is(type)) {
                    return null;
                }                
            }
            return new EvaluatorAddInversePolynomial(this);
        }
    }

    private TypePolynomial typeFunctionPolynomial;
    
    private EvaluatorAddInversePolynomial(Builder builder) {
        assert builder != null;
        typeFunctionPolynomial = TypePolynomial.as(builder.types[0]);
    }

    @Override
    public Type resultType() {
        return typeFunctionPolynomial;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomial resultFunctionPolynomial = ValuePolynomial.as(result);
        ValuePolynomial operand = ValuePolynomial.as(operands[0]);
        resultFunctionPolynomial.adjustNumParameters();
        ValuePolynomial function = ValuePolynomial.as(operand);
        resultFunctionPolynomial.setNumTerms(function.getNumTerms());
        resultFunctionPolynomial.setMonomials(function.getMonomials().clone());
        BigInteger[] resultCoefficients = new BigInteger[function.getNumTerms()];
        for (int coeffNr = 0; coeffNr < function.getNumTerms(); coeffNr++) {
            resultCoefficients[coeffNr] = function.getCoefficients()[coeffNr].negate();
        }
        resultFunctionPolynomial.setCoefficients(resultCoefficients);
    }
}
