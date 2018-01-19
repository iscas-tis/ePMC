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

import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverObjectiveExplicitBoundedCumulative implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private boolean computeScheduler;
    private boolean min;
    private Value time;
    private ValueArrayAlgebra stateRewards;
    private ValueArrayAlgebra result;
    private BitSet computeFor;

    @Override
    public void setGraph(GraphExplicit graph) {
        this.graph = graph;
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }

    public void setComputeScheduler(boolean computeScheduler) {
        this.computeScheduler = computeScheduler;
    }

    public boolean isComputeScheduler() {
        return computeScheduler;
    }

    public void setMin(boolean min) {
        this.min = min;
    }

    public boolean isMin() {
        return min;
    }

    public void setTime(Value time) {
        this.time = time;
    }

    public Value getTime() {
        return time;
    }

    public void setStateRewards(ValueArrayAlgebra stateRewards) {
        this.stateRewards = stateRewards;
    }

    public ValueArrayAlgebra getStateRewards() {
        return stateRewards;
    }


    @Override
    public void setResult(ValueArray result) {
        this.result = ValueArrayAlgebra.as(result);
    }

    @Override
    public ValueArrayAlgebra getResult() {
        return result;
    }    

    void setComputeFor(BitSet computeFor) {
        this.computeFor = computeFor;
    }
    
    @Override
    public BitSet getComputeFor() {
        return computeFor;
    }
}
