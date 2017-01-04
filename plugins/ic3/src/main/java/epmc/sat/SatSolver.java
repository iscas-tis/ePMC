package epmc.sat;

import java.util.List;
import java.util.Set;

public interface SatSolver {
    
    int new_var();
    void addClause(List cls);
    void addClause(Lit p);
    void addClause(Lit p1, Lit p2);
    void addClause(Lit p1, Lit p2, Lit p3);
    boolean solve(List assums);
    Lbool getModelValue(Lit p);
    Set getUnsatCore();

    void pushvar();
    void popvar();

    void setFrozen(int v);
    void unfreezeAll();
    void preprocess();

    int resetFreq();

}
