package epmc.param.operatorevaluator.polynomial;

import epmc.operator.Operator;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorPow;
import epmc.operator.OperatorSet;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.ValueInteger;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorPowPolynomialInt implements OperatorEvaluator {
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
            if (!TypePolynomial.is(types[0])) {
                return null;
            }
            if (!TypeInteger.is(types[1])) {
                return null;
            }
            return new EvaluatorPowPolynomialInt(this);
        }
        
    }

    private final TypePolynomial typePolynomial;
    private final OperatorEvaluator multiply;
    private final OperatorEvaluator set;
    private final ValuePolynomial resultPolynomial;

    private EvaluatorPowPolynomialInt(Builder builder) {
        assert builder != null;
        TypePolynomial typePolynomial = null;
        if (TypePolynomial.is(builder.types[0])) {
            typePolynomial = TypePolynomial.as(builder.types[0]);
        } else if (TypePolynomial.is(builder.types[1])) {
            typePolynomial = TypePolynomial.as(builder.types[1]);
        } else {
            assert false;
        }
        multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY,
                typePolynomial, typePolynomial);
        set = ContextValue.get().getEvaluator(OperatorSet.SET,
                typePolynomial, typePolynomial);
        this.typePolynomial = typePolynomial;
        resultPolynomial = typePolynomial.newValue();
    }

    @Override
    public Type resultType() {
        return typePolynomial;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValuePolynomial operand1 = ValuePolynomial.as(operands[0]);
        ValueInteger operand2 = ValueInteger.as(operands[1]);
        int op2Int = operand2.getInt();
        assert op2Int >= 0 : op2Int; // TODO
        resultPolynomial.set(1);
        for (int i = 0; i < op2Int; i++) {
            multiply.apply(resultPolynomial, resultPolynomial, operand1);
        }
        set.apply(result, resultPolynomial);
    }
}
