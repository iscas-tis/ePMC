package epmc.param.operatorevaluator.polynomialfraction;

import epmc.operator.Operator;
import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorIsZero;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorIsZeroPolynomialFraction implements OperatorEvaluator {
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
            if (operator != OperatorIsZero.IS_ZERO) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypePolynomialFraction.is(types[0])) {
                return null;
            }
            return new EvaluatorIsZeroPolynomialFraction(this);
        }
        
    }

    private final TypeBoolean typeBoolean;
    private final TypePolynomial typePolynomial;
    private final OperatorEvaluator isZeroPolynomial;
    private final OperatorEvaluator isOnePolynomial;
    private final ValueBoolean cmp;
    
    private EvaluatorIsZeroPolynomialFraction(Builder builder) {
        assert builder != null;
        typeBoolean = TypeBoolean.get();
        typePolynomial = TypePolynomialFraction.as(builder.types[0]).getTypePolynomial();
        isZeroPolynomial = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, typePolynomial);
        isOnePolynomial = ContextValue.get().getEvaluator(OperatorIsOne.IS_ONE, typePolynomial);
        cmp = typeBoolean.newValue();
    }

    @Override
    public Type resultType() {
        return typeBoolean;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValueBoolean resultBoolean = ValueBoolean.as(result);
        ValuePolynomialFraction operand = ValuePolynomialFraction.as(operands[0]);
        isZeroPolynomial.apply(cmp, operand.getNumerator());
        if (!cmp.getBoolean()) {
            resultBoolean.set(false);
            return;
        }
        isOnePolynomial.apply(cmp, operand.getDenominator());
        if (!cmp.getBoolean()) {
            resultBoolean.set(false);
            return;
        }
        resultBoolean.set(true);
    }

}
