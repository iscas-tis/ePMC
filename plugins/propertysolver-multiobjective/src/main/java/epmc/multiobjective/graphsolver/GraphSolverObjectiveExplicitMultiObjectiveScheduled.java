package epmc.multiobjective.graphsolver;

import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverObjectiveExplicitMultiObjectiveScheduled implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private boolean computeScheduler;
    private boolean min;
    private ValueArrayAlgebra stopStateRewards;
    private Value scheduler;
    private ValueArrayAlgebra transitionRewards;
    private ValueArrayAlgebra values;
	private ValueArrayAlgebra result;

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

    public void setStopStateRewards(ValueArrayAlgebra stopStateRewards) {
        this.stopStateRewards = stopStateRewards;
    }
    
    public ValueArrayAlgebra getStopStateRewards() {
        return stopStateRewards;
    }

    public void setScheduler(Value scheduler) {
        this.scheduler = scheduler;
    }
    
    public Value getScheduler() {
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
        this.result = ValueArrayAlgebra.asArrayAlgebra(result);
    }
    
    @Override
	public ValueArrayAlgebra getResult() {
        return result;
    }
}
