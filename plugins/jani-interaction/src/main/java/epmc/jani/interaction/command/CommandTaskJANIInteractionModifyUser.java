package epmc.jani.interaction.command;

import epmc.error.EPMCException;
import epmc.jani.interaction.UserManager;
import epmc.jani.interaction.database.Database;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.modelchecker.CommandTask;
import epmc.options.Options;

public final class CommandTaskJANIInteractionModifyUser implements CommandTask {
	/** Unique identifier of JANI interaction modify user command. */
	public final static String IDENTIFIER = "jani-interaction-modify-user";
	private Options options;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setOptions(Options options) {
		assert this.options == null;
		assert options != null;
		this.options = options;
	}

	@Override
	public void executeOnClient() throws EPMCException {
		Database storage = new Database(options);
		UserManager userManager = new UserManager(storage);
		String username = options.get(OptionsJANIInteraction.JANI_INTERACTION_MODIFIED_USERNAME);
		String password = options.get(OptionsJANIInteraction.JANI_INTERACTION_MODIFIED_PASSWORD);
		String usernameNew = options.get(OptionsJANIInteraction.JANI_INTERACTION_NEW_USERNAME);
		if (username != null) {
			userManager.changeUsername(username, usernameNew);
		}
		if (password != null) {
			userManager.changePassword(username, password);
		}
	}
}
