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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.graph.LowLevel;
import epmc.graph.Scheduler;
import epmc.messages.Message;
import epmc.messages.OptionsMessages;
import epmc.messages.UtilMessages;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelCheckerResult;
import epmc.modelchecker.RawProperty;
import epmc.options.Options;
import epmc.util.StopWatch;

/**
 * Log printing output to command line.
 * Messages sent are printed to the command line.
 * Results and exceptions are stored to be retrieved later using according
 * methods.
 * 
 * @author Ernst Moritz Hahn
 */
public final class LogCommandLine implements Log {
    /** Empty string. */
    private final static String EMPTY = "";
    /** String containing a single space. */
    private final static String SPACE = " ";
    /** Formatter used for printing. */
    private final static MessageFormat formatter = new MessageFormat(EMPTY);
    /** Options used. */
    private final Options options;
    /** Stop watch measuring time since creation of the log. */
    private final StopWatch watch = new StopWatch(true);
    /** Whether the log should not print messages for the moment. */
    private boolean silent;
    /** Exception thrown. */
    private EPMCException exception;    
    /**
     * Whether message should be output in human-readable format.
     * If yes, full sentences will be generated.
     * If not, the message type identifier followed by its parameters will be
     * printed.
     */
    private boolean translate;
    /** Time at which the log has been created, ms since epoche time. */
    private long timeStarted;
    /** Result common to all properties. */
    private Object commonResult;
    /** Map from properties to results computed for them. */
    private final Map<RawProperty, Object> results = new LinkedHashMap<>();
    private final Map<RawProperty, Scheduler> schedulers = new LinkedHashMap<>();
    private final Map<RawProperty, LowLevel> lowLevels = new LinkedHashMap<>();
    /**
     * Create new command line log.
     * The options parameter must not be {@code null}.
     * 
     * @param options options to use
     */
    public LogCommandLine(Options options) {
        assert options != null;
        this.options = options;
        translate = options.getBoolean(OptionsMessages.TRANSLATE_MESSAGES);
        timeStarted = System.currentTimeMillis();
    }

    @Override
    public void send(Message message, Object... parameters) {
        if (silent) {
            return;
        }
        for (Object param : parameters) {
            assert param != null;
        }
        long time = watch.getTime();
        System.out.print(translateTimeStamp(time));
        if (translate) {
            Locale locale = this.options.getLocale();
            formatter.applyPattern(message.getMessage(locale));
            System.out.println(formatter.format(parameters));
        } else {
            System.out.print(message);
            for (Object argument : parameters) {
                System.out.print(SPACE + argument);
            }
            System.out.println();
        }
    }

    @Override
    public void send(EPMCException exception) {
        assert exception != null;
        this.exception = exception;
    }

    @Override
    public void send(ModelCheckerResult result) {
        assert result != null;
        if (result.getProperty() == null) {
            commonResult = result.getResult();
        } else {
            results.put(result.getProperty(), result.getResult());
            Scheduler scheduler = result.getScheduler();
            if (scheduler != null) {
                schedulers.put(result.getProperty(), scheduler);
            }
            LowLevel lowLevel = result.getLowLevel();
            if (lowLevel != null) {
                lowLevels.put(result.getProperty(), lowLevel);
            }
        }
    }

    @Override
    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    /**
     * Get exception thrown.
     * 
     * @return exception thrown
     */
    public EPMCException getException() {
        return exception;
    }

    /**
     * Translate time in ms since epoche to {@link String}.
     * The time parameter must be larger or equal to 0.
     * 
     * @param time time to translate
     * @return string representation of time
     */
    private String translateTimeStamp(long time) {
        assert time >= 0;
        return UtilMessages.translateTimeStamp(options, timeStarted, time);
    }

    /**
     * Get common result obtained.
     * 
     * @return common result obtained
     */
    public Object getCommonResult() {
        return commonResult;
    }

    /**
     * Get properties for which results were computed.
     * 
     * @return properties for which results were computed
     */
    public Collection<RawProperty> getProperties() {
        return results.keySet();
    }

    /**
     * Get result for a given property.
     * The property parameter must not be {@code null}.
     * 
     * @param property property for which to get result
     * @return result for a given property
     */
    public Object get(RawProperty property) {
        assert property != null;
        return results.get(property);
    }

    public Scheduler getScheduler(RawProperty property) {
        assert property != null;
        return schedulers.get(property);
    }

    public LowLevel getLowLevel(RawProperty property) {
        assert property != null;
        return lowLevels.get(property);
    }
}
