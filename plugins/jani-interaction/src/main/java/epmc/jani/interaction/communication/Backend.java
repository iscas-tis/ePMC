package epmc.jani.interaction.communication;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import epmc.options.Options;
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
public final class Backend {
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
	/** Options used in this back end. */
	private final Options options;
	private final boolean stdio;
	private final Database permanentStorage;
	private final Map<String,InteractionExtension> extensions = new LinkedHashMap<>();
	
	/**
	 * Construct a new backend.
	 * None of the parameters may be {@code null}.
	 * 
	 * @param options options to use for the backend
	 * @param feedback feedback channel
	 * @throws EPMCException 
	 */
	Backend(Options options, BackendFeedback feedback) throws EPMCException {
		assert options != null;
		assert feedback != null;
		this.options = options;
		this.feedback = feedback;
		permanentStorage = new Database(options);
		prepareExtensions(options);
		JANIInteractionIO type = options.get(OptionsJANIInteraction.JANI_INTERACTION_TYPE);
		stdio = type == JANIInteractionIO.STDIO;
		handlers = buildHandlers();
	}

	private void prepareExtensions(Options options) {
		assert options != null;
		Map<String,Class<? extends InteractionExtension>> extensions = options.get(OptionsJANIInteraction.JANI_INTERACTION_EXTENSION_CLASS);
		assert extensions != null;
		for (Entry<String, Class<? extends InteractionExtension>> entry : extensions.entrySet()) {
			this.extensions.put(entry.getKey(), Util.getInstance(entry.getValue()));
		}
	}

	private Map<String, Handler> buildHandlers() throws EPMCException {
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
	synchronized void handle(Object client, String message) {
		assert client != null;
		assert message != null;
		assert feedback != null;
		JsonValue value = null;
		try {
			value = UtilJSON.read(message);
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
	private void handle(Object client, JsonValue value) throws EPMCException {
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
		String resourceName = getOptions().getResourceFileName();
		Locale locale = getOptions().getLocale();
        ResourceBundle poMsg = ResourceBundle.getBundle(resourceName, locale);
        return poMsg.getString(Options.TOOL_NAME);
	}
	
	public String buildUserErrorMessage(EPMCException exception) {
		assert exception != null;
		String message = exception.getProblem().getMessage(getOptions().getLocale());
		MessageFormat formatter = new MessageFormat(message);
		formatter.applyPattern(message);
		return formatter.format(exception.getArguments());
	}

	public void send(Object client, JsonValue reply) {
		assert client != null;
		assert reply != null;
		feedback.send(client, reply.toString());
	}
	
	public Options getOptions() {
		return options;
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
				.setOptions(getOptions())
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
