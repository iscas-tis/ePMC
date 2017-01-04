package epmc.graphsolver.objective;

import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.Scheduler;
import epmc.util.BitSet;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverObjectiveExplicitUnboundedReachability implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private boolean computeScheduler;
    private boolean min;
    private BitSet target;
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
        
    public void setScheduler(Scheduler computeScheduler2) {
        // TODO Auto-generated method stub
        
    }

	public void setZeroSink(BitSet zeroSet) {
		this.zeroSet = zeroSet;
	}
	
	public BitSet getZeroSet() {
		return zeroSet;
	}
}
