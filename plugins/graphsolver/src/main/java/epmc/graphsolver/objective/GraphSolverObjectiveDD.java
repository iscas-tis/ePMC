package epmc.graphsolver.objective;

import epmc.graph.dd.GraphDD;

public interface GraphSolverObjectiveDD {
    void setGraph(GraphDD graph);
    
    GraphDD getGraph();
}
