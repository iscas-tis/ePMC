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

package epmc.jani.interaction.remote;

import static epmc.error.UtilError.fail;

import java.rmi.RemoteException;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.interaction.communication.resultformatter.ResultFormatter;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.messages.Message;
import epmc.messages.ProblemsMessages;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelCheckerResult;
import epmc.options.Options;
import epmc.util.StopWatch;
import epmc.util.Util;

/**
 * Class to conveniently send messages via an {@link EPMCChannel}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class LogJANI implements Log {
    private final static String VALUE = "value";
    private final static String FORMATTED_VALUE = "formatted-value";
    private final static String LABEL = "label";
    private final static String LABEL_RESULT = "Result";
    private final static String TYPE = "type";

    private final Options options;
    /** Message channel used by this log. */
    private final EPMCChannel channel;
    /** Stop watch mesuring time since creation of the log. */
    private final StopWatch watch = new StopWatch(true);
    /** Whether messages send should be suppressed. */
    private boolean silent;

    public LogJANI(Options options, EPMCChannel channel) {
        assert options != null;
        assert channel != null;
        this.options = options;
        this.channel = channel;
        try {
            channel.setTimeStarted(System.currentTimeMillis());
        } catch (RemoteException e) {
            fail(ProblemsMessages.REMOTE, e);
        }
    }

    @Override
    public void send(Message key, Object... params) {
        assert key != null;
        assert params != null;
        for (Object param : params) {
            assert param != null;
        }
        if (silent) {
            return;
        }
        String[] paramStrings = new String[params.length];
        for (int index = 0; index < params.length; index++) {
            paramStrings[index] = params[index].toString();
        }
        try {
            channel.send(watch.getTime(), key, paramStrings);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(EPMCException exception) {
        assert exception != null;
        try {
            getChannel().send(exception);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void send(ModelCheckerResult result) {
        assert result != null;
        if (result.getResult() instanceof EPMCException) {
            EPMCException exception = (EPMCException) result.getResult();
            try {
                getChannel().send(exception);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                getChannel().send(result.getProperty().getName(), formatResult(result));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private JsonValue formatResult(ModelCheckerResult mcResult) {
        Object value = mcResult.getResult();
        ResultFormatter formatter = getResultFormatter(value);
        String label = formatter.getLabel();
        if (label == null) {
            label = LABEL_RESULT;
        }
        return Json.createObjectBuilder()
                .add(LABEL, label)
                .add(TYPE, formatter.getType())
                .add(VALUE, formatter.getValue())
                .add(FORMATTED_VALUE, formatter.getFormattedValue())
                .build();
    }

    @Override
    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    @Override
    public boolean isSilent() {
        return silent;
    }

    public EPMCChannel getChannel() {
        return channel;
    }

    private ResultFormatter getResultFormatter(Object result) {
        assert result != null;
        Map<String,Class<ResultFormatter>> formatterClasses = options.get(OptionsJANIInteraction.JANI_INTERACTION_RESULT_FORMATTER_CLASS);
        for (Class<ResultFormatter> formatterClass : formatterClasses.values()) {
            ResultFormatter formatter = Util.getInstance(formatterClass);
            assert formatter != null : formatterClass;
            formatter.setResult(result);
            if (formatter.canHandle()) {
                return formatter;
            }
        }
        /* Because we have a general result handler, the lines below should
         * never be reached. */
        assert false;
        return null;
    }
}
