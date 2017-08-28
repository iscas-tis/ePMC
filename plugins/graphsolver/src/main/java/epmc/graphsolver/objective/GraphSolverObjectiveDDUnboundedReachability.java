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

package epmc.graphsolver.objective;

import java.util.List;

import epmc.dd.DD;
import epmc.graph.dd.GraphDD;

public final class GraphSolverObjectiveDDUnboundedReachability implements GraphSolverObjectiveDD {
    private GraphDD graph;
    private DD target;
    private boolean min;
    private List<DD> sinks;

    @Override
    public void setGraph(GraphDD graph) {
        this.graph = graph;
    }

    @Override
    public GraphDD getGraph() {
        return graph;
    }

    public void setMin(boolean min) {
        this.min = min;
    }

    public boolean isMin() {
        return min;
    }

    public void setTarget(DD target) {
        this.target = target;
    }

    public DD getTarget() {
        return target;
    }

    public void setSinks(List<DD> sinks) {
        this.sinks = sinks;
    }

    public List<DD> getSinks() {
        return sinks;
    }
}
