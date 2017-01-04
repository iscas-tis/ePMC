package epmc.plugin;

import java.util.List;

/**
 * Class collections names of options for plugin part of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public enum OptionsPlugin {
    /** Resource identifier. */
    OPTIONS_PLUGIN,
    /** {@link List} of plugin filename {@link String}s. */
    PLUGIN,
    /** Filename specifying list of plugins to load. */
    PLUGIN_LIST_FILE,
    /** Stores plugin interface classes. */
    PLUGIN_INTERFACE_CLASS,
}
