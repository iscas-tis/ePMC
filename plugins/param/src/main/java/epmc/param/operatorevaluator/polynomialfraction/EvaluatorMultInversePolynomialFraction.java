package epmc.param.operatorevaluator.polynomialfraction;

import epmc.operator.Operator;
import epmc.operator.OperatorMultiplyInverse;
import epmc.operator.OperatorSet;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorMultInversePolynomialFraction implements OperatorEvaluator {
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
            if (operator != OperatorMultiplyInverse.MULTIPLY_INVERSE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            for (Type type : types) {
                if (!TypePolynomialFraction.is(type)) {
                    return null;
                }                
            }
            return new EvaluatorMultInversePolynomialFraction(this);
        }
    }

    private TypePolynomialFraction typePolynomial;
    private final OperatorEvaluator setPolynomial;
    
    private EvaluatorMultInversePolynomialFraction(Builder builder) {
        assert builder != null;
        typePolynomial = TypePolynomialFraction.as(builder.types[0]);
        setPolynomial = ContextValue.get().getEvaluator(OperatorSet.SET,
                typePolynomial, typePolynomial);
    }

    @Override
    public Type resultType() {
        return typePolynomial;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomialFraction resultFraction = ValuePolynomialFraction.as(result);
        ValuePolynomialFraction poly = ValuePolynomialFraction.as(operands[0]);
        setPolynomial.apply(resultFraction.getNumerator(), poly.getDenominator());
        setPolynomial.apply(resultFraction.getDenominator(), poly.getNumerator());
        resultFraction.normalise();
        assert !resultFraction.getDenominator().toString().equals("0");
    }
}
