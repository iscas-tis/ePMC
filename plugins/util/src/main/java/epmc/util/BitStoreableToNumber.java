package epmc.util;

public interface BitStoreableToNumber {
    int toNumber(BitStoreable storeable);
    
    void fromNumber(BitStoreable storeable, int number);

    int size();
    
    static BitStoreableToNumber newNodeStoreInt() {
        return new BitStreamToNumberInt();
    }
    
    static BitStoreableToNumber newNodeStoreLong() {
        return new BitStreamToNumberLong();
    }
    
    static BitStoreableToNumber newNodeStoreLongArray(int numBits) {
        assert numBits >= 0;
        return new BitStreamToNumberLongArray(numBits);
    }
}
