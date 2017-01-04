package epmc.jani.type.ctmc;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.OptionsJANIExplorer;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.OptionsJANIModel;
import epmc.options.OptionTypeBoolean;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public class AfterOptionsCreationJANICTMC implements AfterOptionsCreation {
	private final static String IDENTIFIER = "jani-ctmc";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
        Map<String,Class<ModelExtension>> modelExtensions = options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS);
        if (modelExtensions == null) {
        	modelExtensions = new OrderedMap<>();
            options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS, modelExtensions);
        }
        Map<String,Class<? extends ExplorerExtension>> explorerExtensions = options.get(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS);
        if (explorerExtensions == null) {
        	explorerExtensions = new OrderedMap<>();
        }
        explorerExtensions.put(ExplorerExtensionCTMC.IDENTIFIER, ExplorerExtensionCTMC.class);
        options.set(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS, explorerExtensions);
        addSemantics(options);
	}
	
	private void addSemantics(Options options) {
		assert options != null;
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();

        options.addOption().setBundleName(OptionsJANICTMC.OPTIONS_JANI_CTMC)
        	.setIdentifier(OptionsJANICTMC.JANI_CTMC_ALLOW_MULTI_TRANSITION)
        	.setType(typeBoolean).setDefault(true)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(OptionsJANIModel.JANI_MODEL_CATEGORY).build();

		Map<String, Class<? extends ModelExtensionSemantics>> modelSemanticTypes =
				options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS);
        if (modelSemanticTypes == null) {
        	modelSemanticTypes = new OrderedMap<>(true);
        }
		modelSemanticTypes.put(ModelExtensionCTMC.IDENTIFIER, ModelExtensionCTMC.class);
        options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS, modelSemanticTypes);
	}

}
