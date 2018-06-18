package epmc.jani.interaction.commandline;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import epmc.jani.interaction.communication.SimpleAsynchronous;
import epmc.jani.interaction.plugin.AfterCommandLineOptionsCreation;
import epmc.options.Options;
import epmc.plugin.OptionsPlugin;
import epmc.plugin.PluginInterface;
import epmc.plugin.StartInConsole;
import epmc.plugin.UtilPlugin;
import epmc.util.Util;

public final class StartInConsoleJaniInteractionJani implements StartInConsole {
    private final static String IDENTIFIER = "start-in-console-jani-interaction";
    /** Empty string. */
    private final static String EMPTY = "";
    private final static String COMMA = ",";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Start the command to be executed with output shown in standard output.
     * The command to be executed will be read for {@link Options#COMMAND}.
     * Then, the client part of the command will be executed. Afterwards, a task
     * server will be created and the server part of the command will be
     * executed there. The options parameter must not be {@code null}.
     * 
     * @param options
     *            options to use
     */
    @Override
    public void process(String[] args, List<Class<? extends PluginInterface>> plugins) {
        if (true) return;
        assert args != null;
        for (String arg : args) {
            assert arg != null;
        }
        CommandLineOptions options = new CommandLineOptions();
        addPluginOptions(options);
        options.setIgnoreUnknownOptions(true);
        options.setIgnoreUnknownCommands(true);
        options.parse(args);
        for (Class<? extends AfterCommandLineOptionsCreation> clazz : UtilPlugin.getPluginInterfaceClasses(plugins, AfterCommandLineOptionsCreation.class)) {
            AfterCommandLineOptionsCreation instance = Util.getInstance(clazz);
            instance.process(options);
        }
        options.clearValues();
        options.setIgnoreUnknownCommands(false);
        options.parse(args);
        if (options.getCommand() == null) {
            System.out.println(options.getShortUsage());
            System.exit(1);
        }
        SimpleAsynchronous backendInterface = new SimpleAsynchronous(plugins);
        CommandJANIClient.Builder builder = options.getCommandJANI();
        CommandJANIClient command = builder
                .setArgs(args)
                .setBackend(backendInterface)
                .setClient(this)
                .setOptions(options)
                .build();
        backendInterface.setClient(command);
        backendInterface.start();
    }

    private void addPluginOptions(CommandLineOptions options) {
        assert options != null;
        options.addOption(new CommandLineOption.Builder()
                .setBundleName(OptionsPlugin.OPTIONS_PLUGIN)
                .setIdentifier(OptionsPlugin.PLUGIN)
                .build());
        options.addOption(new CommandLineOption.Builder()
                .setBundleName(OptionsPlugin.OPTIONS_PLUGIN)
                .setIdentifier(OptionsPlugin.PLUGIN_LIST_FILE)
                .build());
    }

    private static List<String> getPluginList(CommandLineOptions options) {
        assert options != null;
        List<String> result = new ArrayList<>();

        /* Read external plugins from plugin files list. */
        String pluginsListFilename = options.get(OptionsPlugin.PLUGIN_LIST_FILE).toString();
        if (pluginsListFilename != null) {
            Path pluginsListPath = Paths.get(pluginsListFilename);
            List<String> pluginsFromListFile = UtilPlugin.readPluginList(pluginsListPath);
            result.addAll(pluginsFromListFile);
        }

        /* Read plugins from option (command line).  */
        String plugins = options.get(OptionsPlugin.PLUGIN).toString();
        if (plugins == null) {
            plugins = EMPTY;
        }
        for (String plugin : plugins.split(COMMA)) {
            if (plugin.equals(EMPTY)) {
                continue;
            }
            result.add(plugin);
        }

        return result;
    }
}
