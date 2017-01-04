package epmc.jani.type.smg;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.OptionsJANIExplorer;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.OptionsJANIModel;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public final class AfterOptionsCreationSMG implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-smg";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		Map<String, Class<? extends ModelExtensionSemantics>> janiToSemantics = options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS);
		assert janiToSemantics != null;
		janiToSemantics.put(ModelExtensionSMG.IDENTIFIER, ModelExtensionSMG.class);
        Map<String,Class<? extends ExplorerExtension>> explorerExtensions = options.get(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS);
        if (explorerExtensions == null) {
        	explorerExtensions = new OrderedMap<>();
        }
        explorerExtensions.put(ExplorerExtensionSMG.IDENTIFIER, ExplorerExtensionSMG.class);
        options.set(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS, explorerExtensions);
	}
}
