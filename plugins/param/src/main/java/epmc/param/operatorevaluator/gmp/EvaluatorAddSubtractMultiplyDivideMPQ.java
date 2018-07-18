package epmc.param.operatorevaluator.gmp;

import epmc.operator.Operator;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSubtract;
import epmc.param.value.gmp.GMP;
import epmc.param.value.gmp.TypeMPQ;
import epmc.param.value.gmp.ValueMPQ;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorAddSubtractMultiplyDivideMPQ implements OperatorEvaluator {
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
            if (operator != OperatorAdd.ADD
                    && operator != OperatorSubtract.SUBTRACT
                    && operator != OperatorMultiply.MULTIPLY
                    && operator != OperatorDivide.DIVIDE) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeMPQ.is(types[0])) {
                return null;
            }
            if (!TypeMPQ.is(types[1])) {
                return null;
            }
            return new EvaluatorAddSubtractMultiplyDivideMPQ(this);
        }
        
    }

    private final TypeMPQ resultType;
    private final Operator operator;
    
    private EvaluatorAddSubtractMultiplyDivideMPQ(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        operator = builder.operator;
        resultType = TypeMPQ.as(builder.types[0]);
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
        ValueMPQ op1 = ValueMPQ.as(operands[0]);
        ValueMPQ op2 = ValueMPQ.as(operands[1]);
        ValueMPQ resultMPQ = ValueMPQ.as(result);
        if (operator == OperatorAdd.ADD) {
            GMP.__gmpq_add(resultMPQ.getContent(), op1.getContent(), op2.getContent());
        } else if (operator == OperatorSubtract.SUBTRACT) {
            GMP.__gmpq_sub(resultMPQ.getContent(), op1.getContent(), op2.getContent());
        } else if (operator == OperatorMultiply.MULTIPLY) {
            GMP.__gmpq_mul(resultMPQ.getContent(), op1.getContent(), op2.getContent());
        } else {
            assert operator == OperatorDivide.DIVIDE;
            GMP.__gmpq_div(resultMPQ.getContent(), op1.getContent(), op2.getContent());
        }
    }
}
