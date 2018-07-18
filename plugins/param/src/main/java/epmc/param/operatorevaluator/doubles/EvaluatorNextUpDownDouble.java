package epmc.param.operatorevaluator.doubles;

import epmc.operator.Operator;
import epmc.param.operator.OperatorNextDown;
import epmc.param.operator.OperatorNextUp;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.Value;
import epmc.value.ValueDouble;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorNextUpDownDouble implements OperatorEvaluator {
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
            if (operator != OperatorNextUp.NEXT_UP
                    && operator != OperatorNextDown.NEXT_DOWN) {
                return null;
            }
            if (types.length != 1) {
                return null;
            }
            if (!TypeDouble.is(types[0])) {
                return null;
            }
            return new EvaluatorNextUpDownDouble(this);
        }
    }

    private final TypeDouble resultType;
    private final boolean down;
    
    private EvaluatorNextUpDownDouble(Builder builder) {
        assert builder != null;
        assert builder.operator != null;
        assert builder.types != null;
        resultType = TypeDouble.as(builder.types[0]);
        down = builder.operator == OperatorNextDown.NEXT_DOWN;
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
        ValueDouble valueRational = ValueDouble.as(operands[0]);
        ValueDouble resultRational = ValueDouble.as(result);
        double doubleValue = valueRational.getDouble();
        if (down) {
            doubleValue = Math.nextDown(doubleValue);
        } else {
            doubleValue = Math.nextUp(doubleValue);
        }
        resultRational.set(doubleValue);
    }
}
