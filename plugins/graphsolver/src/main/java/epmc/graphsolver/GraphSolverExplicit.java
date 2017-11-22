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

import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;

/**
 * Class for solvers of one or several graph-based problems.
 * 
 * @author Ernst Moritz Hahn
 */
public interface GraphSolverExplicit {
    /**
     * Obtain unique identifier of this graph solver.
     * The result of this function should be user-readable as it will be used
     * to allow the user to choose between several available graph solvers.
     * 
     * @return unique identifier of this graph solver.
     */
    String getIdentifier();

    void setGraphSolverObjective(GraphSolverObjectiveExplicit objective);

    boolean canHandle();

    void solve();
}
