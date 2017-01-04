package epmc.qmc;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.qmc.model.ModelPRISMQMC;
import epmc.qmc.model.PropertyPRISMQMC;
import epmc.qmc.options.OptionsQMC;

public final class AfterOptionsCreationQMC implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-qmc";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
		modelInputType.put(ModelPRISMQMC.IDENTIFIER, ModelPRISMQMC.class);
		Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
		assert propertyClasses != null;
		propertyClasses.put(PropertyPRISMQMC.IDENTIFIER, PropertyPRISMQMC.class);
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsQMC.QMC_OPTIONS)
        	.setIdentifier(OptionsQMC.QMC_FLATTEN)
        	.setType(typeBoolean).setDefault(true)
        	.setCommandLine().setGui().setWeb().build();
	}
}
