package epmc.jani.interaction.communication.handler;

import javax.json.JsonObject;

import epmc.jani.interaction.communication.Backend;

/**
 * Handler for Close messages.
 * 
 * @author Ernst Moritz Hahn
 */
public final class HandlerClose implements Handler {
	/** Type of messages this handler handles. */
	public final static String TYPE = "close";

	/** Backend in which this handler is used. */
	private final Backend backend;

	public HandlerClose(Backend backend) {
		assert backend != null;
		this.backend = backend;
	}
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void handle(Object client, JsonObject object) {
		assert client != null;
		assert object != null;
		backend.logOffClient(client);
	}

}
