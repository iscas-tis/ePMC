package epmc.param.operatorevaluator.gmp;

import epmc.operator.Operator;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLe;
import epmc.operator.OperatorLt;
import epmc.param.value.gmp.GMP;
import epmc.param.value.gmp.TypeMPQ;
import epmc.param.value.gmp.ValueMPQ;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorCmpMPQ implements OperatorEvaluator {
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
            if (operator != OperatorLt.LT
                    && operator != OperatorLe.LE
                    && operator != OperatorEq.EQ
                    && operator != OperatorGe.GE
                    && operator != OperatorGt.GT) {
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
            return new EvaluatorCmpMPQ(this);
        }
        
    }

    private final TypeBoolean resultType;
    private final Operator operator;
    
    private EvaluatorCmpMPQ(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        operator = builder.operator;
        resultType = TypeBoolean.get();
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
        int cmp = GMP.__gmpq_cmp(op1.getContent(), op2.getContent());
        ValueBoolean resultBoolean = ValueBoolean.as(result);
        if (operator == OperatorLt.LT) {
            resultBoolean.set(cmp < 0);
        } else if (operator == OperatorLe.LE) {
            resultBoolean.set(cmp <= 0);
        } else if (operator == OperatorEq.EQ) {
            resultBoolean.set(cmp == 0);            
        } else if (operator == OperatorGe.GE) {
            resultBoolean.set(cmp >= 0);
        } else if (operator == OperatorGt.GT) {
            resultBoolean.set(cmp > 0);
        } else {
            assert false;
        }
    }
}
