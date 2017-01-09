package epmc.modelchecker;

import epmc.modelchecker.Engine;

// TODO move outside main part

/**
 * Explicit-state engine.
 * 
 * @author Ernst Moritz Hahn
 */
public enum EngineExplicit implements Engine {
    /** The singleton instance of this engine. */
    ENGINE_EXPLICIT;
    
    /** Unique identifier of the engine. */
    public final static String IDENTIFIER = "explicit";

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
        return ENGINE_EXPLICIT;
    }
}
