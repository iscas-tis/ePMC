package epmc.param.operatorevaluator;

import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.param.value.TypeFunction;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueSetString;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class EvaluatorSetFunctionReal implements OperatorEvaluator {
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
            if (operator != OperatorSet.SET) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeReal.is(types[0])) {
                return null;
            }
            if (TypeFunction.is(types[0])) {
                return null;
            }
            if (!TypeFunction.is(types[1])) {
                return null;
            }
            return new EvaluatorSetFunctionReal(this);
        }
    }

    private Type type;
    
    private EvaluatorSetFunctionReal(Builder builder) {
        assert builder != null;
        type = builder.types[1];
    }

    @Override
    public Type resultType() {
        return type;
    }

    @Override
    public void apply(Value result, Value... operands) {
        ValueSetString.as(result).set(operands[0].toString());
    }
}
