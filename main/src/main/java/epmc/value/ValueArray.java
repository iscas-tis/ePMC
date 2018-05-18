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

/**
 * {@link Value} storing multiple {@link Value}s of given {@link Type}.
 * In principle, multiple values could just be stored as Java arrays.
 * Using a subinterface of {@link Value} instead of Java arrays has the
 * advantage of flexibility, such that for instance
 * <ul>
 * <li>they can use specialised data structures to be more memory-efficient,
 * </li>
 * <li>they can be stored in native memory for preparing calls to native
 * code,</li>
 * <li>or in the hard-disk to safe memory,</li>
 * <li>while they can still be created using the same API as arrays in Java
 * memory.</li>
 * </ul>
 * 
 * @author Ernst Moritz Hahn
 */
public interface ValueArray extends Value {
    /**
     * Checks whether given value is an array value.
     * 
     * @param value value for which to check whether it is an array value
     * @return whether given value is an array value
     */
    public static boolean is(Value value) {
        return value instanceof ValueArray;
    }

    /**
     * Cast given value to array type.
     * If the type is not an array value, {@code null} will be returned.
     * 
     * @param value value to cast to array type
     * @return value casted to array value, or {@null} if not possible to cast
     */
    public static ValueArray as(Value value) {
        if (is(value)) {
            return (ValueArray) value;
        } else { 
            return null;
        }
    }

    @Override
    TypeArray getType();

    void setSize(int size);

    int size();

    void get(Value value, int index);

    void set(Value value, int index);
}
