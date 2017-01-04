package epmc.jani.interaction.communication.handler;

import java.math.BigInteger;

import javax.json.JsonObject;

import epmc.error.EPMCException;
import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.communication.ClientInfo;
import epmc.util.UtilJSON;

/**
 * Handler for StopAnalysis messages.
 * 
 * @author Ernst Moritz Hahn
 */
public final class HandlerStopAnalysis implements Handler {
	public final static String TYPE = "analysis-stop";
	private final static String ID = "id";

	private final Backend backend;

	public HandlerStopAnalysis(Backend backend) {
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
		ClientInfo clientDescription = backend.getClientData(client);
		if (clientDescription == null) {
			return;
		}
		BigInteger id = null;
		try {
			id = UtilJSON.getBigInteger(object, ID);
		} catch (EPMCException e) {
			return;
		}
		clientDescription.stopAnalysis(id);
		
		// TODO
	}
}
