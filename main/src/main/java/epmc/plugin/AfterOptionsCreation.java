package epmc.plugin;

import epmc.error.EPMCException;
import epmc.options.Options;

/**
 * Interface to perform tasks after {@link Options} creation on client.
 * 
 * @author Ernst Moritz Hahn
 */
public interface AfterOptionsCreation extends PluginInterface {
    /**
     * Performs a task after {@link Options} creation on client.
     * 
     * @param options options used in analysis
     * @throws EPMCException thrown in case of a problem occurring
     */
    void process(Options options) throws EPMCException;
}
