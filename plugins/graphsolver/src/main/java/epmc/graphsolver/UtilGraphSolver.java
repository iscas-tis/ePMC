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

package epmc.graphsolver;

import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;

/**
 * Collection of static methods for graph solvers.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UtilGraphSolver {
    /**
     * Create new explicit-state graph solver configuration.
     * 
     * @return graph solver configuration created
     */
    public static GraphSolverConfigurationExplicit newGraphSolverConfigurationExplicit() {
        return new GraphSolverConfigurationExplicit();
    }

    public static void solve(GraphSolverObjectiveExplicit objective) {
        assert objective != null;
        GraphSolverConfigurationExplicit configuration = newGraphSolverConfigurationExplicit();
        configuration.setObjective(objective);
        configuration.solve();
    }

    /**
     * Create new DD-based sstate graph solver configuration.
     * 
     * @return graph solver configuration created
     */
    public static GraphSolverConfigurationDD newGraphSolverConfigurationDD() {
        return new GraphSolverConfigurationDD();
    }

    public static BitSet map(int size, StateMap map, BitSet original) {
        assert size >= 0;
        assert map != null;
        if (original == null) {
            return null;
        }
        BitSet result = new BitSetBoundedLongArray(size);

        // TODO HACK to make sigref based lumper work
        if (size == 0) {
            return result;
        }
        for (int entryNr = 0; entryNr < size; entryNr++) {
            result.set(entryNr, original.get(map.map(entryNr)));
        }
        return result;
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private UtilGraphSolver() {
    }
}
