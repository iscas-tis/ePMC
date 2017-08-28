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

package epmc.lumping.lumpingexplicitsignature;

import java.util.List;

import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.value.ValueArray;

interface Equivalence {
    // TODO following methods group should be removed

    void setSuccessorsFromTo(int[] successorsFromTo);

    void setSuccessorStates(int[] successorStates);

    void setSuccessorWeights(ValueArray weights);

    void setPrecessorsFromTo(int[] predecessorsFromTo);

    void setPrecessorStates(int[] predecessorStates);


    void prepare();

    void prepareInitialPartition(int[] partition);

    List<int[]> splitBlock(int[] block, int[] partition)
    ;

    GraphExplicit computeQuotient(int[] originalToQuotientState,
            List<int[]> blocks);

    void setObjective(GraphSolverObjectiveExplicit objective);

    boolean canHandle();
}
