package epmc.plugin;

import epmc.error.EPMCException;
import epmc.value.ContextValue;

/**
 * Interface to perform tasks after a command has been executed on the server.
 * 
 * @author Ernst Moritz Hahn
 */
public interface AfterCommandExecution extends PluginInterface {
    /**
     * Performs a task after a command has been executed on the server.
     * 
     * @param contextValue value context used in analysis
     * @throws EPMCException thrown in case of a problem occurring
     */
    void process(ContextValue contextValue) throws EPMCException;
}
