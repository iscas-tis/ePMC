package epmc.param.operatorevaluator.polynomial;

import java.math.BigInteger;

import epmc.operator.Operator;
import epmc.operator.OperatorIsOne;
import epmc.param.value.polynomial.TypePolynomial;
import epmc.param.value.polynomial.ValuePolynomial;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorIsOnePolynomial implements OperatorEvaluator {
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
            if (operator != OperatorIsOne.IS_ONE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypePolynomial.is(types[0])) {
                return null;
            }
            return new EvaluatorIsOnePolynomial(this);
        }
        
    }

    private TypeBoolean typeBoolean;
    
    private EvaluatorIsOnePolynomial(Builder builder) {
        assert builder != null;
        typeBoolean = TypeBoolean.get();
    }

    @Override
    public Type resultType() {
        return typeBoolean;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValueBoolean resultBoolean = ValueBoolean.as(result);
        ValuePolynomial operand = ValuePolynomial.as(operands[0]);
        
        if (operand.getNumTerms() != 1) {
            resultBoolean.set(false);
            return;
        }
        if (!operand.getCoefficients()[0].equals(BigInteger.ONE)) {
            resultBoolean.set(false);
            return;
        }
        int numParameters = operand.getNumParameters();
        for (int paramNr = 0; paramNr < numParameters; paramNr++) {
            if (operand.getMonomials()[paramNr] != 0) {
                resultBoolean.set(false);
                return;
            }
        }
        resultBoolean.set(true);
    }

}
