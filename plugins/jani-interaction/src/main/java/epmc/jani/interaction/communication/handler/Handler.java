package epmc.jani.interaction.communication.handler;

import javax.json.JsonObject;

import epmc.error.EPMCException;

/**
 * Handler for methods of a given type.
 * 
 * @author Ernst Moritz Hahn
 */
public interface Handler {
	/**
	 * Get the message type the handler shall be used for.
	 * 
	 * @return message type the handler shall be used for
	 */
	String getType();

	/**
	 * Handle the given message.
	 * None of the parameters may be {@code null}.
	 * This method is used to handle a message sent by a given client. The
	 * backend must take care to forward the messages to the correct handler.
	 * In case this method throws an {@link EPMCException}, the backend is
	 * supposed to close the connection with the client with the error message
	 * specified in the exception.
	 * 
	 * @param client client which sent the message
	 * @param message message sent to the backend
	 * @throws EPMCException thrown in case of problems
	 */
	void handle(Object client, JsonObject message) throws EPMCException;
}
