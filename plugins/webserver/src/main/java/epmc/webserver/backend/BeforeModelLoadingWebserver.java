package epmc.webserver.backend;

import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.plugin.BeforeModelCreation;
import epmc.webserver.options.CommandBackend;

public class BeforeModelLoadingWebserver implements BeforeModelCreation {
	private final static String IDENTIFIER = "before-model-loading-webserver";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		options.addCommand(new CommandBackend());
	}

}
