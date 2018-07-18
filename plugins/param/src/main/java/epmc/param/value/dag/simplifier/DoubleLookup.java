package epmc.param.value.dag.simplifier;

public interface DoubleLookup {
    public interface Builder {
        Builder setStore(DoubleStore store);
        
        DoubleLookup build();
    }
    
    int get(double entry);
    
    void put(double entry, int number);
}
