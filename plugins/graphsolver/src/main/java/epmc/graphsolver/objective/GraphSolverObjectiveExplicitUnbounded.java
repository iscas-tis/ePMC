package epmc.graphsolver.objective;

import epmc.graph.explicit.GraphExplicit;
import epmc.value.ValueArray;

public final class GraphSolverObjectiveExplicitUnbounded implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private boolean computeScheduler;
    private boolean min;
    private ValueArray result;

    public void setComputeScheduler(boolean computeScheduler) {
        this.computeScheduler = computeScheduler;
    }
    
    public boolean isComputeScheduler() {
        return computeScheduler;
    }
    
    void setMin(boolean min) {
        this.min = min;
    }
    
    public boolean isMin() {
        return min;
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
        this.result = result;
    }
    
    @Override
    public ValueArray getResult() {
        return result;
    }    
}
