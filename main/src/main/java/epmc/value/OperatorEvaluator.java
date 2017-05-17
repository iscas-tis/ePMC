package epmc.value;

import epmc.error.EPMCException;

public interface OperatorEvaluator {
	// TODO change back to Operator instead of String once restructuring done
    boolean canApply(String operator, Type... types);

    Type resultType(String operator, Type... types);

    void apply(Value result, Value... operands) throws EPMCException;
}
