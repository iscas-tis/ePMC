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

package epmc.graph;

import epmc.error.Problem;
import epmc.error.UtilError;

/**
 * Class collecting problems potentially occurring in graph module.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ProblemsGraph {
    /** Base name of resource file containing module problem descriptions. */
    private final static String ERROR_GRAPH = "ProblemsGraph";

    /** Chosen explorer node storage type is too small for given model. */
    public final static Problem STATE_DS_TOO_SMALL = newProblem("state-ds-too-small");
    /** Chosen successor storage type is too small for given model. */
    public final static Problem WRAPPER_GRAPH_SUCCESSORS_SIZE_TOO_SMALL = newProblem("wrapper-graph-successors-size-too-small");

    /**
     * Create new problem object using module resource file.
     * The name parameter must not be {@code null}.
     * 
     * @param name problem identifier String
     * @return newly created problem identifier
     */
    private static Problem newProblem(String name) {
        assert name != null;
        return UtilError.newProblem(ERROR_GRAPH, name);
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private ProblemsGraph() {
    }
}
