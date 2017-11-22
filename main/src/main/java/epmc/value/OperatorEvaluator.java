package epmc.value;

public interface OperatorEvaluator {
    Type resultType();

    void apply(Value result, Value... operands);
}
