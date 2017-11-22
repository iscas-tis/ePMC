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

package epmc.value;

import epmc.value.Value;

final class ValueArrayIntegerBounded implements ValueArrayInteger {
    /** Log2 of {@link Long#SIZE}. */
    private static final int LOG2LONGSIZE = 6;
    /** String containing a single space. */
    private final static String SPACE = " ";
    private final TypeArrayIntegerBounded type;
    /** Lower bound of integers stored in this array. */
    private final int lower;
    /** Upper bound of integers stored in this array. */
    private final int upper;
    /** Content of the array. */
    private long[] content;
    /** Bits used to store a single entry.
     * Note that, while this number could be computed using
     * <code>getEntryType().getNumBits()</code>, we store this information in a
     * variable here because the computation would be too expensive.
     * */
    private final int bitsPerEntry;
    private int size;

    ValueArrayIntegerBounded(TypeArrayIntegerBounded type) {
        assert type != null;
        this.type = type;
        this.content = new long[0];
        this.lower = TypeInteger.as(type.getEntryType()).getLowerInt();
        this.upper = TypeInteger.as(type.getEntryType()).getUpperInt();
        this.bitsPerEntry = getType().getEntryType().getNumBits();
    }

    @Override
    public int getInt(int index) {
        assert index >= 0;
        assert index < size();
        int number = 0;
        for (int bitNr = 0; bitNr < getBitsPerEntry(); bitNr++) {
            int bitIndex = index * getBitsPerEntry() + bitNr;
            int offset = bitIndex >> LOG2LONGSIZE;
        boolean bitValue = (content[offset] & (1L << bitIndex)) != 0;
        if (bitValue) {
            number |= (1 << bitNr);
        }
        }
        number += lower;
        return number;
    }

    @Override
    public void set(Value value, int index) {
        assert value != null;
        assert index >= 0;
        assert index < size();
        set(ValueInteger.as(value).getInt(), index);
    }

    @Override
    public void get(Value value, int index) {
        assert value != null;
        assert index >= 0;
        assert index < size();
        ValueAlgebra.as(value).set(getInt(index));
    }

    private int getBitsPerEntry() {
        return bitsPerEntry;
    }

    @Override
    public TypeArrayIntegerBounded getType() {
        return type;
    }

    public int getBoundLower() {
        return lower;
    }

    public int getBoundUpper() {
        return upper;
    }    

    @Override
    public int hashCode() {
        int hash = 0;
        hash = content.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public void set(int value, int index) {
        assert index >= 0;
        assert index < size();
        assert value >= lower : value + SPACE + lower;
        assert value <= upper : value + SPACE + upper;
        int number = value - lower;
        for (int bitNr = 0; bitNr < getBitsPerEntry(); bitNr++) {
            boolean bitValue = (number & (1 << bitNr)) != 0;
            int bitIndex = index * getBitsPerEntry() + bitNr;
            int offset = bitIndex >> LOG2LONGSIZE;
            if (bitValue) {
                content[offset] |= 1L << bitIndex;
            } else {
                content[offset] &= ~(1L << bitIndex);
            }
        }
    }

    @Override
    public void setSize(int size) {
        assert size >= 0;
        int numBits = size * getBitsPerEntry();
        int num = ((numBits - 1) >> LOG2LONGSIZE) + 1;
        this.content = new long[num];
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return UtilValue.arrayToString(this);
    }
}
