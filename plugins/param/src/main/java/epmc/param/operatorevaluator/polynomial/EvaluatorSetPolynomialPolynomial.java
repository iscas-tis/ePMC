package epmc.param.operatorevaluator.polynomial;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorSetPolynomialPolynomial implements OperatorEvaluator {
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
            if (operator != OperatorSet.SET) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            for (Type type : types) {
                if (!TypePolynomial.is(type)) {
                    return null;
                }                
            }
            return new EvaluatorSetPolynomialPolynomial(this);
        }
    }

    private TypePolynomial typeFunctionPolynomial;
    
    private EvaluatorSetPolynomialPolynomial(Builder builder) {
        assert builder != null;
        TypePolynomial typePolynomial = null;
        if (TypePolynomial.is(builder.types[0])) {
            typePolynomial = TypePolynomial.as(builder.types[0]);
        } else if (TypePolynomial.is(builder.types[1])) {
            typePolynomial = TypePolynomial.as(builder.types[1]);
        } else {
            assert false;
        }
        typeFunctionPolynomial = typePolynomial;
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
        operand.adjustNumParameters();
        resultFunctionPolynomial.setNumTerms(operand.getNumTerms());
        resultFunctionPolynomial.setMonomials(operand.getMonomials().clone());
        resultFunctionPolynomial.setCoefficients(operand.getCoefficients().clone());
    }
}
