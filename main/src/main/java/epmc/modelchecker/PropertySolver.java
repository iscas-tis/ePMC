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

package epmc.modelchecker;

import java.util.Set;

import epmc.expression.Expression;
import epmc.graph.StateMap;
import epmc.graph.StateSet;

/**
 * Interface to implement by solvers for a property in {@link Expression} form.
 * 
 * @author Ernst Moritz Hahn
 */
public interface PropertySolver {
    // TODO identifier probably not needed
    /**
     * Returns a user-readable identifier for this solver.
     * The identifier is supposed to allow the user to choose between several
     * solvers 
     * 
     * @return identifier for this solver
     */
    String getIdentifier();

    /**
     * Sets the model checker for this property solver.
     * 
     * @param modelChecker model checker to set
     */
    void setModelChecker(ModelChecker modelChecker);

    /**
     * Set the property to be checked.
     * The property must not be {@code null}.
     * 
     * @param property property to be checked
     */
    void setProperty(Expression property);

    /**
     * Set for which states the property shall be solved.
     * The set of states must not be {@code null}.
     * 
     * @param forStates states for which the property shall be solved
     */
    void setForStates(StateSet forStates);

    default void setComputeScheduler(boolean computeScheduler) {
    }

    /**
     * Checks whether a property can be decided by this solver.
     * If the property can be solved, the method will return true.
     * If it cannot be solved because it is not intended to solve the particular
     * type of property (e.g. a PCTL solver called on an LTL query) it shall
     * return false. It shall also return false if the property cannot be solved
     * due to technical restrictions (e.g. an explicit-state PCTL solver called
     * for a BDD-based model checker. If the property cannot be solved because
     * of of incorrect model/property combination, the method shall throw an
     * exception instead of returning false.
     * 
     * @return whether the solver can decide the property
     */
    boolean canHandle();

    default boolean canComputeScheduler() {
        return false;
    }

    /**
     * Obtain set of identifiers of graph properties required by this solver.
     * 
     * @return set of identifiers of graph properties required by this solver
     */
    Set<Object> getRequiredGraphProperties();

    /**
     * Obtain set of identifiers of node properties required by this solver.
     * 
     * @return set of identifiers of node properties required by this solver
     */
    Set<Object> getRequiredNodeProperties();

    /**
     * Obtain set of identifiers of edge properties required by this solver.
     * 
     * @return set of identifiers of edge properties required by this solver
     */
    Set<Object> getRequiredEdgeProperties();

    /**
     * Decide given property.
     * 
     * @return value of the property for given relevant states
     */
    StateMap solve();
}
