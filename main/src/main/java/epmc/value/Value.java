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
 * Interface to represent values used during analysis.
 * TODO complete documentation
 * 
 * @author Ernst Moritz Hahn
 */
public interface Value extends Comparable<Value> {
    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();

    @Override
    String toString();

    /**
     * Get type with which this value was created.
     * 
     * @return type with which this value was created
     */
    Type getType();

    /**
     * Set value to this value.
     * The value parameter must not be {@code null}.
     * 
     * @param value value to set this value to
     */
    void set(Value value);

    void set(String value);

    /**
     * Sets value to be immutable.
     * Attempts to modify immutable values should result in an assertion
     * failure (if assertions are enabled).
     * 
     * @see #isImmutable()
     */
    void setImmutable();

    /**
     * Checks whether value is immutable.
     * 
     * @see #setImmutable()
     * @return whether value is immutable
     */
    boolean isImmutable();

    double distance(Value other);

    /**
     * Check whether value is equal to another value.
     * In contrast to the {@link #equals(Object)} method, this method usually
     * should not perform an exact comparison. Values of different types such
     * as an integer value and a double value might be considered equal. Also,
     * values which differ by a certain epsilon, e.g. 1E-6, might be
     * considered equal, in particular when working with values such as
     * doubles for which basic operations are not exact.
     * 
     * @param other value to compare value to 
     * @return {@code true} if values are approximately equal
     */
    boolean isEq(Value other);
}
