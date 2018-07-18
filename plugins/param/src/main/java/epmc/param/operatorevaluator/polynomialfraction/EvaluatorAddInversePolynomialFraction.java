package epmc.param.operatorevaluator.polynomialfraction;

import epmc.operator.Operator;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorSet;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddInversePolynomialFraction implements OperatorEvaluator {
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
                if (!TypePolynomialFraction.is(type)) {
                    return null;
                }                
            }
            return new EvaluatorAddInversePolynomialFraction(this);
        }
    }

    private final TypePolynomialFraction typePolynomialFraction;
    private final OperatorEvaluator addInversePolynomial;
    private final OperatorEvaluator setPolynomial;

    private EvaluatorAddInversePolynomialFraction(Builder builder) {
        assert builder != null;
        typePolynomialFraction = TypePolynomialFraction.as(builder.types[0]);
        TypePolynomial typePolynomial = typePolynomialFraction.getTypePolynomial();
        addInversePolynomial = ContextValue.get().getEvaluator(OperatorAddInverse.ADD_INVERSE, typePolynomial);
        setPolynomial = ContextValue.get().getEvaluator(OperatorSet.SET,
                typePolynomial, typePolynomial);
    }

    @Override
    public Type resultType() {
        return typePolynomialFraction;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomialFraction resultFraction = ValuePolynomialFraction.as(result);
        ValuePolynomialFraction poly = ValuePolynomialFraction.as(operands[0]);
        addInversePolynomial.apply(resultFraction.getNumerator(), poly.getNumerator());
        setPolynomial.apply(resultFraction.getDenominator(), poly.getDenominator());
        resultFraction.normalise();
        assert !resultFraction.getDenominator().toString().equals("0");
    }
}
