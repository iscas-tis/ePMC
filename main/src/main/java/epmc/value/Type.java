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
 * Type of given {@link Value}.
 * The main usage of this class is to create new {@link Value} objects of a
 * given type using {@Link #newValue()}.
 * 
 * @author Ernst Moritz Hahn
 * @see Value
 */
public interface Type {
    /**
     * Create a new value of this type.
     * 
     * @return new value of this type
     */
    Value newValue();

    /**
     * Check whether values of given type can be imported.
     * Importing is possible means that for the
     * {@link Value}s
     * created using
     * {@link #newValue()}
     * of this type can successfully call
     * {@link Value#set(Value)}
     * for values of the type given as parameter.
     * The type parameter must not be {@code null}.
     * 
     * @param type type of which to check whether values are importable
     * @return whether values of given type can be imported
     */
    boolean canImport(Type type);

    /**
     * Get array type for this type.
     * 
     * @return array type for this type
     */
    TypeArray getTypeArray();
}
