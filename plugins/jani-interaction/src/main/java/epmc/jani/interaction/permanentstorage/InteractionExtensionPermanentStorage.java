package epmc.jani.interaction.permanentstorage;

import epmc.jani.interaction.InteractionExtension;

public final class InteractionExtensionPermanentStorage implements InteractionExtension {
	public final static String IDENTIFIER = "permanent-storage";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
}
