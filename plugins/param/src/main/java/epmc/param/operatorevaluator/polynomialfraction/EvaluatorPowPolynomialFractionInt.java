package epmc.param.operatorevaluator.polynomialfraction;

import epmc.operator.Operator;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorSet;
import epmc.param.value.polynomialfraction.TypePolynomialFraction;
import epmc.param.value.polynomialfraction.ValuePolynomialFraction;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorPowPolynomialFractionInt implements OperatorEvaluator {
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
            if (operator != OperatorPow.POW) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypePolynomialFraction.is(types[0])) {
                return null;
            }
            if (!TypeInteger.is(types[1])) {
                return null;
            }
            return new EvaluatorPowPolynomialFractionInt(this);
        }
        
    }

    private final OperatorEvaluator multiply;
    private final OperatorEvaluator set;
    private final ValuePolynomialFraction resultPolynomial;
    private final TypePolynomialFraction typePolynomialFraction;

    private EvaluatorPowPolynomialFractionInt(Builder builder) {
        assert builder != null;
        TypePolynomialFraction typeFraction = null;
        if (TypePolynomialFraction.is(builder.types[0])) {
            typeFraction = TypePolynomialFraction.as(builder.types[0]);
        } else if (TypePolynomialFraction.is(builder.types[1])) {
            typeFraction = TypePolynomialFraction.as(builder.types[1]);
        } else {
            assert false;
        }
        typePolynomialFraction = typeFraction;
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY,
                typeFraction, typeFraction);
        set = ContextValue.get().getEvaluator(OperatorSet.SET,
                typeFraction, typeFraction);
        resultPolynomial = typePolynomialFraction.newValue();
    }

    @Override
    public Type resultType() {
        return typePolynomialFraction;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomialFraction operand1 = ValuePolynomialFraction.as(operands[0]);
        ValueInteger operand2 = ValueInteger.as(operands[1]);
        int op2Int = operand2.getInt();
        assert op2Int >= 0 : op2Int; // TODO could support negative values
        resultPolynomial.set(1);
        for (int i = 0; i < op2Int; i++) {
            multiply.apply(resultPolynomial, resultPolynomial, operand1);
        }
        set.apply(result, resultPolynomial);
    }
}
