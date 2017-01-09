package epmc.modelchecker;

import epmc.modelchecker.Engine;

// TODO move outside main part

/**
 * Decision-diagram-based engine.
 * 
 * @author Ernst Moritz Hahn
 */
public enum EngineDD implements Engine {
    /** The singleton instance of this engine. */
    ENGINE_DD;

    /** Unique identifier of the engine. */
    public final static String IDENTIFIER = "dd";
    
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
        return ENGINE_DD;
    }
}
