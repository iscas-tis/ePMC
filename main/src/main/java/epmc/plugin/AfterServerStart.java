package epmc.plugin;

import epmc.error.EPMCException;
import epmc.options.Options;

/**
 * Interface to perform tasks just after task server has started.
 * 
 * @author Ernst Moritz Hahn
 */
public interface AfterServerStart extends PluginInterface {
    /**
     * Performs a task just after server has started.
     * 
     * @param options options used in analysis
     * @throws EPMCException thrown in case of a problem occurring
     */
    void process(Options options) throws EPMCException;
}
