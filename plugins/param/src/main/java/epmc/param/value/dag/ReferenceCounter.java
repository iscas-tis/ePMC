package epmc.param.value.dag;

public interface ReferenceCounter {
    public interface Builder {
        ReferenceCounter build();
    }
    
    void addNode();
    
    boolean isAlive(int node);
    
    void incRef(int node);
    
    void decRef(int node);
}
