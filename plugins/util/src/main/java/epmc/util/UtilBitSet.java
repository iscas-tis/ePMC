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

public final class UtilBitSet {
    /** String containing opening curly brace. */
    private static String CURLY_BRACE_OPEN = "{";
    /** String containing closing curly brace. */
    private static String CURLY_BRACE_CLOSE = "}";
    /** String containing a comma. */
    private static String COMMA = ",";

    /**
     * Auxiliary method to implement {@link BitSet#equals(Object)}.
     * 
     * @param thisObj {@code this} object of bitset
     * @param obj object to compare against
     * @return {@code true} iff equal
     */
    public static boolean equals(BitSet thisObj, Object obj) {
        assert thisObj != null;
        assert obj != null;
        if (!(obj instanceof BitSet)) {
            return false;
        }
        BitSet other = (BitSet) obj;
        int thisLength = thisObj.length();
        int otherLength = other.length();
        if (thisLength != otherLength) {
            return false;
        }
        for (int i = 0; i < thisLength; i++) {
            if (thisObj.get(i) == other.get(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Auxiliary method to implement {@link BitSet#hashCode()}.
     * 
     * @param bitSet {@code this} object of bitset
     * @return hash code of bitset
     */
    public static int hashCode(BitSet bitSet) {
        assert bitSet != null;
        int length = bitSet.length();
        int hash = 0;
        int help = 0;
        for (int i = 0; i < length; i++) {
            int mod = i % Integer.SIZE;
            help |= bitSet.get(i) ? (1 << mod) : 0;
            if (mod == (Integer.SIZE - 1)) {
                hash = help + (hash << 6) + (hash << 16) - hash;
                help = 0;
            }
        }
        if (length % Integer.SIZE != 0) {
            hash = help + (hash << 6) + (hash << 16) - hash;
        }
        return hash;
    }

    /**
     * Auxiliary method to implement {@link BitSet#toString()}.
     * 
     * @param bitSet {@code this} object of bitset
     * @return {@link String} representation of bit set
     */
    public static String toString(BitSet bitSet) {
        assert bitSet != null;
        StringBuilder result = new StringBuilder();
        result.append(CURLY_BRACE_OPEN);
        for (int i = bitSet.nextSetBit(0); i >= 0; i = bitSet.nextSetBit(i + 1)) {
            result.append(i);
            result.append(COMMA);
        }
        if (result.length() > 1) {
            result.delete(result.length() - 1, result.length());
        }
        result.append(CURLY_BRACE_CLOSE);
        return result.toString();
    }

    /**
     * Return new bounded bit set.
     * The actual class of the bounded bit set depends on the options of this
     * value context. The size parameter must be nonnegative.
     * 
     * @param size number of bits reserved for the bit set
     * @return unbounded bit set
     */
    public static BitSet newBitSetBounded(int size) {
        assert size >= 0;
        return new BitSetBoundedLongArray(size);
    }

    /**
     * Return new unbounded bit set.
     * The actual class of the unbounded bit set depends on the options of this
     * value context.
     * 
     * @return unbounded bit set
     */
    public static BitSet newBitSetUnbounded() {
        return new BitSetUnboundedLongArray();
    }

    /**
     * Return new unbounded bit set.
     * The actual class of the unbounded bit set depends on the options of this
     * value context. The initial size parameter must be nonnegative.
     * 
     * @param initialSize initial number of bits reserved for the bit set
     * @return unbounded bit set
     */
    public static BitSet newBitSetUnbounded(int initialSize) {
        assert initialSize >= 0;
        return new BitSetUnboundedLongArray(initialSize);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilBitSet() {
    }
}
