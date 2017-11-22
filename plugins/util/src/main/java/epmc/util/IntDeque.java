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
 * Integer double-ended queue (deque).
 * This implementation is based on a ring buffer using a dynamic arrays, making
 * it more space and time efficient than e.g. implementations based on linked
 * lists. The class is there because there is nothing like this in Trove (yet),
 * and using Java classes would be unnecessarily inefficient.
 * 
 * @url https://en.wikipedia.org/wiki/Double-ended_queue
 * @url https://en.wikipedia.org/wiki/Circular_buffer
 * @url https://en.wikipedia.org/wiki/Dynamic_array
 * 
 * @author Ernst Moritz Hahn
 */
public final class IntDeque {
    /** values in the deque */
    private int[] values;
    /** position of the head in the deque */
    private int head;
    /** position of the tail in the deque */
    private int tail;

    /**
     * Create new deque with given initial capacity.
     * Note that the size of the deque will be increased if necessary, so the
     * initial size specification is only for efficiency. The initial capacity
     * must be larger or equal to 1.
     * 
     * @param initialSize initial size of the deque
     */
    public IntDeque(int initialSize) {
        assert initialSize >= 1;
        this.values = new int[initialSize];
    }

    /**
     * Create new deque.
     * The initial capacity will be 1, but will be increased later if necessary.
     */
    public IntDeque() {
        this(1);
    }

    /**
     * Adds an element at the end of the deque.
     * 
     * @param element element to add to the end of the deque
     */
    public void addLast(int element) {
        values[head] = element;
        head = (head + 1) % values.length;
        if (head == tail) {
            int oldLength = values.length;
            int[] newValues = new int[oldLength * 2];
            for (int i = 0; i < values.length; i++) {
                newValues[i] = values[(tail + i) % values.length];
            }
            values = newValues;
            head = oldLength;
            tail = 0;
        }
    }

    /**
     * Adds an element at the beginning of the deque.
     * 
     * @param element element to add to the beginning of the deque
     */
    public void addFirst(int element) {
        tail = Math.floorMod(tail - 1, values.length);
        if (head == tail) {
            int oldLength = values.length;
            int[] newValues = new int[oldLength * 2];
            for (int i = 0; i < values.length; i++) {
                newValues[i] = values[(tail + i) % values.length];
            }
            values = newValues;
            head = oldLength;
            tail = 0;
        }
        values[tail] = element;
    }

    /**
     * Remove and obtain the first element of the deque.
     * The deque must not be empty before calling this function.
     * 
     * @return first element of deque before removal
     */
    public int removeFirst() {
        assert head != tail;
        int elem = values[tail];
        tail = (tail + 1) % values.length;
        return elem;
    }

    /**
     * Remove and obtain the last element of the deque.
     * The deque must not be empty before calling this function.
     * 
     * @return last element of deque before removal
     */
    public int removeLast() {
        assert head != tail;
        head = Math.floorMod(head - 1, values.length);
        int elem = values[head];
        return elem;
    }

    /**
     * Check whether deque is empty.
     * 
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return tail == head;
    }

    /**
     * Clears this deque.
     * Implementation notice: this will only reset the head and tail positions;
     * the capacity of the deque will not be reset.
     */
    public void clear() {
        head = 0;
        tail = 0;
    }
}
