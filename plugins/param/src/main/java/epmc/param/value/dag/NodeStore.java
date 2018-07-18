package epmc.param.value.dag;

public interface NodeStore {
    public interface Builder {
        NodeStore build();
    }
    
    int add(OperatorType type, int operandLeft, int operandRight);
    
    boolean assertValidNumber(int number);
    
    OperatorType getType(int number);
    
    int getOperandLeft(int number);
    
    int getOperandRight(int number);
    
    int getNumNodes();

    void sendStatistics();
}
