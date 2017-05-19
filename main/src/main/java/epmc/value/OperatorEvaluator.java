package epmc.value;

import epmc.error.EPMCException;

public interface OperatorEvaluator {
    boolean canApply(Operator operator, Type... types);

    Type resultType(Operator operator, Type... types);

    void apply(Value result, Value... operands) throws EPMCException;
}
