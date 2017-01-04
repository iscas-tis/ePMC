package epmc.util.map;

interface HashFunction extends Cloneable {
    void setArray(int[] array);
    
    void setNumBits(int numBits);

    void setFieldSize(int size);
    
    int computeHash();
    
    HashFunction clone();
}
