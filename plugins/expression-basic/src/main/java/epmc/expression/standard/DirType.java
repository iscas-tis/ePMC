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

package epmc.expression.standard;

/**
 * Enum specifying whether minimal/maximal/exists/forall values be computed.
 * 
 * @author Ernst Moritz Hahn
 */
public enum DirType {
    /** Enum for maximising values. */
    MAX("max"),
    /** Enum for minimising values. */
    MIN("min"),
    EXISTS("exists"),
    FORALL("forall"),    
    /** Enum for unspecified direction. */
    NONE("");

    /** User-readable {@link String} representing the direction. */
    private String string;

    /**
     * Construct new direction type.
     * The parameter must not be {@code null}.
     * 
     * @param string string representing direction type.
     */
    private DirType(String string) {
        assert string != null;
        this.string = string;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * Check whether this object represents the minimising direction.
     * 
     * @return whether this object represents the minimising direction
     */
    public boolean isMin() {
        return this == MIN;
    }

    /**
     * Check whether this object represents the maximising direction.
     * 
     * @return whether this object represents the maximising direction
     */
    public boolean isMax() {
        return this == MAX;
    }

    public boolean isExists() {
        return this == EXISTS;
    }

    public boolean isForall() {
        return this == FORALL;
    }

    /**
     * Check whether this object represents the unspecified direction.
     * 
     * @return whether this object represents the unspecified direction
     */
    public boolean isNone() {
        return this == NONE;
    }
}
