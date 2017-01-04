package epmc.jani.interaction.communication;

/**
 * Interface with which the backend sends messages.
 * 
 * @author Ernst Moritz Hahn
 */
public interface BackendFeedback {
	/**
	 * Send message.
	 * The client parameter decides where the message should be send to.
	 * The second parameter contains the actual message, which should be
	 * parsable as a JSON object.
	 * 
	 * 
	 * @param client to which instance to send message
	 * @param message message to send to client
	 */
	void send(Object client, String message);
	
	/**
	 * Log off the client with the given identifier.
	 * 
	 * @param who client to log off
	 */
	void logOff(Object who);
}
