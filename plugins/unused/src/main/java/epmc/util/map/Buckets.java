package epmc.util.map;

interface Buckets {
    int getNumBits();
    
    int getNumBuckets();
    
    int getNumUsed();
    
    boolean isUsed(int bucket);
    
    void write(int[] entry, int bucket);
    
    void read(int[] entry, int bucket);
}
