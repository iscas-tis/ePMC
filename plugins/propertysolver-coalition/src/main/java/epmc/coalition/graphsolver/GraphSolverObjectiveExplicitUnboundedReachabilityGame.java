package epmc.coalition.graphsolver;

import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.SchedulerSimple;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.util.BitSet;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverObjectiveExplicitUnboundedReachabilityGame implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private boolean computeScheduler;
    private BitSet target;
    private ValueArrayAlgebra result;
    private SchedulerSimple scheduler;

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
    
    public void setTarget(BitSet target) {
        this.target = target;
    }
    
    public BitSet getTarget() {
        return target;
    }
    
    @Override
 	public void setResult(ValueArray result) {
        this.result = ValueArrayAlgebra.asArrayAlgebra(result);
    }
    
    @Override
	public ValueArrayAlgebra getResult() {
        return result;
    }
    
    public void setScheduler(SchedulerSimple scheduler) {
        this.scheduler = scheduler;
    }
    
    public SchedulerSimple getScheduler() {
        return scheduler;
    }
}
