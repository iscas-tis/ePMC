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

package epmc.modelchecker;

import java.text.MessageFormat;
import java.util.Locale;

import epmc.error.EPMCException;
import epmc.messages.Message;
import epmc.messages.UtilMessages;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelCheckerResult;
import epmc.modelchecker.ModelCheckerResults;
import epmc.modelchecker.TestHelper.LogType;
import epmc.options.Options;
import epmc.util.StopWatch;

public final class LogTest implements Log {
    /** Empty string. */
    private final static String EMPTY = "";
    /** String containing a single space. */
    private final static String SPACE = " ";
    /** Formatter used for printing. */
    private final static MessageFormat formatter = new MessageFormat(EMPTY);

    private final Options options;
    /** Stop watch mesuring time since creation of the log. */
    private final StopWatch watch = new StopWatch(true);
    ModelCheckerResults results = new ModelCheckerResults();
    private boolean silent;
    private EPMCException exception;
    /**
     * Whether message should be output in human-readable format.
     * If yes, full sentences will be generated.
     * If not, the message type identifier followed by its parameters will be
     * printed.
     */
    private final long timeStarted;
    private final boolean translate;

    public LogTest(Options options, LogType logType) {
        assert options != null;
        this.options = options;
        timeStarted = System.currentTimeMillis();
        translate = (logType != LogType.NOTRANSLATE);
        silent = (logType == LogType.SILENT);
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
        if (translate) {
            System.out.print(translateTimeStamp(time));
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
        results.set(result);
    }

    @Override
    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    public EPMCException getException() {
        return exception;
    }

    private String translateTimeStamp(long time) {
        assert time >= 0 : time;
        return UtilMessages.translateTimeStamp(options, timeStarted, time);
    }

    public ModelCheckerResults getResults() {
        return results;
    }
}
