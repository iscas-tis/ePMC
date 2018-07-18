package epmc.param.value.dag.simplifier;

public interface DoubleStore {
    public interface Builder {
        DoubleStore build();
    }
    
    void add(double entry);
    
    int size();
    
    double get(int index);
}
