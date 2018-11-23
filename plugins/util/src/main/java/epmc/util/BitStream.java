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

/**
 * Represents a stream of bits.
 * 
 * @author Ernst Moritz Hahn
 */
public interface BitStream {
    /**
     * Read next bit from bit stream and increase read cursor by one.
     * 
     * @return bit read
     */
    boolean read();

    /**
     * Write next bit to bit stream and increase write cursor by one.
     * 
     * @param value bit to write
     */
    void write(boolean value);

    default int readInt() {
        int result = 0;
        int mark = 1;
        for (int bitNr = 0; bitNr < Integer.SIZE; bitNr++) {
            result |= read() ? mark : 0;
            mark <<= 1;
        }
        return result;
    }
    
    default void writeInt(int value) {
        int mark = 1;
        for (int bitNr = 0; bitNr < Integer.SIZE; bitNr++) {
            write((value & mark) != 0);
            mark <<= 1;
        }
    }

    default int readInt(int numBits) {
        int result = 0;
        int mark = 1;
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            result |= read() ? mark : 0;
            mark <<= 1;
        }
        return result;
    }

    default void writeInt(int value, int numBits) {
        int mark = 1;
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            write((value & mark) != 0L);
            mark <<= 1;
        }
    }
    
    default long readLong() {
        long result = 0L;
        long mark = 1L;
        for (int bitNr = 0; bitNr < Long.SIZE; bitNr++) {
            result |= read() ? mark : 0L;
            mark <<= 1L;
        }
        return result;
    }
    
    default void writeLong(long value) {
        long mark = 1L;
        for (int bitNr = 0; bitNr < Long.SIZE; bitNr++) {
            write((value & mark) != 0L);
            mark <<= 1L;
        }
    }
    
    default double readDouble() {
        long v = readLong();
        double w = Double.longBitsToDouble(v);
        assert !Double.isNaN(w) : v;
        return w;
    }
    
    default void writeDouble(double value) {
        assert !Double.isNaN(value) : value;
        long l = Double.doubleToLongBits(value);
        writeLong(l);
    }
}
