package epmc.param.value.dag.simplifier;

import epmc.param.value.dag.Dag;
import epmc.param.value.dag.OperatorType;

public interface Simplifier {
    static int INVALID = -1;
    
    interface Builder {
        Builder setDag(Dag dag);
        
        Simplifier build();
    }
    
    void setType(OperatorType type);
    
    void setOperandLeft(int operand);
    
    void setOperandRight(int operand);
    

    boolean simplify();
    
    default int getResultNode() {
        return INVALID;
    }
    
    OperatorType getResultType();
    
    int getResultOperandLeft();
    
    int getResultOperandRight();
    
    default void tellNewEntry(int number) {
    }

    default void sendStatistics() {
    }
}
