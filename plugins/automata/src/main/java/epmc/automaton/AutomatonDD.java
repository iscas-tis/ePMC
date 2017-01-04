package epmc.automaton;

import java.io.Closeable;
import java.util.List;

import epmc.dd.ContextDD;
import epmc.dd.DD;

// TODO probably should also extend GraphDD

public interface AutomatonDD extends Closeable {
    DD getInitial();
    
    DD getTransitions();
    
    List<DD> getPresVars();
    
    List<DD> getNextVars();
    
    List<DD> getLabelVars();
    
    DD getPresCube();
    
    DD getNextCube();
    
    DD getLabelCube();
    
    @Override
    public void close();
    
    
    default ContextDD getContextDD() {
        return getPresCube().getContext();
    }
}
