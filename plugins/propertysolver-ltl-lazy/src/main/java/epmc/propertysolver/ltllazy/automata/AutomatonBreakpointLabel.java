package epmc.propertysolver.ltllazy.automata;

public interface AutomatonBreakpointLabel {
    boolean isNeutral();
    
    boolean isAccepting();
    
    boolean isRejecting();
}
