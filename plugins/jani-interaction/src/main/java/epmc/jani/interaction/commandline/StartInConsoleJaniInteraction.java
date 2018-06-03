package epmc.jani.interaction.commandline;

import java.text.MessageFormat;
import java.util.Locale;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.main.Analyse;
import epmc.main.LogCommandLine;
import epmc.main.RawModelLocalFiles;
import epmc.main.options.OptionsEPMC;
import epmc.main.options.UtilOptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.RawModel;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.OptionsPlugin;
import epmc.plugin.StartInConsole;
import epmc.plugin.UtilPlugin;

public final class StartInConsoleJaniInteraction implements StartInConsole {
    private final static String IDENTIFIER = "start-in-console-jani-interaction";
    /** Empty string. */
    private final static String EMPTY = "";
    private final boolean janiMode = false;

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
    // TODO use JANI for interaction
    @Override
    public void process(String[] args) {
        assert args != null;
        for (String arg : args) {
            assert arg != null;
        }
        if (janiMode) {
            processJaniMode(args);
        } else {
            processNonJaniMode(args);
        }
    }

    private void processNonJaniMode(String[] args) {
        Options options = null;
        try {
            options = prepareOptions(args);
            Options.set(options);
            if (options.getString(Options.COMMAND) == null) {
                System.out.println(options.getShortUsage());
                System.exit(1);
            }
            CommandTask command = UtilOptions.getInstance(options, OptionsEPMC.COMMAND_CLASS, Options.COMMAND);
            assert command != null;
            LogCommandLine log = new LogCommandLine(options);
            options.set(OptionsMessages.LOG, log);
            command.executeInClientBeforeServer();
            if (command.isRunOnServer()) {
                execute(options, log);
            }
            command.executeInClientAfterServer();
        } catch (EPMCException e) {
            handleException(e, options);
        }
    }

    /**
     * Execute task on a new task server and print results. The options
     * parameter must not be {@code null}.
     * 
     * @param options
     *            options to use
     * @param log
     *            log to use
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

    /**
     * Prepare options from command line arguments. The command line arguments
     * parameters must not be {@code null} and must not contain {@code null}
     * entries.
     * 
     * @param args
     *            command line arguments
     * @return options parsed from command line arguments
     */
    private static Options prepareOptions(String[] args) {
        assert args != null;
        for (String arg : args) {
            assert arg != null;
        }
        Locale locale = Locale.getDefault();
        Options options = UtilOptionsEPMC.newOptions();
        options.parseOptions(args, true);
        options.reset();
        UtilPlugin.loadPlugins(options);
        options.getOption(OptionsPlugin.PLUGIN).reset();
        options.getOption(OptionsPlugin.PLUGIN_LIST_FILE).reset();
        options.parseOptions(args, false);
        options.set(OptionsEPMC.LOCALE, locale);
        return options;
    }

    private static void handleException(EPMCException e, Options options) {
        assert e != null;
        assert options != null;
        String message = e.getProblem().getMessage(options.getLocale());
        MessageFormat formatter = new MessageFormat(EMPTY);
        formatter.applyPattern(message);
        String formattedMessage = formatter.format(e.getArguments(), new StringBuffer(), null).toString();
        Positional positional = e.getPositional();
        if (positional != null) {
            if (positional.getContent() != null) {
                System.err.print(positional.getContent());
                if (positional.getPart() != null
                        || positional.getLine() > 0
                        || positional.getColumn() > 0) {
                    System.err.print(", ");
                }
            }
            if (positional.getPart() != null) {
                System.err.print("part: " + positional.getPart());
                if (positional.getLine() > 0 || positional.getColumn() > 0) {
                    System.err.print(", ");
                }
            }
            if (positional.getLine() > 0) {
                System.err.print("line: " + positional.getLine());
                if (positional.getColumn() > 0) {
                    System.err.print(", ");
                }                    
            }
            if (positional.getColumn() > 0) {
                System.err.print("column: " + positional.getColumn());
            }
            if (positional.getContent() != null
                    || positional.getPart() != null
                    || positional.getLine() > 0
                    || positional.getColumn() > 0) {
                System.err.print(": ");
            }
        }
        System.err.println(formattedMessage);
        if (options == null || options.getBoolean(OptionsEPMC.PRINT_STACKTRACE)) {
            e.printStackTrace();
        }
        System.exit(1);
    }
    
    private void processJaniMode(String[] args) {
        Options options = null;
        options = prepareOptions(args);
        Options.set(options);
        if (options.getString(Options.COMMAND) == null) {
            System.out.println(options.getShortUsage());
            System.exit(1);
        }
        setupConnection(args);
        if (options.getString(Options.COMMAND).equals("help")) {
            JsonObjectBuilder request = Json.createObjectBuilder();
            request.add("jani-versions", Json.createArrayBuilder().add(1));
            send(request.build());
            JsonValue result = getPending();
            // TODO Auto-generated method stub
        }

    }

    private void setupConnection(String[] args) {
        
        // TODO Auto-generated method stub
        
    }

    private int numPending() {
        return -1;
    }
    
    private JsonValue getPending() {
        return null;
    }

    private void send(JsonValue value) {

    }
}
