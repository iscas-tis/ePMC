package epmc.value;

public interface OperatorEvaluator {
    Type resultType(Type... types);

    void apply(Value result, Value... operands);
}
