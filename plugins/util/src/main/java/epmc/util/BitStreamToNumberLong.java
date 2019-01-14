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

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

final class BitStreamToNumberLong implements BitStoreableToNumber {
    private final class ReadWriteHelper implements BitStream {
        @Override
        public boolean read() {
            boolean value = (bitSet & (1L << index)) != 0;
            index++;
            assert index <= Long.SIZE;
            return value;
        }

        @Override
        public int readInt(int numBits) {
            long result = bitSet;
            result >>>= index;
            long mask = ~0L;
            mask >>>= Long.SIZE - numBits;
            result &= mask;
            index += numBits;
            return (int) result;
        }

        @Override
        public void write(boolean value) {
            if (value) {
                bitSet |= 1L << index;
            } else {
                bitSet &= ~(1L << index);
            }
            index++;
            assert index <= Long.SIZE;
        }

        @Override
        public void writeInt(int value, int numBits) {
            long valueLong = value;
            valueLong <<= index;
            bitSet |= valueLong;
            index += numBits;
        }
    }

    private long bitSet;
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
        bitSet = 0L;
        index = 0;
    }

    public void set(long bitSet) {
        this.bitSet = bitSet;
        index = 0;
    }

    private long get() {
        return bitSet;
    }

    private final Long2IntOpenHashMap nodeToNumber = new Long2IntOpenHashMap();
    private final LongArrayList numberToNode = new LongArrayList();

    BitStreamToNumberLong() {
        nodeToNumber.defaultReturnValue(-1);
    }
    
    private void setNumber(int number) {
        set(numberToNode.getLong(number));
    }

    private int toNumber() {
        long key = get();
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
