package epmc.jani.value;

import epmc.jani.explorer.TypeDecision;
import epmc.jani.explorer.ValueDecision;
import epmc.operator.Operator;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.operatorevaluator.OperatorEvaluatorSimpleBuilder;

public final class OperatorEvaluatorSetDecisionDecision implements OperatorEvaluator {
    public final static class Builder implements OperatorEvaluatorSimpleBuilder {
        private boolean built;
        private Operator operator;
        private Type[] types;

        @Override
        public void setOperator(Operator operator) {
            assert !built;
            this.operator = operator;
        }

        @Override
        public void setTypes(Type[] types) {
            assert !built;
            this.types = types;
        }

        @Override
        public OperatorEvaluator build() {
            assert !built;
            assert operator != null;
            assert types != null;
            built = true;
            for (Type type : types) {
                assert type != null;
            }
            if (operator != OperatorSet.SET) {
                return null;
            }
            if (types.length != 2) {
                return null;
            }
            if (!TypeDecision.is(types[0])) {
                return null;
            }
            if (!TypeDecision.is(types[1])) {
                return null;
            }
            return new OperatorEvaluatorSetDecisionDecision(this);
        }
    }

    private final OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeInteger.get(), TypeInteger.get());
    private final Type resultType;

    private OperatorEvaluatorSetDecisionDecision(Builder builder) {
        resultType = builder.types[1];
    }

    @Override
    public Type resultType() {
        return resultType;
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        Value[] resultValues = ValueDecision.as(result).getValues();
        Value[] operandValues = ValueDecision.as(operands[0]).getValues();
        assert resultValues.length == operandValues.length;
        for (int valueNr = 0; valueNr < resultValues.length; valueNr++) {
            set.apply(resultValues[valueNr], operandValues[valueNr]);
        }
    }
}
