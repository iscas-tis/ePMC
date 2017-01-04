package epmc.graphsolver;

import epmc.error.EPMCException;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;

/**
 * Class for solvers of one or several graph-based problems.
 * 
 * @author Ernst Moritz Hahn
 */
public interface GraphSolverExplicit {
    /**
     * Obtain unique identifier of this graph solver.
     * The result of this function should be user-readable as it will be used
     * to allow the user to choose between several available graph solvers.
     * 
     * @return unique identifier of this graph solver.
     */
    String getIdentifier();
    
    void setGraphSolverObjective(GraphSolverObjectiveExplicit objective);
    
    boolean canHandle();
    
    void solve() throws EPMCException;
}
