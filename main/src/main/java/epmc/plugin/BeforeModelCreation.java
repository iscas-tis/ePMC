package epmc.plugin;

import epmc.error.EPMCException;
import epmc.value.ContextValue;

/**
 * Interface to perform tasks just before model creation on the server.
 * 
 * @author Ernst Moritz Hahn
 */
public interface BeforeModelCreation extends PluginInterface {
    
    /**
     * Performs a before model creation on server.
     * 
     * @param contextValue value context used in analysis
     * @throws EPMCException thrown in case of a problem occurring
     */
    void process(ContextValue contextValue) throws EPMCException;
}
