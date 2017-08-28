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

package epmc.jani.interaction.communication.handler;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.MessageFormat;
import java.util.Locale;

import javax.json.Json;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.remote.EPMCChannel;
import epmc.messages.Message;
import epmc.messages.MessageInstance;
import epmc.messages.OptionsMessages;
import epmc.options.Options;

/**
 * JANI analysis channel.
 * 
 * @author Ernst Moritz Hahn
 */
final class JANIChannel extends UnicastRemoteObject implements EPMCChannel {
    /** 1L, as I don't know any better. */
    private static final long serialVersionUID = 1L;
    /** Empty string. */
    private final static String EMPTY = "";
    /** String containing white space. */
    private final static String SPACE = " ";
    /** Type field of JANI messages. */
    private final static String TYPE = "type";
    private final static String ANALYSIS_MESSAGE = "analysis-message";
    private final static String ANALYSIS_RESULTS = "analysis-results";
    private final static String ID = "id";
    private final static String ANALYSIS_MESSAGE_SEVERITY = "severity";
    private final static String ANALYSIS_MESSAGE_SEVERITY_INFO = "info";
    private final static String ANALYSIS_MESSAGE_SEVERITY_WARNING = "warning";
    private final static String ANALYSIS_MESSAGE_SEVERITY_ERROR = "error";
    private final static String ANALYSIS_MESSAGE_MESSAGE = "message";
    private final static MessageFormat formatter = new MessageFormat(EMPTY);
    private final static String RESULTS = "results";
    private final static String PROPERTY = "property";

    /** Backend to which this channel belongs. */
    private final Backend backend;
    /** Client to which this channel belongs. */
    private final Object client;
    /** Analysis ID to which this channel belongs. */
    private final BigInteger id;

    /**
     * Create a new JANI message channel.
     * None of the parameters may be {@code null}.
     * 
     * @param backend backend to which the channel belongs
     * @param client client which started analysis to which channel belongs
     * @param id ID of analysis to which the channel belongs
     * @return new JANI message channel
     */
    static JANIChannel newChannel(Backend backend, Object client, BigInteger id) {
        assert backend != null;
        assert client != null;
        assert id != null;
        try {
            return new JANIChannel(backend, client, id);
        } catch (RemoteException e) {
            return null;
        }
    }

    /**
     * Create a new JANI message channel.
     * None of the parameters may be {@code null}.
     * 
     * @param backend backend to which the channel belongs
     * @param client client which started analysis to which channel belongs
     * @param id ID of analysis to which the channel belongs
     * @throws RemoteException thrown in case of remove error (never, I guess)
     */
    private JANIChannel(Backend backend, Object client, BigInteger id) throws RemoteException {
        super();
        assert backend != null;
        assert client != null;
        assert id != null;
        this.backend = backend;
        this.client = client;
        this.id = id;
    }

    @Override
    public void setTimeStarted(long time) throws RemoteException {
        assert time >= 0;
    }

    void reportMessage(Object client, BigInteger id, MessageInstance instance) {
        assert client != null;
        assert id != null;
        assert instance != null;
        String status = buildMessage(instance);
        JsonValue reply = Json.createObjectBuilder()
                .add(TYPE, ANALYSIS_MESSAGE)
                .add(ID, id)
                .add(ANALYSIS_MESSAGE_SEVERITY,
                        instance.getMessage().isWarning()
                        ? ANALYSIS_MESSAGE_SEVERITY_WARNING
                                : ANALYSIS_MESSAGE_SEVERITY_INFO)
                .add(ANALYSIS_MESSAGE_MESSAGE, status)
                .build();
        backend.send(client, reply);
    }

    private String buildMessage(MessageInstance instance) {
        assert instance != null;
        Message message = instance.getMessage();
        String[] parameters = instance.getParametersArray();
        boolean translate = Options.get().getBoolean(OptionsMessages.TRANSLATE_MESSAGES);
        String status;
        if (translate) {
            Locale locale = Options.get().getLocale();
            formatter.applyPattern(message.getMessage(locale));
            status = formatter.format(parameters);
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append(message);
            for (String argument : parameters) {
                builder.append(SPACE);
                builder.append(argument);
            }
            status = builder.toString();
        }
        return status;
    }

    private void reportException(Object client, BigInteger id, EPMCException exception) {
        assert client != null;
        assert id != null;
        assert exception != null;
        String status = backend.buildUserErrorMessage(exception);
        JsonValue reply = Json.createObjectBuilder()
                .add(TYPE, ANALYSIS_MESSAGE)
                .add(ID, id)
                .add(ANALYSIS_MESSAGE_SEVERITY, ANALYSIS_MESSAGE_SEVERITY_ERROR)
                .add(ANALYSIS_MESSAGE_MESSAGE, status)
                .build();
        backend.send(client, reply);
    }

    private void reportModelCheckerResult(Object client, BigInteger id, String name, JsonValue result) {
        assert client != null;
        assert id != null;
        assert result != null;
        JsonValue results = Json.createArrayBuilder()
                .add(result)
                .build();
        JsonValue analysisDataSet = Json.createObjectBuilder()
                .add(PROPERTY, name)
                .add(RESULTS, results)
                .build();
        JsonValue reply = Json.createObjectBuilder()
                .add(TYPE, ANALYSIS_RESULTS)
                .add(ID, id)
                .add(RESULTS, analysisDataSet)
                .build();
        backend.send(client, reply);
    }

    @Override
    public void send(long time, Message key, String... arguments) throws RemoteException {
        reportMessage(client, id, new MessageInstance(time, key, arguments));
    }

    @Override
    public void send(EPMCException exception) throws RemoteException {
        reportException(exception, id, exception);
    }

    @Override
    public void send(String name, JsonValue result) throws RemoteException {
        reportModelCheckerResult(result, id, name, result);
    }
}
