package epmc.value;

public interface OperatorEvaluatorFactory {
    public OperatorEvaluator getEvaluator(Operator operator, Type...types);
}
