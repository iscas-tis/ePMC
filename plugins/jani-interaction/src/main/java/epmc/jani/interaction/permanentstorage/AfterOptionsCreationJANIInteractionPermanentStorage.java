package epmc.jani.interaction.permanentstorage;

import java.util.LinkedHashMap;
import java.util.Map;

import epmc.error.EPMCException;
import epmc.jani.interaction.InteractionExtension;
import epmc.jani.interaction.options.OptionsJANIInteraction;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationJANIInteractionPermanentStorage implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-jani-interaction-permanent-storage";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String,Class<? extends InteractionExtension>> extensions = options.get(OptionsJANIInteraction.JANI_INTERACTION_EXTENSION_CLASS);
		if (extensions == null) {
			extensions = new LinkedHashMap<>();
		}
		extensions.put(InteractionExtensionPermanentStorage.IDENTIFIER, InteractionExtensionPermanentStorage.class);
		options.set(OptionsJANIInteraction.JANI_INTERACTION_EXTENSION_CLASS, extensions);
	}

}
