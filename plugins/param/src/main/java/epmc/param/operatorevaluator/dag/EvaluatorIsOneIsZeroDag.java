package epmc.param.operatorevaluator.dag;

import epmc.operator.Operator;
import epmc.operator.OperatorIsOne;
import epmc.operator.OperatorIsZero;
import epmc.param.value.dag.TypeDag;
import epmc.param.value.dag.ValueDag;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorIsOneIsZeroDag implements OperatorEvaluator {
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
            if (operator != OperatorIsOne.IS_ONE
                    && operator != OperatorIsZero.IS_ZERO) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeDag.is(types[0])) {
                return null;
            }
            return new EvaluatorIsOneIsZeroDag(this);
        }
        
    }

    private final TypeBoolean typeBoolean;
    private final Operator operator;
    
    private EvaluatorIsOneIsZeroDag(Builder builder) {
        assert builder != null;
        typeBoolean = TypeBoolean.get();
        operator = builder.operator;
    }

    @Override
    public Type resultType() {
        return typeBoolean;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValueBoolean resultBoolean = ValueBoolean.as(result);
        ValueDag operand = ValueDag.as(operands[0]);
        int number = operand.getNumber();
        if (operator == OperatorIsOne.IS_ONE) {
            resultBoolean.set(operand.getType().getDag().isOne(number));
        } else if (operator == OperatorIsZero.IS_ZERO) {
            resultBoolean.set(operand.getType().getDag().isZero(number));
        } else {
            assert false;
        }
    }
}
