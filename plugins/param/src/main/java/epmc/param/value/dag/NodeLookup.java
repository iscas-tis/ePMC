package epmc.param.value.dag;

public interface NodeLookup {
    public interface Builder {
        Builder setNodeStore(NodeStore nodeStore);
        
        NodeLookup build();
    }
    
    int get(OperatorType type, int operandLeft, int operandRight);
    
    void put(OperatorType type, int operandLeft, int operandRight, int number);
    
    void sendStatistics();
}
