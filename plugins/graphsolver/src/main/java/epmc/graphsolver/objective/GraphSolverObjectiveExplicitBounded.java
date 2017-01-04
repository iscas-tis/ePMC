package epmc.graphsolver.objective;

import java.util.List;

import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverObjectiveExplicitBounded implements GraphSolverObjectiveExplicit {
    private GraphExplicit graph;
    private boolean min;
    private List<BitSet> sinks;
    private ValueArrayAlgebra values;
    private Value time;
    private ValueArrayAlgebra result;

    @Override
    public void setGraph(GraphExplicit graph) {
        this.graph = graph;
    }
    
    @Override
    public GraphExplicit getGraph() {
        return graph;
    }
    
    public void setMin(boolean min) {
        this.min = min;
    }
    
    public boolean isMin() {
        return min;
    }
    
    public void setSinks(List<BitSet> sinks) {
        this.sinks = sinks;
    }
    
    public List<BitSet> getSinks() {
        return sinks;
    }

    public void setValues(ValueArrayAlgebra values) {
        this.values = values;
    }
    
    public ValueArrayAlgebra getValues() {
        return values;
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
}
