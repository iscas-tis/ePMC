package epmc.lumping.lumpingexplicitsignature;

import java.util.List;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.value.ValueArray;

interface Equivalence {
    // TODO following methods group should be removed
    
    void setSuccessorsFromTo(int[] successorsFromTo);
    
    void setSuccessorStates(int[] successorStates);
    
    void setSuccessorWeights(ValueArray weights);
    
    void setPrecessorsFromTo(int[] predecessorsFromTo);
    
    void setPrecessorStates(int[] predecessorStates);

    
    void prepare() throws EPMCException;

    void prepareInitialPartition(int[] partition);
    
    List<int[]> splitBlock(int[] block, int[] partition)
            throws EPMCException;

    GraphExplicit computeQuotient(int[] originalToQuotientState,
            List<int[]> blocks) throws EPMCException;

	void setObjective(GraphSolverObjectiveExplicit objective);
	
    boolean canHandle();
}
