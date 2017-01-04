package epmc.util.map;

import epmc.util.BitStoreable;
import epmc.util.BitStream;

final class MapBitStoreableIntegerHash implements MapBitStoreableInteger {
    private class WriteHelper implements BitStream {
        @Override
        public boolean read() {
            assert false;
            return false;
        }

        @Override
        public void write(boolean value) {
            UtilMap.setBitSet(prepare, readWriteIndex, value);
            readWriteIndex++;
        }
    }
    
    private final static int DEFAULT_INIT_SIZE = 10;
    private final static float DEFAULT_MAX_LOADFACTOR = 0.5f;
    private float maxLoadFactor;
    private final int numBits;
    private int missingValue = Integer.MAX_VALUE;
    private int numBuckets;
    private int numEntries;
    private HashFunction hashFunction;
    private ProbingFunction probingFunction;
    int readWriteIndex;
    private int[] prepare;
    private final WriteHelper writeHelper = new WriteHelper();
    private int[] keys;
    private int[] occupied;
    private int[] values;

    MapBitStoreableIntegerHash(int initSize, int numBits, float maxLoadFactor) {
        assert numBits >= 0;
        assert initSize >= 2;
        this.numBits = numBits;
        keys = UtilMap.newBitSetInt(numBits * initSize);
        values = new int[initSize];
        occupied = UtilMap.newBitSetInt(initSize);
        numBuckets = initSize;
        prepare = UtilMap.newBitSetInt(numBits);
        hashFunction = new HashFunctionSimple();
        hashFunction.setArray(this.prepare);
        hashFunction.setFieldSize(initSize);
        hashFunction.setNumBits(numBits);
        probingFunction = new ProbingFunctionQuadracticAlternate();
        probingFunction.setFieldSize(initSize);
        this.maxLoadFactor = maxLoadFactor;
    }
    
    MapBitStoreableIntegerHash(int numBits) {
        this(DEFAULT_INIT_SIZE, numBits, DEFAULT_MAX_LOADFACTOR);
    }
    
    @Override
    public void setMissingValue(int value) {
        missingValue = value;
    }

    @Override
    public boolean containsKey(BitStoreable key) {
        assert key != null;
        readWriteIndex = 0;
        key.write(writeHelper);
        int entry = findPosition(prepare);
        return entry >= 0;
    }

    @Override
    public int get(int[] prepare) {
        int entry = findPosition(prepare);
        if (entry == -1) {
            return missingValue;
        } else {
            return values[entry];
        }
    }

    private int findPosition(int[] prepare) {
        assert prepare != null;
        hashFunction.setArray(prepare);
        int hash = hashFunction.computeHash();
        int probNr = 0;
        do {
            int entry = probingFunction.getPosition(hash, probNr);
            if (entry == -1) {
                return -1;
            }
            if (!occupied(entry)) {
                return -1;
            }
            if (compare(prepare, entry)) {
                return entry;
            }
            probNr++;
        } while (true);
    }
    
    @Override
    public void put(int[] prepare, int value) {
        while ((numEntries + 1.0f) / numBuckets > this.maxLoadFactor) {
            resize();
        }
        boolean success;
        do {
            success = tryPut(prepare, value);
            if (!success) {
                resize();
            }
        } while (!success);
    }
    
    private boolean tryPut(int[] prepare, int value) {
        hashFunction.setArray(prepare);
        int hash = hashFunction.computeHash();
        int probNr = 0;
        do {
            int entry = probingFunction.getPosition(hash, probNr);
            if (entry == -1) {
                return false;
            }
            if (!occupied(entry)) {
                write(prepare, entry);
                setOccupied(entry);
                values[entry] = value;
                numEntries++;
                return true;
            }
            if (compare(prepare, entry)) {
                write(prepare, entry);
                setOccupied(entry);
                values[entry] = value;
                return true;
            }
            probNr++;
        } while (true);
    }

    private void setOccupied(int entry) {
        UtilMap.setBitSet(this.occupied, entry, true);
    }

    private void resize() {
        int[] oldKeys = keys;
        int[] oldOccupied = occupied;
        int[] oldValues = values;
        boolean success;
        int numElements;
        int oldNumBuckets = numBuckets;
        do {
            success = true;
            numBuckets = newBucketSize(numBuckets);
            keys = UtilMap.newBitSetInt(numBits * numBuckets);
            values = new int[numBuckets];
            occupied = UtilMap.newBitSetInt(numBuckets);
            hashFunction.setFieldSize(numBuckets);
            probingFunction.setFieldSize(numBuckets);
            numElements = 0;
            int[] prepare = UtilMap.newBitSetInt(numBits);
            for (int bucket = 0; bucket < oldNumBuckets; bucket++) {
                if (UtilMap.getBitSet(oldOccupied, bucket)) {
                    read(prepare, oldKeys, bucket);
                    success = success && tryPut(prepare, oldValues[bucket]);
                    if (!success) {
                        break;
                    }
                    numElements++;
                }
            }
        } while (!success);
        this.numEntries = numElements;
    }
    
    private int newBucketSize(int oldSize) {
        return nextPrime(2 * oldSize);
    }

    private void write(int[] prepare, int entry) {
        assert entry >= 0 : entry;
        assert entry < numBuckets : entry + " " + numBuckets;
        for (int bit = 0; bit < numBits; bit++) {
            UtilMap.setBitSet(keys, entry * numBits + bit, UtilMap.getBitSet(prepare, bit));
        }
    }

    private void read(int[] prepare, int[] from, int entry) {
        for (int bit = 0; bit < numBits; bit++) {
            UtilMap.setBitSet(prepare, bit, UtilMap.getBitSet(from, entry * numBits + bit));
        }
    }
    
    @Override
    public int size() {
        return numEntries;
    }
    
    private boolean compare(int[] prepare, int position) {
        position *= numBits;
        int compare = 0;
        int keyIndex = position / Integer.SIZE;
        int mod = position % Integer.SIZE;
        for (int index = 0; index < prepare.length; index++) {
            compare = keys[keyIndex];
            compare >>>= mod;
            if (keyIndex + 1 < keys.length && mod != 0) {
                int aux = keys[keyIndex + 1];
                aux = aux << (Integer.SIZE - mod);
                compare |= aux;
            }
            if (index == prepare.length - 1) {
                int mask = -1 >>> (Integer.SIZE - (numBits % Integer.SIZE));
                compare &= mask;
            }
            if (prepare[index] != compare) {
                return false;
            }
            keyIndex++;
        }
        return true;
        /*
        for (int bit = 0; bit < numBits; bit++) {
            if (UtilMap.getBitSet(prepare, bit)
                    != UtilMap.getBitSet(keys, position + bit)) {
                return false;
            }
        }
        return true;
                */
    }
    
    private boolean occupied(int position) {
        return UtilMap.getBitSet(occupied, position);
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        /*
        builder.append("{");
        for (int bucket = 0; bucket < numBuckets; bucket++) {
            if (occupied(bucket)) {
                builder.append(keyToString(bucket));
                builder.append("=");
                builder.append(values[bucket]);
                builder.append(",");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        */
        return builder.toString();
    }
    
    String keyToString(int bucket) {
        StringBuilder builder = new StringBuilder();
        for (int bit = 0; bit < numBits; bit++) {
            boolean value = UtilMap.getBitSet(keys, bucket * numBits + bit);
            builder.append(value ? "1" : "0");
        }
        return builder.toString();
    }
    
    private static boolean isPrime(int number) {
        // more efficient methods could be used, but this one suffices for the
        // order of magnitude of numbers (< Integer.MAX_VALUE) we consider
        int sqrt = (int) Math.sqrt(number) + 1;
        for (int dividor = 2; dividor < sqrt; dividor++) {
            if (number % dividor == 0) {
                return false;
            }
        }
        return true;
    }
    
    private static int nextPrime(int number) {
        int candidate = number;
        boolean primeFound = false;
        while (candidate < Integer.MAX_VALUE) {
            if (isPrime(candidate)) {
                primeFound = true;
                break;
            }
            candidate++;
        }
        if (primeFound) {
            return candidate;
        } else {
            return -1;
        }
    }
}
