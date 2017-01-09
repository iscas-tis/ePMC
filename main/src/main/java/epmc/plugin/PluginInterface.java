package epmc.plugin;

/**
 * Interface of classes to be loaded by plugin loader.
 * Other interfaces may extend this interface to allow to plugins to execute
 * tasks at specific occasions.
 * 
 * @author Ernst Moritz Hahn
 */
public interface PluginInterface {
    /**
     * Obtain unique identifier for the particular plugin interface
     * @return unique identifier for the particular plugin interface
     */
    String getIdentifier();
}
