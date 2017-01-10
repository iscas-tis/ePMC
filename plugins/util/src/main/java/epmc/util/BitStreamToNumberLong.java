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

import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

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
        public int read(int numBits) {
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
        public void write(int value, int numBits) {
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

    private final TLongIntMap nodeToNumber = new TLongIntHashMap(10, 0.5f, -1L, -1);
    private final TLongArrayList numberToNode = new TLongArrayList();

    private void setNumber(int number) {
        set(numberToNode.get(number));
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
