package epmc.util;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.custom_hash.TObjectIntCustomHashMap;
import gnu.trove.strategy.HashingStrategy;

import java.util.ArrayList;
import java.util.Arrays;

final class BitStreamToNumberLongArray implements BitStoreableToNumber {
    private final class ReadWriteHelper implements BitStream {
        private static final int LOG2LONGSIZE = 6;
        
        @Override
        public boolean read() {
            int offset = index >>> LOG2LONGSIZE;
            boolean value = (bitSet[offset] & (1L << index)) != 0;
            index++;
            return value;
        }

        @Override
        public void write(boolean value) {
            int offset = index >>> LOG2LONGSIZE;
            if (value) {
                bitSet[offset] |= 1L << index;
            } else {
                bitSet[offset] &= ~(1L << index);
            }
            index++;
        }        
    }

    private final ReadWriteHelper helper = new ReadWriteHelper();
    private long[] bitSet;
    private int index;

    @Override
    public int toNumber(BitStoreable storeable) {
        assert storeable != null;
        reset();
        storeable.write(helper);
        return toNumber();
    }
    
    @Override
    public void fromNumber(BitStoreable storeable, int number) {
        setNumber(number);
        storeable.read(helper);
    }
    
    private void set(long[] bitSet) {
        this.bitSet = bitSet;
        index = 0;
    }
    
    private final static class LongArrayStrategy implements HashingStrategy<long[]> {
        private static final long serialVersionUID = 1L;

        @Override
        public int computeHashCode(long[] arg) {
            assert arg != null;
            return Arrays.hashCode(arg);
        }

        @Override
        public boolean equals(long[] arg0, long[] arg1) {
            assert arg0 != null;
            assert arg1 != null;
            return Arrays.equals(arg0, arg1);
        }
    }

    private long[] testLongArray;
    private TObjectIntMap<long[]> nodeToNumber = new TObjectIntCustomHashMap<>(new LongArrayStrategy(), 10, 0.5f, -1);

    private ArrayList<long[]> numberToNode = new ArrayList<>();
    private int size;

    private void setNumber(int number) {
        set(numberToNode.get(number));
    }
    
    private void reset() {
        Arrays.fill(testLongArray, 0L);
        set(testLongArray);
    }

    private int toNumber() {
        int newNumber = nodeToNumber.size();
        int number = nodeToNumber.putIfAbsent(testLongArray, newNumber);
        if (number == -1) {
            number = newNumber;
            numberToNode.add(testLongArray);
            testLongArray = new long[size];
        }
        return number;
    }
    
    public BitStreamToNumberLongArray(int numBits) {
        assert numBits >=0;
        this.size = numBits / Long.SIZE + (numBits % Long.SIZE > 0 ? 1 : 0);
        testLongArray = new long[size];
    }

    @Override
    public int size() {
        return numberToNode.size();
    }
}
