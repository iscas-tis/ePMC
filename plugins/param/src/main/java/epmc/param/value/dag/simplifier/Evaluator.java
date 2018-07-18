package epmc.param.value.dag.simplifier;

import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;

public interface Evaluator {
    public interface Builder {
        Builder setDag(Dag dag);
        
        Evaluator build();
    }
    
    int evaluate(OperatorType type, int operandLeft, int operandRight);

    void commitResult();

}
