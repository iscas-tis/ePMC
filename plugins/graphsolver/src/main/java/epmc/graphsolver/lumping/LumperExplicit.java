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

package epmc.graphsolver.lumping;

import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;

/**
 * Interface for a given explicit-state lumper.
 * This interface should be implemented by classes to be used for bisimulation
 * lumping. Depending on the options set, EPMC will check which lumpers are
 * applicable for a given model/property configuration and apply the lumping
 * at the appropriate occasions, e.g. before applying value iteration.
 * 
 * @author Ernst Moritz Hahn
 */
public interface LumperExplicit {
    /**
     * Obtain unique identifier for lumping class.
     * This identifier is used to allow users to choose between different lumper
     * implementations, provide feedback about the used lumper to users, etc.
     * 
     * @return unique identifier of lumping class
     */
    String getIdentifier();

    /**
     * Set the graph solver objective.
     * The graph solver objective contains the graph to be lumped as well as the
     * property for which the lumper should maintain validity.
     * 
     * @param objective graph solver objective to use
     */
    void setOriginal(GraphSolverObjectiveExplicit objective);

    /**
     * Checks whether lumper can perform lumping for the given configuration.
     * 
     * Whether the lumper can perform lumping depends on factors such as:
     * <ul>
     * <li>semantic type of original model</li>
     * <li>property types (e.g. weak bisimulation is invalid for step-bounded
     * until, whether the lumper can handle reward-based properties correctly,
     * etc.),</li>
     * <li>possible specialisation to subclasses of {@link GraphExplicit}.</li>
     * <li>...</li>
     * </ul>
     * The implementation must take care that the result of performing the
     * model checking will actually be valid and return {@code false} if in
     * doubt. The method may only be called once for a given object.
     * 
     * @return whether lumper can perform lumping for the given configuration
     */
    boolean canLump();

    /**
     * Perform lumping for given configuration.
     * The method may only be called if an immediately preceding call to {@link
     * #canLump()} would be allowed and returns or would return {@code true}.
     * The method may only be called once for a given object.
     * 
     */
    void lump();

    /**
     * Obtain quotient model.
     * The method may only be called after {@link #lump()} has finished without
     * throwing an exception.
     * 
     * @return quotient model
     */
    GraphSolverObjectiveExplicit getQuotient();

    void quotientToOriginal();
}
