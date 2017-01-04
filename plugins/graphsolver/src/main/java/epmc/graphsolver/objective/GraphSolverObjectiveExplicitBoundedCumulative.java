package epmc.graphsolver.objective;

import epmc.graph.explicit.GraphExplicit;
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
        this.result = ValueArrayAlgebra.asArrayAlgebra(result);
    }
    
    @Override
    public ValueArrayAlgebra getResult() {
        return result;
    }    
}
