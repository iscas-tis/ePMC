package epmc.graphsolver.objective;

import epmc.graph.explicit.GraphExplicit;
import epmc.value.ValueArray;

public final class GraphSolverObjectiveExplicitLump implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private int[] partition;

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
    }

    @Override
    public ValueArray getResult() {
        return null;
    }
    
    public void setPartition(int[] partition) {
        this.partition = partition;
    }
    
    public int[] getPartition() {
        return partition;
    }
}
