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

package epmc.lumpingdd;

import java.util.List;

import epmc.dd.DD;
import epmc.dd.VariableDD;
import epmc.expression.Expression;
import epmc.graph.dd.GraphDD;
import epmc.lumpingdd.transrepresentation.TransitionRepresentation;

public interface Signature {
    /**
     * Set the original graph that the lumper works on
     * The signature can use this to decide whether it can
     * lump.
     */
    public void setOriginal(GraphDD original);

    /**
     * Decides whether this class can lump the original graph
     */
    public boolean canLump(List<Expression> validFor);

    /**
     * Set the variable that defines the block indices
     */
    public void setBlockIndexVar(VariableDD partitionsVar);

    /**
     * Compute the signatures for the given partition
     * @param partitions The current partition
     * @return A DD defining the new signature for each state
     */
    public DD computeSignatures(DD partitions);

    /**
     * Get the transition representation associated with
     * this signature.
     */
    public TransitionRepresentation getTransitionRepresentation();
}
