package epmc.param.operatorevaluator.gmp;

import epmc.operator.Operator;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorMultiplyInverse;
import epmc.param.value.gmp.GMP;
import epmc.param.value.gmp.TypeMPQ;
import epmc.param.value.gmp.ValueMPQ;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddInverseMultiplyInverseMPQ implements OperatorEvaluator {
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
            assert operator != null;
            assert types != null;
            for (Type type : types) {
                assert type != null;
            }
            if (operator != OperatorAddInverse.ADD_INVERSE
                    && operator != OperatorMultiplyInverse.MULTIPLY_INVERSE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeMPQ.is(types[0])) {
                return null;
            }
            return new EvaluatorAddInverseMultiplyInverseMPQ(this);
        }
        
    }

    private final TypeMPQ resultType;
    private final Operator operator;
    
    private EvaluatorAddInverseMultiplyInverseMPQ(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        resultType = TypeMPQ.as(builder.types[0]);
        operator = builder.operator;
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        for (Value operand : operands) {
            assert operand != null;
        }
        ValueMPQ valueMPQ = ValueMPQ.as(operands[0]);
        ValueMPQ resultMPQ = ValueMPQ.as(result);
        if (operator == OperatorAddInverse.ADD_INVERSE) {
            GMP.__gmpq_neg(resultMPQ.getContent(), valueMPQ.getContent());
        } else {
            assert operator == OperatorMultiplyInverse.MULTIPLY_INVERSE;
            GMP.__gmpq_inv(resultMPQ.getContent(), valueMPQ.getContent());
        }
    }
}
