package epmc.graphsolver.objective;

import java.util.List;

import epmc.dd.DD;
import epmc.graph.dd.GraphDD;

public final class GraphSolverObjectiveDDUnboundedReachability implements GraphSolverObjectiveDD {
    private GraphDD graph;
    private DD target;
    private boolean min;
    private List<DD> sinks;

    @Override
    public void setGraph(GraphDD graph) {
        this.graph = graph;
    }

    @Override
    public GraphDD getGraph() {
        return graph;
    }
    
    public void setMin(boolean min) {
        this.min = min;
    }
    
    public boolean isMin() {
        return min;
    }
    
    public void setTarget(DD target) {
        this.target = target;
    }
    
    public DD getTarget() {
        return target;
    }

    public void setSinks(List<DD> sinks) {
        this.sinks = sinks;
    }
    
    public List<DD> getSinks() {
        return sinks;
    }
}
