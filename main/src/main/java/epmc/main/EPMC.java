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
import epmc.graph.LowLevel;
import epmc.graph.Scheduler;
import epmc.main.options.OptionsEPMC;
import epmc.main.options.UtilOptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.RawModel;
import epmc.modelchecker.RawProperty;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.OptionsPlugin;
import epmc.plugin.UtilPlugin;
import epmc.util.Util;

/**
 * Main class of EPMC.
 * 
 * @author Ernst Moritz Hahn
 */
public final class EPMC {
    /** Empty string. */
    private final static String EMPTY = "";
    /** String ": ".*/
    private final static String SPACE_COLON = ": ";
    
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
        try {
            options = prepareOptions(args);
            startInConsole(options);
        } catch (EPMCException e) {
            String message = e.getProblem().getMessage(options.getLocale());
            MessageFormat formatter = new MessageFormat(EMPTY);
            formatter.applyPattern(message);
            String formattedMessage = formatter.format(e.getArguments(), new StringBuffer(), null).toString();
            System.err.println(formattedMessage);
            if (options == null || options.getBoolean(OptionsEPMC.PRINT_STACKTRACE)) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }
    
    /**
     * Prepare options from command line arguments.
     * The command line arguments parameters must not be {@code null} and must
     * not contain {@code null} entries.
     * 
     * @param args command line arguments
     * @return options parsed from command line arguments
     * @throws EPMCException thrown in case of problems
     */
    private static Options prepareOptions(String[] args) throws EPMCException {
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
     * @throws EPMCException thrown in case of problems
     */
    private static void startInConsole(Options options) throws EPMCException {
        assert options != null;
        if (options.getString(Options.COMMAND) == null) {
            System.out.println(options.getShortUsage());
            System.exit(1);
        }
        CommandTask command = UtilOptions.getInstance(options,
                OptionsEPMC.COMMAND_CLASS,
                Options.COMMAND);
        assert command != null;
        command.setOptions(options);
        LogCommandLine log = new LogCommandLine(options);
        options.set(OptionsMessages.LOG, log);
        command.executeOnClient();
        if (command.isRunOnServer()) {
            execute(options, log);
        }
    }

    /**
     * Execute task on a new task server and print results.
     * The options parameter must not be {@code null}.
     * 
     * @param options options to use
     * @param log2 
     * @throws EPMCException thrown in case of problems
     */
    private static void execute(Options options, LogCommandLine log) throws EPMCException {
        assert options != null;
        RawModel model = new RawModelLocalFiles(
                options.getStringList(OptionsEPMC.MODEL_INPUT_FILES).toArray(new String[0]),
                options.getStringList(OptionsEPMC.PROPERTY_INPUT_FILES).toArray(new String[0]));
        Analyse.execute(model, options, log);
        if (log.getException() != null) {
            throw log.getException();
        }
        printResults(options, log);
    }

    /**
     * Print model checking result to command line.
     * The options and result parameters must not be {@code null}.
     * 
     * @param options options to use
     * @param log log used
     * @throws EPMCException 
     */
    private static void printResults(Options options, LogCommandLine log) throws EPMCException {
        assert options != null;
        assert log != null;
        for (RawProperty property : log.getProperties()) {
            String exprString = property.getDefinition();
            Object propResult = log.get(property);
            if (propResult == null) {
                continue;
            }
            String resultString = null;
            if (propResult instanceof EPMCException) {
                EPMCException e = (EPMCException) propResult;
                String message = e.getProblem().getMessage(options.getLocale());
                MessageFormat formatter = new MessageFormat(message);
                formatter.applyPattern(message);
                resultString = formatter.format(e.getArguments());
                if (options == null || options.getBoolean(OptionsEPMC.PRINT_STACKTRACE)) {
                    e.printStackTrace();
                }
            } else {
                resultString = propResult.toString();
            }
            System.out.println(exprString + SPACE_COLON + resultString);
            Scheduler scheduler = log.getScheduler(property);
            LowLevel lowLevel = log.getLowLevel(property);
            if (scheduler != null) {
            	Util.printScheduler(System.out, lowLevel, scheduler, options);
            }
        }
        if (log.getCommonResult() != null) {
            System.out.println(log.getCommonResult().toString());
        }
    }
}
