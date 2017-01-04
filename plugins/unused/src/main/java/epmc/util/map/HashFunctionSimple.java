package epmc.util.map;

import java.util.Arrays;

final class HashFunctionSimple implements HashFunction {
    private int[] array;
    private int numBits;
    private int size;

    @Override
    public void setArray(int[] array) {
        assert array != null;
        this.array = array;
    }

    @Override
    public void setNumBits(int numBits) {
        assert numBits >= 0;
        this.numBits = numBits;
    }

    @Override
    public void setFieldSize(int size) {
        assert size >= 0;
        this.size = size;
    }

    @Override
    public int computeHash() {
        int hash = Arrays.hashCode(array);
        /*
        int hash = 0;
        int numInts = UtilMap.numBitsToNumInts(numBits);
        for (int intNr = 0; intNr < numInts; intNr++) {
            int current = array[intNr];
            hash = current + (hash << 6) + (hash << 16) - hash;
        }
        */
        /*
        int remBits = numBits % Integer.SIZE;
        int mask = ~(-1 << remBits);
        int current = array[numInts - 1] & mask;
        hash = current + (hash << 6) + (hash << 16) - hash;
        */
        hash = hash % size;
        if (hash < 0) {
            hash += size;
        }
        assert hash >= 0 : size;
        assert hash < size;
        return hash;
    }

    @Override
    public HashFunctionSimple clone() {
        HashFunctionSimple result = new HashFunctionSimple();
        result.setArray(array);
        result.setFieldSize(size);
        result.setNumBits(numBits);
        return result;
    }
}
