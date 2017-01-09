package epmc.jani.interaction.communication.handler;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.jani.interaction.communication.Backend;

public final class UtilHandler {
	public static void addIntegratedHandlers(Backend backend, Map<String, Handler> handlers) throws EPMCException {
		assert backend != null;
		assert handlers != null;
		handlers.put(HandlerAuthenticate.TYPE, new HandlerAuthenticate(backend));
		handlers.put(HandlerClose.TYPE, new HandlerClose(backend));
		handlers.put(HandlerQueryAnalysisEngines.TYPE, new HandlerQueryAnalysisEngines(backend));
		handlers.put(HandlerStartAnalysis.TYPE, new HandlerStartAnalysis(backend));
		handlers.put(HandlerStopAnalysis.TYPE, new HandlerStopAnalysis(backend));
		handlers.put(HandlerUpdateServerParameters.TYPE, new HandlerUpdateServerParameters(backend));
	}
	
	private UtilHandler() {
	}
}
