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

import java.util.Collection;

public enum OptionsGraphsolver {
    OPTIONS_GRAPHSOLVER,
    GRAPHSOLVER_CATEGORY,

    GRAPHSOLVER_PREPROCESSOR_EXPLICIT,
    GRAPHSOLVER_PREPROCESSOR_EXPLICIT_CLASS,

    /** {@link Collection} of {@link String} of graph solver identifiers to try to solve graph problems */
    GRAPHSOLVER_SOLVER,
    GRAPHSOLVER_SOLVER_CLASS,

    /** whether to perform lumping before calling a graph solver */
    GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING,

    GRAPHSOLVER_LUMPER_EXPLICIT_CLASS,
    GRAPHSOLVER_LUMPER_EXPLICIT,

    GRAPHSOLVER_LUMPER_DD,
    GRAPHSOLVER_DD_LUMPER_CLASS,
    
    GRAPHSOLVER_UPDATE_DELAY,
}
