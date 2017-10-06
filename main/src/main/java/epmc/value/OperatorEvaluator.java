package epmc.value;

public interface OperatorEvaluator {
    Operator getOperator();

    boolean canApply(Type... types);

    Type resultType(Type... types);

    void apply(Value result, Value... operands);
}
