package epmc.util;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

final class BitStreamToNumberInt implements BitStoreableToNumber {
    private final class ReadWriteHelper implements BitStream {
        @Override
        public boolean read() {
            boolean value = (bitSet & (1 << index)) != 0;
            index++;
            assert index <= Integer.SIZE;
            return value;
        }

        @Override
        public int read(int numBits) {
            int result = bitSet;
            result >>>= index;
            int mask = ~0;
            mask >>>= Integer.SIZE - numBits;
            result &= mask;
            index += numBits;
            return result;
        }
        
        @Override
        public void write(boolean value) {
            if (value) {
                bitSet |= 1 << index;
            } else {
                bitSet &= ~(1 << index);
            }
            index++;
            assert index <= Integer.SIZE;
        }
        
        @Override
        public void write(int value, int numBits) {
            value <<= index;
            bitSet |= value;
            index += numBits;
        }
    }

    private int bitSet;
    private int index;
    private final ReadWriteHelper helper = new ReadWriteHelper();

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

    private void reset() {
        bitSet = 0;
        index = 0;
    }
        
    public void set(int bitSet) {
        this.bitSet = bitSet;
        index = 0;
    }

    private int get() {
        return bitSet;
    }
    
    private final TIntIntMap nodeToNumber = new TIntIntHashMap(10, 0.5f, -1, -1);
    private final TIntArrayList numberToNode = new TIntArrayList();

    private void setNumber(int number) {
        set(numberToNode.get(number));
    }
    
    private int toNumber() {        
        int key = get();
        int newNumber = nodeToNumber.size();
        int number = nodeToNumber.putIfAbsent(key, newNumber);
        if (number == -1) {
            number = newNumber;
            numberToNode.add(key);
        }
        return number;
    }

    @Override
    public int size() {
        return numberToNode.size();
    }
}
