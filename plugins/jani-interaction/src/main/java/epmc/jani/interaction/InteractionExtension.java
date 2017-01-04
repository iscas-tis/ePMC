package epmc.jani.interaction;

import java.util.LinkedHashMap;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.jani.interaction.communication.Backend;
import epmc.jani.interaction.communication.handler.Handler;

public interface InteractionExtension {
	String getIdentifier();
	
	default void setBackend(Backend backend) throws EPMCException {
	}
	
	default Map<String,Handler> getHandlers() throws EPMCException {
		return new LinkedHashMap<>();
	}
}
