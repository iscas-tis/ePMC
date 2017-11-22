package epmc.value.operatorevaluator;

import epmc.operator.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;

public interface OperatorEvaluatorSimpleBuilder {
    void setOperator(Operator operator);
        
    void setTypes(Type[] types);
        
    OperatorEvaluator build();
}
