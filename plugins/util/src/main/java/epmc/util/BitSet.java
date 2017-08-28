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
 * Bit set interface.
 * Java already includes a bit set implementation {@link java.util.BitSet}.
 * However, we decided to provide our own implementation because:
 * <ul>
 * <li>{@link java.util.BitSet} cannot be reasonably subclassed.
 * Implementation of cases where e.g. a bitset shall be optionally made
 * read-only are very annoying. Having different possible implemetations is
 * also interesting to balance speed against space. For instance, a bitset could
 * be implemented either by an array of booleans, or by a more compact but
 * slower array of longs, in which each long represents 64 values at once.<li>
 * <li>Subclassing even allows bit sets to be computed implicitly without
 * storing the bit values explicitly. For instance, a bit set could implicitly
 * compute the conjunction of two existing bit sets. This would be slower than
 * storing the bit values explicitly, but would require almost no memory, thus
 * being appropriate where speed is not the most important factor.</li>
 * <li>The {@link java.util.BitSet#clone()} returns an
 * {@link #Object} and not a bit set, such that a type cast is always needed,
 * which makes the source code unnecessarily less readable.</li>
 * <li>From some initial experiments, {@link java.util.BitSet} also seem to be
 * quite slow in some situations (see also the point about subclassing).</li>
 * <li>{@link java.util.BitSet} are always of unbounded size. For some cases,
 * it is useful to restrict the highest bit to a certain number, using
 * {@code assert} statements, to ease debugging.</li>
 * <li>{@link java.util.BitSet} is missing some functionality which we consider
 * useful. For instance, there is no logical OR operator which builds the OR
 * of all bits of two bitsets while leaving the two bit sets as they are,
 * instead assigning the result to a third bit set. Using the {@code default}
 * methods feature, it is easily possible to add such methods to a bit set
 * implementation which is an interface.</li>
 * <li>For performance, we only want to do any range checks if assertions are
 * enabled. However, {@link java.util.BitSet} also does range checks if this is
 * not the case. This might be useful for many applications because it is safer.
 * However, if we thrive for maximal speed, it is not.</li>
 * </ul>
 * The methods of this class are a superset of the methods of
 * {@link java.util.BitSet}. Only where necessary to allow interoperability with
 * other classes of EPMC, methods of this interface have names different from
 * the ones of {@link java.util.BitSet}.
 * 
 * @author Ernst Moritz Hahn
 */
public interface BitSet {
    /* methods to be implemented by implementing classes */

    void set(int bitIndex, boolean value);

    boolean get(int bitIndex);

    int size();

    BitSet clone();

    /* default methods - for efficiency, implementations might also want to
     * override some or most of the following methods */

    default int length() {
        int bitIndex = size() - 1;
        while (bitIndex >= 0) {
            if (get(bitIndex)) {
                return bitIndex + 1;
            }
            bitIndex--;
        }

        return 0;
    }

    default int nextSetBit(int index) {
        assert index >= 0;
        int size = size();
        while (index < size) {
            if (get(index)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    default int nextClearBit(int index) {
        assert index >= 0;
        int size = size();
        while (true) {
            if (index >= size || !get(index)) {
                return index;
            }
            index++;
        }
    }

    default void flip(int bitIndex) {
        assert bitIndex >= 0;
        set(bitIndex, !get(bitIndex));
    }

    default void clear(int fromIndex, int toIndex) {
        assert fromIndex >= 0;
        assert fromIndex <= toIndex;
        for (int bitIndex = fromIndex; bitIndex < toIndex; bitIndex++) {
            clear(bitIndex);
        }
    }

    default void and(BitSet operand1, BitSet operand2) {
        assert operand1 != null;
        assert operand2 != null;
        int size = Math.max(operand1.size(), operand2.size());
        for (int bitIndex = 0; bitIndex < size; bitIndex++) {
            set(bitIndex, operand1.get(bitIndex) && operand2.get(bitIndex));
        }
        clear(size, size());
    }

    default void or(BitSet operand1, BitSet operand2) {
        int size = Math.max(operand1.size(), operand2.size());
        for (int bitIndex = 0; bitIndex < size; bitIndex++) {
            set(bitIndex, operand1.get(bitIndex) || operand2.get(bitIndex));
        }
        clear(size, size());
    }

    default void xor(BitSet operand1, BitSet operand2) {
        int size = Math.max(operand1.size(), operand2.size());
        for (int bitIndex = 0; bitIndex < size; bitIndex++) {
            set(bitIndex, operand1.get(bitIndex) ^ operand2.get(bitIndex));
        }
        clear(size, size());
    }

    default void clear(int bitIndex) {
        set(bitIndex, false);
    }

    default void and(BitSet other) {
        and(this, other);
    }

    default void andNot(BitSet operand1, BitSet operand2) {
        int size = Math.max(operand1.size(), operand2.size());
        for (int bitIndex = 0; bitIndex < size; bitIndex++) {
            set(bitIndex, operand1.get(bitIndex) && !operand2.get(bitIndex));
        }
        clear(size, size());
    }

    default void andNot(BitSet other) {
        assert other != null;
        andNot(this, other);
    }

    default int cardinality() {
        int cardinality = 0;
        int size = size();
        for (int bitIndex = 0; bitIndex < size; bitIndex++) {
            cardinality += get(bitIndex) ? 1 : 0;
        }
        return cardinality;
    }

    default void clear() {
        clear(0, size());
    }

    default void flip(int fromIndex, int toIndex) {
        assert fromIndex >= 0;
        assert fromIndex <= toIndex;
        for (int bitIndex = fromIndex; bitIndex < toIndex; bitIndex++) {
            flip(bitIndex);
        }
    }

    default void set(int bitIndex) {
        set(bitIndex, true);
    }

    default void set(int fromIndex, int toIndex) {
        for (int bitIndex = fromIndex; bitIndex < toIndex; bitIndex++) {
            set(bitIndex);
        }
    }

    default void set(int fromIndex, int toIndex, boolean value) {
        for (int bitIndex = fromIndex; bitIndex < toIndex; bitIndex++) {
            set(bitIndex, value);
        }
    }

    default boolean intersects(BitSet set) {
        int size = Math.min(size(), set.size());
        for (int bitIndex = 0; bitIndex < size; bitIndex++) {
            if (get(bitIndex) && set.get(bitIndex)) {
                return false;
            }
        }
        return false;
    }

    default java.util.BitSet toJavaBitSet() {
        int size = size();
        java.util.BitSet result = new java.util.BitSet(size());
        for (int bitIndex = 0; bitIndex < size; bitIndex++) {
            result.set(bitIndex, get(bitIndex));
        }
        return result;
    }

    default boolean isEmpty() {
        return cardinality() == 0;
    }

    default void or(BitSet other) {
        or(this, other);
    }

    default void xor(BitSet other) {
        xor(this, other);
    }
}
