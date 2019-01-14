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

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

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
        public int readInt(int numBits) {
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
        public void writeInt(int value, int numBits) {
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

    private final Int2IntOpenHashMap nodeToNumber = new Int2IntOpenHashMap();
    private final IntArrayList numberToNode = new IntArrayList();

    BitStreamToNumberInt() {
        nodeToNumber.defaultReturnValue(-1);
    }
    
    private void setNumber(int number) {
        set(numberToNode.getInt(number));
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
