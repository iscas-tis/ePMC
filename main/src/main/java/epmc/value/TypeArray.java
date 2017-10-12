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

// TODO complete documentation

/**
 * Type to create {@link Value}s storing several {@link Value}s of given {@link Type}.
 * 
 * @author Ernst Moritz Hahn
 */
public interface TypeArray extends Type {
    /**
     * Checks whether given type is an array type.
     * 
     * @param type type for which to check whether it is an array type
     * @return whether given type is an array type
     */
    static boolean is(Type type) {
        return type instanceof TypeArray;
    }

    /**
     * Cast given type to array type.
     * If the type is not an array type, {@code null} will be returned.
     * 
     * @param type type to cast to array type
     * @return type casted to array type, or {@null} if not possible to cast
     */
    static TypeArray as(Type type) {
        if (is(type)) {
            return (TypeArray) type;
        } else {
            return null;
        }
    }

    /**
     * Get entry type of this array type.
     * 
     * 
     * @return entry type
     */
    Type getEntryType();

    @Override
    ValueArray newValue();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    @Override
    String toString();
}
