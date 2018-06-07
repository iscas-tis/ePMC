package epmc.jani.interaction.plugin;

import epmc.jani.interaction.commandline.CommandLineOptions;
import epmc.plugin.PluginInterface;

public interface AfterCommandLineOptionsCreation extends PluginInterface {
    void process(CommandLineOptions options);
}
