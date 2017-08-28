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

package epmc.constraintsolver;

/**
 * Direction for which a given problem should be optimised.
 * 
 * @author Ernst Moritz Hahn
 */
public enum Direction {
    /** Solution is unspecified (any valid solution suffices). */
    FEASIBILITY,
    /** Minimal solution is required. */
    MIN,
    /** Maximal solution is required. */
    MAX
    ;

    /**
     * Check whether the direction is unspecified.
     * 
     * @return whether the direction is unspecified
     */
    public boolean isFeasibility() {
        return this == FEASIBILITY;
    }

    /**
     * Check whether a maximising solution is required.
     * 
     * @return whether a maximising solution is required.
     */
    public boolean isMax() {
        return this == MAX;
    }

    /**
     * Check whether a minimising solution is required.
     * 
     * @return whether a minimising solution is required.
     */
    public boolean isMin() {
        return this == MIN;
    }
}
