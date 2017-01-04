package epmc.jani.type.lts;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.OptionsJANIExplorer;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.OptionsJANIModel;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public class AfterOptionsCreationJANILTS implements AfterOptionsCreation {
	private final static String IDENTIFIER = "jani-lts";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;

        Map<String,Class<ModelExtension>> modelExtensions = options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS);
        if (modelExtensions == null) {
        	modelExtensions = new OrderedMap<>();
            options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS, modelExtensions);
        }
        Map<String,Class<? extends ExplorerExtension>> explorerExtensions = options.get(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS);
        if (explorerExtensions == null) {
        	explorerExtensions = new OrderedMap<>();
        }
        explorerExtensions.put(ExplorerExtensionLTS.IDENTIFIER, ExplorerExtensionLTS.class);
        options.set(OptionsJANIExplorer.JANI_EXPLORER_EXTENSION_CLASS, explorerExtensions);
        addSemantics(options);
	}
	
	private void addSemantics(Options options) {
		assert options != null;
		Map<String, Class<? extends ModelExtensionSemantics>> modelSemanticTypes =
				options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS);
        if (modelSemanticTypes == null) {
        	modelSemanticTypes = new OrderedMap<>(true);
        }
		modelSemanticTypes.put(ModelExtensionLTS.IDENTIFIER, ModelExtensionLTS.class);
        options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_SEMANTICS, modelSemanticTypes);        
	}

}
