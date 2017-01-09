package epmc.jani.interaction.command;

import static epmc.error.UtilError.*;

import java.io.IOException;
import java.net.BindException;

import epmc.error.EPMCException;
import epmc.jani.interaction.communication.GUI;
import epmc.jani.interaction.communication.Server;
import epmc.jani.interaction.communication.StandardStream;
import epmc.jani.interaction.error.ProblemsJANIInteraction;
import epmc.jani.interaction.messages.MessagesJANIInteraction;
import epmc.jani.interaction.options.JANIInteractionIO;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.CommandTask;
import epmc.modelchecker.Log;
import epmc.modelchecker.ModelChecker;
import epmc.options.Options;

/**
 * Command to start JANI interaction mode.
 * 
 * @author Ernst Moritz Hahn
 */
public final class CommandTaskJaniInteractionStartServer implements CommandTask {
	/** Integer value to denote no timeout. */
	private final static int NO_TIMEOUT = 0;
	/** Unique identifier of JANI interaction server start command. */
	public final static String IDENTIFIER = "jani-interaction-start-server";
	
	/** Options used. */
	private Options options;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
	
	@Override
	public void setOptions(Options options) {
		assert options != null;
		this.options = options;
	}

	@Override
	public void setModelChecker(ModelChecker modelChecker) {
		assert modelChecker != null;
		this.options = modelChecker.getModel().getContextValue().getOptions();
	}
	
	@Override
	public void executeOnClient() throws EPMCException {
		assert options != null;
		JANIInteractionIO type = options.get(OptionsJANIInteraction.JANI_INTERACTION_TYPE);
		switch (type) {
		case STDIO:
			StandardStream standard = new StandardStream(options);
			standard.start();
			break;
		case WEBSOCKETS:
			Server server = new Server(options);
			try {
				server.start(NO_TIMEOUT);
			} catch (BindException e) {
				fail(ProblemsJANIInteraction.JANI_INTERACTION_SERVER_BIND_FAILED,
						options.getInteger(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT));
			} catch (IOException e) {
				fail(ProblemsJANIInteraction.JANI_INTERACTION_SERVER_IO_PROBLEM,
						options.getInteger(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT));
			}
			getLog().send(MessagesJANIInteraction.JANI_INTERACTION_SERVER_STARTED,
					options.getInteger(OptionsJANIInteraction.JANI_INTERACTION_WEBSOCKET_SERVER_PORT));
			if (options.getBoolean(OptionsJANIInteraction.JANI_INTERACTION_START_GUI)) {
				GUI.startGUI(options);
			}
			try {
				System.in.read();
			} catch (IOException e) {
			}
	        server.stop();
			getLog().send(MessagesJANIInteraction.JANI_INTERACTION_SERVER_STOPPED);
			break;
		default:
			assert false;
			break;		
		}
		// TODO Auto-generated method stub
	}

	private Log getLog() {
		return options.get(OptionsMessages.LOG);
	}
}