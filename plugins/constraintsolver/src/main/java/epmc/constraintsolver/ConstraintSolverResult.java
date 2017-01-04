package epmc.constraintsolver;

public enum ConstraintSolverResult {
    SAT,
    UNSAT,
    UNKNOWN;
    
    public boolean isSat() {
        return this == SAT;
    }
    
    public boolean isUnsat() {
        return this == UNSAT;
    }
    
    public boolean isUnknown() {
        return this == UNKNOWN;
    }
}
