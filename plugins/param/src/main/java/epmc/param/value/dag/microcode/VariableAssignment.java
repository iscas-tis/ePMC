package epmc.param.value.dag.microcode;

final class VariableAssignment {
    private final int numVariables;
    private final int[] assignments;
    private final int[] nodeToIndex;
    
    VariableAssignment(int numVariables, int[] assignments, int[] nodeToIndex) {
        this.numVariables = numVariables;
        this.assignments = assignments;
        this.nodeToIndex = nodeToIndex;
    }
    
    int getNumVariables() {
        return numVariables;
    }
    
    int getAssignedTo(int node) {
        return assignments[nodeToIndex[node]];
    }
}
