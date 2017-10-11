package epmc.jani.value;

import epmc.jani.explorer.TypeDecision;
import epmc.jani.explorer.ValueDecision;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.Value;
import epmc.value.operator.OperatorSet;

public enum OperatorEvaluatorSetDecisionDecision implements OperatorEvaluator {
    INSTANCE;

    @Override
    public Operator getOperator() {
        return OperatorSet.SET;
    }

    @Override
    public boolean canApply(Type... types) {
        assert types != null;
        assert types.length == 2;
        if (!TypeDecision.isDecision(types[0])) {
            return false;
        }
        if (!TypeDecision.isDecision(types[1])) {
            return false;
        }
        return true;
    }

    @Override
    public Type resultType(Type... types) {
        return types[1];
    }

    @Override
    public void apply(Value result, Value... operands) {
        assert result != null;
        assert operands != null;
        assert operands.length >= 1;
        Value[] resultValues = ValueDecision.asDecision(result).getValues();
        Value[] operandValues = ValueDecision.asDecision(operands[0]).getValues();
        assert resultValues.length == operandValues.length;
        OperatorEvaluator set = ContextValue.get().getOperatorEvaluator(OperatorSet.SET, TypeInteger.get(), TypeInteger.get());
        for (int valueNr = 0; valueNr < resultValues.length; valueNr++) {
            set.apply(resultValues[valueNr], operandValues[valueNr]);
        }
    }
}
