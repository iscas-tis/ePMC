/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.main;

import java.text.MessageFormat;
import java.util.Locale;

import epmc.error.EPMCException;
import epmc.error.Positional;
import epmc.main.options.OptionsEPMC;
import epmc.main.options.UtilOptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.RawModel;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.OptionsPlugin;
import epmc.plugin.UtilPlugin;

/**
 * Main class of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public final class EPMC {
    /** Empty string. */
    private final static String EMPTY = "";

    /**
     * The {@code main} entry point of EPMC.
     * 
     * @param args parameters of EPMC.
     */
    public static void main(String[] args) {
        assert args != null;
        for (String arg : args) {
            assert arg != null;
        }
        Options options = UtilOptionsEPMC.newOptions();
        Options.set(options);
        try {
            options = prepareOptions(args);
            Options.set(options);
            startInConsole(options);
        } catch (EPMCException e) {
            handleException(e, options);
        }
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

    /**
     * Prepare options from command line arguments.
     * The command line arguments parameters must not be {@code null} and must
     * not contain {@code null} entries.
     * 
     * @param args command line arguments
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
    private static void startInConsole(Options options) {
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
