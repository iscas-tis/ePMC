package epmc.constraintsolver;

public enum ConstraintType {
    LE,
    EQ,
    GE
    ;
    
    public boolean isLe() {
        return this == LE;
    }
    
    public boolean isEq() {
        return this == EQ;
    }
    public boolean isGE() {
        return this == GE;
    }
}
