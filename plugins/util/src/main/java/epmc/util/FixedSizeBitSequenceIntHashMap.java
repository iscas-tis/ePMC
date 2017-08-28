/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.util;

import java.util.Arrays;

public final class FixedSizeBitSequenceIntHashMap<K extends BitStoreable> {
    public enum ProbeMethod {
        LINEAR,
        QUADRATIC
    }

    public enum GrowMethod {
        DUPLICATE,
        DUPLICATE_NEXT_PRIME
    }

    private final class BitsIntHashMapWriter implements BitStream {
        private int prepareIndex;

        @Override
        public boolean read() {
            assert prepareIndex < bitsPerEntry;
            int offset = prepareIndex >> LOG2LONGSIZE;
            boolean value = (prepareEntry[offset] & (1L << prepareIndex)) != 0;
            prepareIndex++;
            return value;
        }

        @Override
        public void write(boolean value) {
            assert prepareIndex < bitsPerEntry;
            int offset = prepareIndex >> LOG2LONGSIZE;
            if (value) {
                prepareEntry[offset] |= 1L << prepareIndex;
            } else {
                prepareEntry[offset] &= ~(1L << prepareIndex);
            }
            prepareIndex++;
        }

        private void read(K key) {
            assert assertKey(key);
            prepareIndex = 0;
            key.write(this);
            while (prepareIndex < bitsPerEntry) {
                int offset = prepareIndex >> LOG2LONGSIZE;
            prepareEntry[offset] &= ~(1L << prepareIndex);
            prepareIndex++;
            }
        }

        private void write(K key) {
            assert assertKey(key);
            prepareIndex = 0;
            key.read(this);
            while (prepareIndex < bitsPerEntry) {
                prepareIndex++;
            }
        }
    }

    private static final int LOG2LONGSIZE = 6; // log_2 Long.SIZE
    private final static int UNBOUNDED_SIZE = Integer.MAX_VALUE;
    private final static int NOT_FOUND_INDEX = -1;

    private final static int DEFAULT_NUM_SLOTS = 128;
    private final static int DEFAULT_NOT_FOUND_VALUE = -1;
    private final static double DEFAULT_LOAD_FACTOR = 0.75;
    private final static ProbeMethod DEFAULT_PROBE_METHOD = ProbeMethod.QUADRATIC;
    private final static GrowMethod DEFAULT_GROW_METHOD = GrowMethod.DUPLICATE_NEXT_PRIME;

    private final BitsIntHashMapWriter bitsIntHashMapWriter =
            new BitsIntHashMapWriter();
    private final int notFoundValue;
    private final ProbeMethod probeMethod;
    private final GrowMethod growMethod;
    private final K helper;
    private final Class<? extends BitStoreable> helperClass;
    private final double loadFactor;
    private final int bitsPerEntry;
    private final long[] prepareEntry;

    private long[] keys;
    private int[] values;
    private long[] occupiedRemoved;
    private int numSlots;
    private int numOccupied;
    private int numRemoved;

    public FixedSizeBitSequenceIntHashMap(int numSlots, int notFoundValue,
            double loadFactor, ProbeMethod probeMethod, GrowMethod growMethod,            
            int bitsPerEntry,
            K helper) {
        assert numSlots > 0;
        assert loadFactor > 0.0;
        assert loadFactor < 1.0;
        assert bitsPerEntry >= 0;
        assert bitsPerEntry < UNBOUNDED_SIZE;
        assert probeMethod != null;
        assert growMethod != null;
        assert helper != null;
        this.bitsPerEntry = bitsPerEntry;
        this.keys = new long[sizeToNumWords(bitsPerEntry * numSlots)];
        this.values = new int[numSlots];
        this.occupiedRemoved = new long[sizeToNumWords(2 * numSlots)];
        this.prepareEntry = new long[sizeToNumWords(bitsPerEntry)];
        this.notFoundValue = notFoundValue;
        this.probeMethod = probeMethod;
        this.growMethod = growMethod;
        this.helper = helper;
        this.helperClass = helper.getClass();
        this.loadFactor = loadFactor;
    }

    public FixedSizeBitSequenceIntHashMap(int bitsPerEntry, K helper) {
        this(DEFAULT_NUM_SLOTS, DEFAULT_NOT_FOUND_VALUE, DEFAULT_LOAD_FACTOR, DEFAULT_PROBE_METHOD, DEFAULT_GROW_METHOD,
                bitsPerEntry, helper);
    }

    public int put(K key, int value) {
        assert assertKey(key);
        resizeIfNecessary();
        bitsIntHashMapWriter.read(key);
        return put(value);
    }

    public int put(int value) {
        int hash = computeHash();
        int storeIndex = hash % numSlots;

        boolean stop = false;
        boolean found = false;
        boolean cycle = false;
        final int initIndex = storeIndex;
        int stepNr = 0;
        while (!stop) {
            storeIndex = probe(hash, stepNr) % numSlots;
            if (!isRemoved(storeIndex) && !isOccupied(storeIndex)) {
                stop = true;
            } else if (stepNr > 0 && initIndex == storeIndex) {
                stop = true;
                cycle = true;
            } else if (isEntryAt(storeIndex)) {
                found = true;
                stop = true;
            }
            stepNr++;
        }

        if (cycle) {
            resize();
        }

        writeEntry(storeIndex);
        setOccupied(storeIndex, true);
        numOccupied++;
        if (isRemoved(storeIndex)) {
            setRemoved(storeIndex, false);
            numRemoved--;
        }

        if (found) {
            return values[storeIndex];
        } else {
            return notFoundValue;
        }
    }

    private void resizeIfNecessary() {
        if ((double) numOccupied / (double) numSlots > loadFactor) {
            resize();
        }
    }

    private void writeEntry(int storeIndex, long[] keys, long[] prepareEntry) {
        assert storeIndex >= 0;
        assert storeIndex < numSlots;
        for (int pos = 0; pos < bitsPerEntry; pos++) {
            int entryIndex = pos;
            int entryOffset = entryIndex >> LOG2LONGSIZE;
        boolean entryValue = (prepareEntry[entryOffset] & (1L << entryIndex)) != 0;
        int keysIndex = storeIndex * bitsPerEntry + pos;
        int keysOffset = keysIndex >> LOG2LONGSIZE;
        if (entryValue) {
            prepareEntry[keysOffset] |= 1L << keysIndex;
        } else {
            prepareEntry[keysOffset] &= ~(1L << keysIndex);
        }
        }
    }

    private void writeEntry(int storeIndex) {
        writeEntry(storeIndex, keys, prepareEntry);
    }

    private void readEntry(int storeIndex, long[] keys, long[] prepareEntry) {
        assert storeIndex >= 0;
        assert storeIndex < numSlots;
        for (int pos = 0; pos < bitsPerEntry; pos++) {
            int entryIndex = pos;
            int entryOffset = entryIndex >> LOG2LONGSIZE;
        int keysIndex = storeIndex * bitsPerEntry + pos;
        int keysOffset = keysIndex >> LOG2LONGSIZE;            
            boolean keysValue = (keys[keysOffset] & (1L << keysIndex)) != 0;
            if (keysValue) {
                prepareEntry[entryOffset] |= 1L << entryIndex;
            } else {
                prepareEntry[entryOffset] &= ~(1L << entryIndex);
            }
        }
    }

    private void readEntry(int storeIndex) {
        assert storeIndex >= 0;
        assert storeIndex < numSlots;
        readEntry(storeIndex, keys, prepareEntry);
    }

    public int get(K key) {
        assert assertKey(key);
        int storeIndex = find(key);
        if (storeIndex >= 0) {
            return values[storeIndex];
        } else {
            return notFoundValue;
        }
    }

    public boolean containsKey(K key) {
        assert assertKey(key);
        return find(key) != NOT_FOUND_INDEX;
    }

    private int find(K key) {
        assert assertKey(key);
        // TODO
        bitsIntHashMapWriter.read(key);
        int hash = computeHash();
        int storeIndex = hash % numSlots;
        if (isEntryAt(storeIndex)) {
            return storeIndex;
        }
        boolean stop = false;
        boolean found = false;
        final int initIndex = storeIndex;
        int stepNr = 0;
        while (!stop) {
            storeIndex = probe(hash, stepNr) % numSlots;
            if (!isRemoved(storeIndex) && !isOccupied(storeIndex)) {
                stop = true;
            } else if (stepNr > 0 && initIndex == storeIndex) {
                stop = true;
            } else if (isEntryAt(storeIndex)) {
                found = true;
                stop = true;
            }
            stepNr++;
        }

        if (found) {
            return storeIndex;
        } else {
            return notFoundValue;
        }
    }

    private int probe(int storeIndex, int stepNr) {
        assert storeIndex >= 0;
        assert storeIndex < numSlots;
        assert stepNr >= 0;
        int newIndex;
        switch (probeMethod) {
        case LINEAR:
            newIndex = storeIndex + stepNr;
            break;
        case QUADRATIC:
            int qAdd = ((stepNr + 1) / 2) * ((stepNr + 1) / 2);
            qAdd *= stepNr % 2 == 1 ? 1 : -1;
            newIndex = storeIndex + qAdd;
        default:
            assert false;
            return -1;
        }
        return newIndex;
    }

    public int remove(K key) {
        assert assertKey(key);
        int index = find(key);
        if (index == NOT_FOUND_INDEX) {
            return notFoundValue;
        } else {
            setOccupied(index, false);
            setRemoved(index, true);
            numOccupied--;
            numRemoved++;
            return values[index];
        }
    }

    private boolean isEntryAt(int storeIndex) {
        assert storeIndex >= 0;
        assert storeIndex < numSlots;
        for (int pos = 0; pos < bitsPerEntry; pos++) {
            int entryIndex = pos;
            int entryOffset = entryIndex >> LOG2LONGSIZE;
        boolean entryValue = (prepareEntry[entryOffset] & (1L << entryIndex)) != 0;
        int keysIndex = storeIndex * bitsPerEntry + pos;
        int keysOffset = keysIndex >> LOG2LONGSIZE;
        boolean keysValue = (prepareEntry[keysOffset] & (1L << keysIndex)) != 0;
        if (entryValue != keysValue) {
            return false;
        }
        }
        return true;
    }

    private int sizeToNumWords(int size) {
        assert size >= 0;
        assert size < UNBOUNDED_SIZE;
        return ((size - 1) >> LOG2LONGSIZE) + 1;
    }

    private void resize(int newNumSlots) {
        assert newNumSlots >= 0;

        long[] keysOld = this.keys;
        int[] valuesOld = this.values;
        long[] occupiedRemovedOld = occupiedRemoved;
        int numSlotsOld = numSlots;

        this.keys = new long[sizeToNumWords(bitsPerEntry * newNumSlots)];
        this.values = new int[newNumSlots];
        this.occupiedRemoved = new long[sizeToNumWords(2 * newNumSlots)];
        this.numSlots = newNumSlots;
        this.numRemoved = 0;

        for (int oldSlotNr = 0; oldSlotNr < numSlotsOld; oldSlotNr++) {
            if (isOccupied(oldSlotNr, occupiedRemovedOld)) {
                readEntry(oldSlotNr, keysOld, prepareEntry);
                int value = valuesOld[oldSlotNr];
                put(value);
            }
        }
    }

    private void resize() {
        int newSize;
        switch (growMethod) {
        case DUPLICATE:
            newSize = 2 * numSlots;
            break;
        case DUPLICATE_NEXT_PRIME:
            newSize = nextPrime(2 * numSlots);
            break;
        default:
            assert false;
            newSize = -1;
            break;        
        }
        resize(newSize);
    }

    private void setOccupied(int index, boolean occupied) {
        assert index >= 0;
        assert index < UNBOUNDED_SIZE;
        index *= 2;
        int offset = index >> LOG2LONGSIZE;
        if (occupied) {
            occupiedRemoved[offset] |= 1L << index;
        } else {
            occupiedRemoved[offset] &= ~(1L << index);
        }
    }

    private boolean isOccupied(int index, long[] occupiedRemoved) {
        assert index >= 0;
        assert index < UNBOUNDED_SIZE;
        index *= 2;
        int offset = index >> LOG2LONGSIZE;
        return (occupiedRemoved[offset] & (1L << index)) != 0;
    }

    private boolean isOccupied(int index) {
        return isOccupied(index, occupiedRemoved);
    }

    private void setRemoved(int index, boolean removed) {
        assert index >= 0;
        assert index < UNBOUNDED_SIZE;
        assert index >= 0;
        assert index < UNBOUNDED_SIZE;
        index *= 2 + 1;
        int offset = index >> LOG2LONGSIZE;
        if (removed) {
            occupiedRemoved[offset] |= 1L << index;
        } else {
            occupiedRemoved[offset] &= ~(1L << index);
        }
    }

    private boolean isRemoved(int index) {
        assert index >= 0;
        assert index < UNBOUNDED_SIZE;
        index *= 2 + 1;
        int offset = index >> LOG2LONGSIZE;
        return (occupiedRemoved[offset] & (1L << index)) != 0;
    }

    private int computeHash() {
        return Arrays.hashCode(prepareEntry);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int index = 0; index < numSlots; index++) {
            if (isOccupied(index)) {
                readEntry(index);
                bitsIntHashMapWriter.write(helper);
                builder.append(helper);
                builder.append("=");
                builder.append(values[index]);
                builder.append(", ");
            }
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append("}");
        return builder.toString();
    }

    private boolean assertKey(K key) {
        assert key != null;
        assert key.getClass() == helperClass;
        return true;
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
