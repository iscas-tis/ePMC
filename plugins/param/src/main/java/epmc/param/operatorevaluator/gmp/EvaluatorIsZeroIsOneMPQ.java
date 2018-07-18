package epmc.param.operatorevaluator.gmp;

import epmc.operator.Operator;
import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorIsZero;
import epmc.param.value.gmp.GMP;
import epmc.param.value.gmp.TypeMPQ;
import epmc.param.value.gmp.ValueMPQ;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorIsZeroIsOneMPQ implements OperatorEvaluator {
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
            if (operator != OperatorIsZero.IS_ZERO
                    && operator != OperatorIsOne.IS_ONE) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeMPQ.is(types[0])) {
                return null;
            }
            return new EvaluatorIsZeroIsOneMPQ(this);
        }        
    }

    private final TypeBoolean resultType;
    private final Operator operator;
    
    private EvaluatorIsZeroIsOneMPQ(Builder builder) {
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
        ValueMPQ operand = ValueMPQ.as(operands[0]);
        ValueBoolean resultBoolean = ValueBoolean.as(result);
        boolean rbool;
        if (operator == OperatorIsZero.IS_ZERO) {
            rbool = GMP.gmp_util_mpq_is_zero(operand.getContent()) != 0;
        } else {
            assert operator == OperatorIsOne.IS_ONE;
            rbool = GMP.gmp_util_mpq_is_one(operand.getContent()) != 0;            
        }
        resultBoolean.set(rbool);
    }
}
