package epmc.sat;

import java.util.List;
import java.util.Set;

public class SatSolverImpl implements SatSolver {

    @Override
    public int new_var() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void addClause(List cls) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addClause(Lit p) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addClause(Lit p1, Lit p2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addClause(Lit p1, Lit p2, Lit p3) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean solve(List assums) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Lbool getModelValue(Lit p) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set getUnsatCore() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void pushvar() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void popvar() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFrozen(int v) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void unfreezeAll() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void preprocess() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int resetFreq() {
        // TODO Auto-generated method stub
        return 0;
    }

}
