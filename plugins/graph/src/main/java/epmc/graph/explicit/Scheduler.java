package epmc.graph.explicit;

import epmc.value.ContextValue;

/**
 * Interface to represent a scheduler.
 * The type of scheduler depends on the subinterface of this class.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Scheduler {
    /** Value representing an unset decision. */
    static int UNSET = -1;
    
    /* methods to be implemented by implementing classes */
    
    /**
     * Get graph to which this scheduler belongs.
     * 
     * @return graph to which this scheduler belongs
     */
    GraphExplicit getGraph();
    
    Scheduler clone();
    
    
    /* default methods */
    
    default ContextValue getContextValue() {
        return getGraph().getContextValue();
    }
}
