package epmc.modelchecker;

import epmc.modelchecker.Engine;

// TODO move outside main part

/**
 * Explorer-based engine.
 * This engine servers as the base for e.g. Monte-Carlo simulation or other
 * techniques where a state-based representation of the model is not required
 * or where management of nodes should be done completely manually.
 * 
 * @author Ernst Moritz Hahn
 */
public enum EngineExplorer implements Engine {
    /** The singleton instance of this engine. */
    ENGINE_EXPLORER;
    
    /** Unique identifier of the engine. */
    public final static String IDENTIFIER = "explorer";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
    
    /**
     * Obtain the singleton object of this engine.
     * 
     * @return singleton object of this engine
     */
    public static Engine getInstance() {
        return ENGINE_EXPLORER;
    }
}
