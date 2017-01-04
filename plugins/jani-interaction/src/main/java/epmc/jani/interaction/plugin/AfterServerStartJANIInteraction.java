package epmc.jani.interaction.plugin;

import epmc.error.EPMCException;
import epmc.jani.interaction.command.CommandTaskJaniInteractionStartServer;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.plugin.AfterServerStart;

public final class AfterServerStartJANIInteraction implements AfterServerStart {
	public final static String IDENTIFIER = "after-server-start-jani-interaction";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
        String commandName = options.getString(Options.COMMAND);
        if (commandName.equals(CommandTaskJaniInteractionStartServer.IDENTIFIER)) {
        	Log log = options.get(OptionsMessages.LOG);
        	log.setSilent(true);
        }
	}

}
