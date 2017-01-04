package epmc.jani.extensions.hyperbolicfunctions;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.jani.model.ModelExtension;
import epmc.jani.model.OptionsJANIModel;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.util.OrderedMap;

public class AfterOptionsCreationJANIHyperbolicFunctions implements AfterOptionsCreation {
	private final static String IDENTIFIER = "jani-hyperbolic-functions";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
        Map<String,Class<? extends ModelExtension>> modelExtensions =
        		options.get(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS);
        if (modelExtensions == null) {
        	modelExtensions = new OrderedMap<>();
            options.set(OptionsJANIModel.JANI_MODEL_EXTENSION_CLASS, modelExtensions);
        }
        modelExtensions.put(ModelExtensionHyperbolicFunctions.IDENTIFIER,
        		ModelExtensionHyperbolicFunctions.class);
	}
}
