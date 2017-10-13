package epmc.value.operatorevaluator;

import epmc.value.Operator;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;

public interface OperatorEvaluatorSimpleBuilder {
    void setOperator(Operator operator);
        
    void setTypes(Type[] types);
        
    OperatorEvaluator build();
}
