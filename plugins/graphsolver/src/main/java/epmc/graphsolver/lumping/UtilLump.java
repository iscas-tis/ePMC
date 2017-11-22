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

import java.util.Arrays;
import java.util.Collection;

import epmc.expression.Expression;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitLump;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

public final class UtilLump {
    public static GraphSolverObjectiveExplicitLump partitionByAPsObjective(GraphExplicit graph, Collection<Expression> atomics) {
        GraphSolverObjectiveExplicitLump result = new GraphSolverObjectiveExplicitLump();
        result.setGraph(graph);
        result.setAtomics(atomics);
        result.prepare();
        return result;
    }

    public static void fillGaps(int[] partition) {
        BitSet used = new BitSetUnboundedLongArray();
        for (int state = 0; state < partition.length; state++) {
            used.set(partition[state]);
        }
        int[] oldToNew = new int[used.length()];
        Arrays.fill(oldToNew, -1);
        int newIndex = 0;
        for (int oldIndex = used.nextSetBit(0); oldIndex >= 0; oldIndex
                = used.nextSetBit(oldIndex + 1)) {
            oldToNew[oldIndex] = newIndex;
            newIndex++;
        }
        for (int state = 0; state < partition.length; state++) {
            partition[state] = oldToNew[partition[state]];
        }
    }

    private UtilLump() {
    }
}
