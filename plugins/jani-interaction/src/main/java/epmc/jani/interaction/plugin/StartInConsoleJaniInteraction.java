package epmc.jani.interaction.plugin;

import epmc.main.Analyse;
import epmc.main.LogCommandLine;
import epmc.main.RawModelLocalFiles;
import epmc.main.options.OptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.RawModel;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.StartInConsole;

public final class StartInConsoleJaniInteraction implements StartInConsole {
    private final static String IDENTIFIER = "start-in-console-jani-interaction";
    
    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    /**
     * Start the command to be executed with output shown in standard output.
     * The command to be executed will be read for {@link Options#COMMAND}.
     * Then, the client part of the command will be executed.
     * Afterwards, a task server will be created and the server part of the
     * command will be executed there.
     * The options parameter must not be {@code null}.
     * 
     * @param options options to use
     */
    // TODO use JANI for interaction
    @Override
    public void process(Options options) {
        assert options != null;
        if (options.getString(Options.COMMAND) == null) {
            System.out.println(options.getShortUsage());
            System.exit(1);
        }
        CommandTask command = UtilOptions.getInstance(options,
                OptionsEPMC.COMMAND_CLASS,
                Options.COMMAND);
        assert command != null;
        LogCommandLine log = new LogCommandLine(options);
        options.set(OptionsMessages.LOG, log);
        command.executeInClientBeforeServer();
        if (command.isRunOnServer()) {
            execute(options, log);
        }
        command.executeInClientAfterServer();
    }

    /**
     * Execute task on a new task server and print results.
     * The options parameter must not be {@code null}.
     * 
     * @param options options to use
     * @param log log to use
     */
    private static void execute(Options options, LogCommandLine log) {
        assert options != null;
        assert log != null;
        RawModel model = new RawModelLocalFiles(
                options.getStringList(OptionsEPMC.MODEL_INPUT_FILES).toArray(new String[0]),
                options.getStringList(OptionsEPMC.PROPERTY_INPUT_FILES).toArray(new String[0]));
        Analyse.execute(model, options, log);
        if (log.getException() != null) {
            throw log.getException();
        }
    }
}
