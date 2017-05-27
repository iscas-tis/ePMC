package epmc.value;

import epmc.error.EPMCException;

public interface OperatorEvaluator {
	Operator getOperator();
	
    boolean canApply(Type... types);

    Type resultType(Operator operator, Type... types);

    void apply(Value result, Value... operands) throws EPMCException;
}
