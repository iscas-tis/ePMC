package epmc.util;

import java.util.Arrays;

import epmc.util.BitStoreable;
import epmc.util.BitStoreableToNumber;
import epmc.util.BitStream;
import epmc.util.map.MapBitStoreableInteger;
import epmc.util.map.UtilMap;

final class BitStreamToNumberMap implements BitStoreableToNumber {
    private final class ReadWriteHelper implements BitStream {
        private static final int LOG2INTSIZE = 5;
        private int index;
        private int[] array;
        
        private void set(int index, int[] array) {
            assert index >= 0;
            assert array != null;
            this.index = index;
            this.array = array;
        }
        
        @Override
        public boolean read() {
            int offset = index >>> LOG2INTSIZE;
            boolean value = (array[offset] & (1 << index)) != 0;
            index++;
            return value;
        }

        @Override
        public void write(boolean value) {
            int offset = index >>> LOG2INTSIZE;
            if (value) {
                array[offset] |= 1 << index;
            } else {
                array[offset] &= ~(1 << index);
            }
            index++;
        }        
    }

    private int size;
    private final MapBitStoreableInteger map;
    private int[] prepare;
    private int[] fromNumber;
    private final ReadWriteHelper readWriteHelper = new ReadWriteHelper();
    private int numBits;
    
    BitStreamToNumberMap(int numBits) {
        assert numBits >= 0;
        this.map = UtilMap.newMapBitStoreableInteger(numBits);
        map.setMissingValue(-1);
        this.size = 1;
        this.fromNumber = new int[UtilMap.numBitsToNumInts(numBits * size)];
        this.numBits = numBits;
        this.prepare = UtilMap.newBitSetInt(numBits);
    }
    
    @Override
    public int toNumber(BitStoreable storeable) {
        assert storeable != null;
        readWriteHelper.set(0, prepare);
        storeable.write(readWriteHelper);
        int number = map.get(this.prepare);
        if (number == -1) {
            number = map.size();
            map.put(prepare, number);
            ensureSize((number + 1) * numBits);
            write(prepare, number);
        }
        return number;
    }

    private void ensureSize(int size) {
        if (size <= this.size) {
            return;
        }
        int newSize = this.size;
        while (newSize < size) {
            newSize *= 2;
        }
        fromNumber = Arrays.copyOf(fromNumber, UtilMap.numBitsToNumInts(newSize));
    }

    @Override
    public void fromNumber(BitStoreable storeable, int number) {
        assert storeable != null;
        assert number >= 0;
        readWriteHelper.set(numBits * number, fromNumber);
        storeable.read(readWriteHelper);
    }

    @Override
    public String toString() {
        return map.toString();
    }
    
    private void write(int[] prepare, int entry) {
        assert entry >= 0 : entry;
        for (int bit = 0; bit < numBits; bit++) {
            UtilMap.setBitSet(fromNumber, entry * numBits + bit, UtilMap.getBitSet(prepare, bit));
        }
    }

    @Override
    public int size() {
        return map.size();
    }
}
