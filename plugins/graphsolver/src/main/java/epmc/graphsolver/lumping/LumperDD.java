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

import epmc.dd.DD;
import epmc.expression.Expression;
import epmc.graph.dd.GraphDD;
import epmc.modelchecker.ModelChecker;

/**
 * Interface for a given decision-diagram based lumper.
 * This interface should be implemented by classes to be used for bisimulation
 * lumping. Depending on the options set, EPMC will check which lumpers are
 * applicable for a given model/property configuration and apply the lumping
 * at the appropriate occasions, e.g. before applying value iteration.
 * 
 * @author Ernst Moritz Hahn
 */
public interface LumperDD {
    /**
     * Obtain unique identifier for lumping class.
     * This identifier is used to allow users to choose between different lumper
     * implementations, provide feedback about the used lumper to users, etc.
     * 
     * @return unique identifier of lumping class
     */
    String getIdentifier();

    /**
     * Set model checker object to use for lumping.
     * Setting the model checker object allows the lumper to use auxiliary
     * methods provided there. The model checker parameter must not be
     * {@code null}. The method may only be called once for a given object.
     * 
     * @param modelChecker model checker object to use for lumping
     */
    void setModelChecker(ModelChecker modelChecker);

    /**
     * Sets the graph to be lumped.
     * The parameter must not be {@code null}. The method may only be called
     * once for a given object.
     * 
     * @param graph graph to be lumped
     */
    void setOriginal(GraphDD graph);

    /**
     * Requires that a given property is maintained by the lumping.
     * The method can be called several times to ensure the validity of several
     * properties. It must not be called with {@code null} parameter. It must
     * not be called after a call to {@link #canLump()} or {@link #lump()}.
     * 
     * @param property property required to be maintained by lumping.
     */
    void requireValidFor(Expression property);

    /**
     * Checks whether lumper can perform lumping for the given configuration.
     * Before calling this method, {@link #setModelChecker(ModelChecker)} and
     * {@link #setOriginal(GraphDD)} must have been called as well as the
     * necessary number of calls of {@link #requireValidFor(Expression)} or a
     * call to {@link #setInitialPartition(int[])}.
     * 
     * Whether the lumper can perform lumping depends on factors such as:
     * <ul>
     * <li>semantic type of original model</li>
     * <li>property types (e.g. weak bisimulation is invalid for step-bounded
     * until, whether the lumper can handle reward-based properties correctly,
     * etc.),</li>
     * <li>possible specialisation to subclasses of {@link GraphDD}.</li>
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
    GraphDD getQuotient();

    Expression getQuotientExpression(Expression expression)
    ;

    DD originalToQuotient(DD original);

    DD quotientToOriginal(DD quotient);
}
