package epmc.imdp.bio;

import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverObjectiveSteadyStateStateOnly implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private ValueArray result;
    private ValueArrayAlgebra rewards;
    private boolean min;

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
        this.result = result;
    }

    @Override
    public ValueArray getResult() {
        return result;
    }

    public void setRewards(ValueArrayAlgebra rewards) {
        this.rewards = rewards;
    }
    
    public ValueArrayAlgebra getRewards() {
        return rewards;
    }
    
    public void setMin(boolean min) {
        this.min = min;
    }
    
    public boolean isMin() {
        return min;
    }
}
