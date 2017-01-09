package epmc.prism.plugin;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.PropertyPRISM;
import epmc.prism.model.convert.UtilPrismConverter;
import epmc.prism.options.OptionsPRISM;

public final class AfterOptionsCreationPRISM implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-prism";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
		assert modelInputType != null;
		modelInputType.put(ModelPRISM.IDENTIFIER, ModelPRISM.class);
		Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
		assert propertyClasses != null;
		propertyClasses.put(PropertyPRISM.IDENTIFIER, PropertyPRISM.class);
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsPRISM.PRISM_OPTIONS)
        	.setIdentifier(OptionsPRISM.PRISM_FLATTEN)
        	.setType(typeBoolean).setDefault(true)
        	.setCommandLine().setGui().setWeb().build();
        UtilPrismConverter.addOptions(options);
	}
}
