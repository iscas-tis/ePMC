package epmc.graphsolver.objective;

import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverObjectiveExplicitBoundedReachability implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private boolean computeScheduler;
    private boolean min;
    private BitSet targets;
    private Value time;
    private ValueArrayAlgebra result;
	private BitSet zeroSet;

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
    
    public void setTargets(BitSet targets) {
        this.targets = targets;
    }
    
    public BitSet getTarget() {
        return targets;
    }

    public void setTime(Value time) {
        this.time = time;
    }
    
    public Value getTime() {
        return time;
    }
    
    
    @Override
    public void setResult(ValueArray result) {
        this.result = ValueArrayAlgebra.asArrayAlgebra(result);
    }
    
    @Override
    public ValueArrayAlgebra getResult() {
        return result;
    }
    
	public void setZeroSink(BitSet zeroSet) {
		this.zeroSet = zeroSet;
	}
	
	public BitSet getZeroSet() {
		return zeroSet;
	}
}
