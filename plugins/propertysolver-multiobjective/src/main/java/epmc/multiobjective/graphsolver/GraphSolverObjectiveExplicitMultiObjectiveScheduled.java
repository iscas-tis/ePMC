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

package epmc.multiobjective.graphsolver;

import epmc.graph.Scheduler;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverObjectiveExplicitMultiObjectiveScheduled implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private boolean min;
    private ValueArrayAlgebra stopStateRewards;
    private Scheduler scheduler;
    private ValueArrayAlgebra transitionRewards;
    private ValueArrayAlgebra values;
    private ValueArrayAlgebra result;

    void setMin(boolean min) {
        this.min = min;
    }

    public boolean isMin() {
        return min;
    }

    public void setStopStateRewards(ValueArrayAlgebra stopStateRewards) {
        this.stopStateRewards = stopStateRewards;
    }

    public ValueArrayAlgebra getStopStateRewards() {
        return stopStateRewards;
    }

    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public void setTransitionRewards(ValueArrayAlgebra transitionRewards) {
        this.transitionRewards = transitionRewards;
    }

    public ValueArrayAlgebra getTransitionRewards() {
        return transitionRewards;
    }

    public void setValues(ValueArrayAlgebra values) {
        this.values = values;
    }

    public ValueArrayAlgebra getValues() {
        return values;
    }

    @Override
    public void setGraph(GraphExplicit graph) {
        this.graph = graph;
    }

    @Override
    public GraphExplicit getGraph() {
        return graph;
    }


    @Override
    public void setResult(ValueArray result) {
        this.result = ValueArrayAlgebra.as(result);
    }

    @Override
    public ValueArrayAlgebra getResult() {
        return result;
    }
}
