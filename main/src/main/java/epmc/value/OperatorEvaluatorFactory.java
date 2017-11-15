package epmc.value;

import epmc.operator.Operator;

public interface OperatorEvaluatorFactory {
    public OperatorEvaluator getEvaluator(Operator operator, Type...types);
}
