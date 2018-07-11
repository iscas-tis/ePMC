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

package epmc.jani.interaction.communication;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.error.EPMCException;
import epmc.jani.interaction.InteractionExtension;
import epmc.jani.interaction.communication.handler.Handler;
import epmc.jani.interaction.communication.handler.UtilHandler;
import epmc.jani.interaction.database.Database;
import epmc.jani.interaction.options.JANIInteractionIO;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.main.options.UtilOptionsEPMC;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.plugin.PluginInterface;
import epmc.plugin.UtilPlugin;
import epmc.util.Util;
import epmc.util.UtilJSON;

/**
 * Backend part of JANI plugin.
 * This class manages the server side of sending and receiving JSON-encoded
 * messages between a server and its clients. The concrete means of
 * communication, such as via standard input/output or by WebSockets is
 * implemented in other classes.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Backend implements BackendInterface {
    /** Type field of JANI messages. */
    private final static String MESSAGE_TYPE = "type";
    /** Identifier of JSON field identifying reason to close connection. */
    private final static String CLOSE_REASON = "reason";

    /** Authentification message type identifier. */
    private final static String MESSAGE_TYPE_AUTHENTICATE = "authenticate";
    /** Close connection message type identifier. */
    private final static String MESSAGE_TYPE_CLOSE = "close";

    /** JANI version with which this server works TODO indeed 1? */
    private final static BigInteger OUR_JANI_VERSION = new BigInteger("1");
    /** String denoting major version. */
    private final static String VERSION_MAJOR = "major";
    /** String denoting minor version. */
    private final static String VERSION_MINOR = "minor";
    /** String denoting revision version. */
    private final static String VERSION_REVISION = "revision";

    /** Version of our server.
     * We use the SVN revision number of EPMC. */
    private final static JsonValue OUR_SERVER_VERSION;
    static {
        String versionString = Util.getManifestEntry(Util.SCM_REVISION);
        int version = 0;
        if (versionString != null) {
            version = Integer.parseInt(versionString);
        }
        OUR_SERVER_VERSION = Json.createObjectBuilder()
                .add(VERSION_MAJOR, 0)
                .add(VERSION_MINOR, 0)
                .add(VERSION_REVISION, version)
                .build();
    }

    /** Handlers for the different JANI interaction messages. */
    private final Map<String,Handler> handlers;
    private final BackendFeedback feedback;
    private final Map<Object,ClientInfo> clients = new LinkedHashMap<>();
    private final Map<Object,ClientInfo> clientsExternal = Collections.unmodifiableMap(clients);
    private final boolean stdio;
    private final Database permanentStorage;
    private final Map<String,InteractionExtension> extensions = new LinkedHashMap<>();

    /**
     * Construct a new backend.
     * None of the parameters may be {@code null}.
     * @param feedback feedback channel
     * @param plugins 
     * 
     */
    public Backend(BackendFeedback feedback, List<Class<? extends PluginInterface>> plugins) {
        assert feedback != null;
        if (Options.get() == null) {
            Options options = UtilOptionsEPMC.newOptions();
            Options.set(options);
            for (Class<? extends AfterOptionsCreation> clazz : UtilPlugin.getPluginInterfaceClasses(plugins, AfterOptionsCreation.class)) {
                AfterOptionsCreation object = Util.getInstance(clazz);
                object.process(options);
            }
        }
        this.feedback = feedback;
        permanentStorage = new Database();
        prepareExtensions();
        JANIInteractionIO type = Options.get().get(OptionsJANIInteraction.JANI_INTERACTION_TYPE);
        stdio = type == JANIInteractionIO.STDIO;
        handlers = buildHandlers();
    }

    private void prepareExtensions() {
        Map<String,Class<? extends InteractionExtension>> extensions = Options.get().get(OptionsJANIInteraction.JANI_INTERACTION_EXTENSION_CLASS);
        assert extensions != null;
        for (Entry<String, Class<? extends InteractionExtension>> entry : extensions.entrySet()) {
            this.extensions.put(entry.getKey(), Util.getInstance(entry.getValue()));
        }
    }

    private Map<String, Handler> buildHandlers() {
        Map<String,Handler> handlers = new LinkedHashMap<>();
        UtilHandler.addIntegratedHandlers(this, handlers);
        for (InteractionExtension extension : this.extensions.values()) {
            handlers.putAll(extension.getHandlers());
        }
        return Collections.unmodifiableMap(handlers);
    }

    /**
     * Handle a message in text form.
     * None of the parameters may be {@code null}.
     * 
     * @param client client sending the message
     * @param message message to be handled
     */
    @Override
    public synchronized void sendToBackend(Object client, JsonValue value) {
        assert client != null;
        assert value != null;
        assert feedback != null;
        try {
            handle(client, value);
        } catch (EPMCException e) {
            closeConnection(client, e);
        }
    }

    /**
     * Close connection for the given client.
     * This will send a close message to the client with the according error
     * message (if given), will tell the feedback object to log off the client
     * and will remove the client from the list of clients logged in.
     * 
     * @param client
     * @param exception
     */
    private void closeConnection(Object client, EPMCException exception) {
        assert client != null;
        JsonObjectBuilder object = Json.createObjectBuilder();
        object.add(MESSAGE_TYPE, MESSAGE_TYPE_CLOSE);
        if (exception != null) {
            object.add(CLOSE_REASON, buildUserErrorMessage(exception));
        }
        send(client, object.build());
        logOffClient(client);
    }

    /**
     * Handle a message which is already parsed to a JSON value.
     * None of the parameters may be {@code null}.
     * 
     * @param client client sending the message
     * @param message message to be handled
     */
    private void handle(Object client, JsonValue value) {
        assert client != null;
        assert value != null;
        JsonObject object = null;
        object = UtilJSON.toObject(value);
        String type = null;
        if (object.get(MESSAGE_TYPE) != null) {
            type = UtilJSON.toOneOf(object, MESSAGE_TYPE, handlers.keySet());
        } else {
            type = MESSAGE_TYPE_AUTHENTICATE;
        }
        Handler function = handlers.get(type);
        assert function != null;
        function.handle(client, object);
    }	

    /**
     * Obtain the tool name (EPMC).
     * 
     * @return tool name
     */
    public String getToolName() {
        String resourceName = Options.get().getResourceFileName();
        Locale locale = Options.get().getLocale();
        ResourceBundle poMsg = ResourceBundle.getBundle(resourceName, locale);
        return poMsg.getString(Options.TOOL_NAME);
    }

    public String buildUserErrorMessage(EPMCException exception) {
        assert exception != null;
        String message = exception.getProblem().getMessage(Options.get().getLocale());
        MessageFormat formatter = new MessageFormat(message);
        formatter.applyPattern(message);
        return formatter.format(exception.getArguments());
    }

    public void send(Object client, JsonValue reply) {
        assert client != null;
        assert reply != null;
        feedback.sendToClient(client, reply);
    }

    public Map<Object, ClientInfo> getClients() {
        return clientsExternal;
    }

    public boolean isStdio() {
        return stdio;
    }

    public BigInteger getOurJaniVersion() {
        return OUR_JANI_VERSION;
    }

    public Database getPermanentStorage() {
        return permanentStorage;
    }

    public void registerClient(Object client, int loginID, Set<String> clientExtensions) {
        assert client != null;
        Set<String> usableExtensions = new LinkedHashSet<>();
        usableExtensions.addAll(clientExtensions);
        usableExtensions.retainAll(this.extensions.keySet());
        ClientInfo clientDescription = new ClientInfo.Builder()
                .setClient(client)
                .setID(loginID)
                .setOptions(Options.get())
                .setExtensions(usableExtensions)
                .build();
        clients.put(client, clientDescription);
    }

    public JsonValue getOurServerVersion() {
        return OUR_SERVER_VERSION;
    }

    public void removeClient(Object client) {
        assert client != null;
        ClientInfo info = clients.get(client);
        if (info != null) {
            info.terminate();
        }
        clients.remove(client);
    }

    BackendFeedback getFeedback() {
        return feedback;
    }

    public boolean clientLoggedIn(Object client) {
        assert client != null;
        return clients.containsKey(client);
    }

    public ClientInfo getClientData(Object client) {
        return clients.get(client);
    }

    public void logOffClient(Object client) {
        assert client != null;
        removeClient(client);
        feedback.logOff(client);
    }

    public Map<String, InteractionExtension> getExtensions() {
        return extensions;
    }
}
