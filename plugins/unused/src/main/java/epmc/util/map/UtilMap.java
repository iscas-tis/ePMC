package epmc.util.map;

public final class UtilMap {
    private static final int LOG2INTSIZE = 5;
    private static final int LOG2LONGSIZE = 6;

    private UtilMap() {
    }
    
    public static MapBitStoreableInteger newMapBitStoreableInteger(int numBits) {
        return new MapBitStoreableIntegerHash(numBits);
    }
    
    public static int numBitsToNumInts(int numBits) {
        return ((numBits - 1) >>> LOG2INTSIZE) + 1;        
    }
    
    public static int numIntsToNumBits(int numInts) {
        assert numInts >= 0;
        return Integer.SIZE * numInts;
    }
    
    public static int numLongsToNumBits(int numLongs) {
        assert numLongs >= 0;
        return Long.SIZE * numLongs;
    }
    
    public static int[] newBitSetInt(int numBits) {
        return new int[numBitsToNumInts(numBits)];
    }
    
    public static void setBitSet(int[] bitSet, int entry, boolean value) {
        int offset = entry >>> LOG2INTSIZE;
        if (value) {
            bitSet[offset] |= 1 << entry;
        } else {
            bitSet[offset] &= ~(1 << entry);
        }
    }

    public static boolean getBitSet(int[] bitSet, int entry) {
        assert bitSet != null;
        assert entry >= 0;
        int offset = entry >>> LOG2INTSIZE;
        return (bitSet[offset] & (1 << entry)) != 0;
    }
    
    public static int numBitsToNumLongs(int numBits) {
        assert numBits >= 0;
        return ((numBits - 1) >>> LOG2LONGSIZE) + 1;        
    }

    public static long[] newBitSetLong(int numBits) {
        assert numBits >= 0 : numBits;
        return new long[numBitsToNumLongs(numBits)];
    }
    
    public static void setBitSet(long[] bitSet, int entry, boolean value) {
        int offset = entry >>> LOG2LONGSIZE;
        if (value) {
            bitSet[offset] |= 1L << entry;
        } else {
            bitSet[offset] &= ~(1L << entry);
        }
    }

    public static boolean getBitSet(long[] bitSet, int entry) {
        assert entry >= 0 : entry;
        int offset = entry >>> LOG2LONGSIZE;
        assert offset >= 0 : offset;
        assert offset < bitSet.length : offset + " " + bitSet.length;
        return (bitSet[offset] & (1L << entry)) != 0;
    }
}
