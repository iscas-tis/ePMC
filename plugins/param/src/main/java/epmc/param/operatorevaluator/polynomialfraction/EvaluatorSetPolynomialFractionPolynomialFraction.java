package epmc.param.operatorevaluator.polynomialfraction;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorSetPolynomialFractionPolynomialFraction implements OperatorEvaluator {
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
                if (!TypePolynomialFraction.is(type)) {
                    return null;
                }                
            }
            return new EvaluatorSetPolynomialFractionPolynomialFraction(this);
        }
    }

    private final TypePolynomialFraction typeFunctionPolynomialFraction;
    private final OperatorEvaluator setPolynomial;    
    
    private EvaluatorSetPolynomialFractionPolynomialFraction(Builder builder) {
        assert builder != null;
        TypePolynomial typePolynomial = TypePolynomialFraction.as(builder.types[0]).getTypePolynomial();
        setPolynomial = ContextValue.get().getEvaluator(OperatorSet.SET,typePolynomial, typePolynomial);
        typeFunctionPolynomialFraction = TypePolynomialFraction.as(builder.types[0]);
    }

    @Override
    public Type resultType() {
        return typeFunctionPolynomialFraction;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomialFraction resultFunctionPolynomial = ValuePolynomialFraction.as(result);
        ValuePolynomialFraction operand = ValuePolynomialFraction.as(operands[0]);
        resultFunctionPolynomial.adjustNumParameters();
        operand.adjustNumParameters();
        setPolynomial.apply(resultFunctionPolynomial.getNumerator(), operand.getNumerator());
        setPolynomial.apply(resultFunctionPolynomial.getDenominator(), operand.getDenominator());
    }
}
