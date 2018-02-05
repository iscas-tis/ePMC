package epmc.graphsolver.objective;

import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverObjectiveExplicitSteadyState implements GraphSolverObjectiveExplicit {
    private boolean computeScheduler;
    private boolean min;
    private GraphExplicit graph;
    private ValueArray result;
    private ValueArrayAlgebra stateRewards;
    private BitSet computeFor;

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

    public void setStateRewards(ValueArrayAlgebra stateRewards) {
        this.stateRewards = stateRewards;
    }

    public ValueArrayAlgebra getStateRewards() {
        return stateRewards;
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

    public void setComputeFor(BitSet computeFor) {
        this.computeFor = computeFor;
    }
    
    @Override
    public BitSet getComputeFor() {
        return computeFor;
    }
}
