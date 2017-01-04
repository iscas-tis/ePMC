package epmc.webserver.backend;

import epmc.error.EPMCException;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.plugin.AfterOptionsCreation;
import epmc.webserver.options.OptionsWebserver;

public final class AfterOptionsCreationWebserver implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-webserver";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
        OptionTypeString typeString = UtilOptions.getTypeString();
        options.addProgramOption(OptionsWebserver.BACKEND_PROPERTIES, typeString, null, false, false, false);
	}

}
