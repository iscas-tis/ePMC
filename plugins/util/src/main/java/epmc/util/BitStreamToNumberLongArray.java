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

import java.util.ArrayList;
import java.util.Arrays;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

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

    private final static class LongArrayStrategy implements Hash.Strategy<long[]> {
        @Override
        public int hashCode(long[] arg) {
            assert arg != null;
            return Arrays.hashCode(arg);
        }

        @Override
        public boolean equals(long[] arg0, long[] arg1) {
            return Arrays.equals(arg0, arg1);
        }
    }

    private long[] testLongArray;
    private Object2IntOpenCustomHashMap<long[]> nodeToNumber = new Object2IntOpenCustomHashMap<>(new LongArrayStrategy());

    private ArrayList<long[]> numberToNode = new ArrayList<>();
    private int size;

    BitStreamToNumberLongArray() {
        nodeToNumber.defaultReturnValue(-1);
    }
    
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
