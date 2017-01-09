package epmc.graphsolver.objective;

import epmc.graph.explicit.GraphExplicit;
import epmc.value.ValueArray;

public interface GraphSolverObjectiveExplicit {
    void setGraph(GraphExplicit graph);
    
    GraphExplicit getGraph();
    
    void setResult(ValueArray result);
    
    ValueArray getResult();
}
