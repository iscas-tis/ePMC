package epmc.util.map;

import epmc.util.BitStoreable;

public interface MapBitStoreableInteger {
    void setMissingValue(int value);
    
    boolean containsKey(BitStoreable key);
    
    int size();

    void put(int[] prepare, int value);

    int get(int[] prepare);
}
