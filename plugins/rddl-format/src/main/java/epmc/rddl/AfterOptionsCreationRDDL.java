package epmc.rddl;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeMap;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.rddl.model.ModelRDDL;
import epmc.rddl.model.PropertyRDDL;
import epmc.rddl.options.OptionTypeRDDIntRange;
import epmc.rddl.options.OptionsRDDL;

public final class AfterOptionsCreationRDDL implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-rddl";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		OptionTypeString typeString = OptionTypeString.getInstance();
		OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
		OptionTypeRDDIntRange typeIntRange = new OptionTypeRDDIntRange();
		options.addOption().setBundleName(OptionsRDDL.RDDL_OPTIONS)
			.setIdentifier(OptionsRDDL.RDDL_INSTANCE_NAME)
			.setType(typeString)
			.setCommandLine().setGui().setWeb().build();
		options.addOption().setBundleName(OptionsRDDL.RDDL_OPTIONS)
			.setIdentifier(OptionsRDDL.RDDL_USE_INTERNAL_PROPERTIES)
			.setType(typeBoolean).setDefault(true)
			.setCommandLine().setGui().setWeb().build();
		options.addOption().setBundleName(OptionsRDDL.RDDL_OPTIONS)
			.setIdentifier(OptionsRDDL.RDDL_SUBSUME_SUCCESSORS_POST)
			.setType(typeBoolean).setDefault(true)
			.setCommandLine().setGui().setWeb().build();
		options.addOption().setBundleName(OptionsRDDL.RDDL_OPTIONS)
			.setIdentifier(OptionsRDDL.RDDL_ALLOW_DTMC_SEMANTICS)
			.setType(typeBoolean).setDefault(true)
			.setCommandLine().setGui().setWeb().build();
		options.addOption().setBundleName(OptionsRDDL.RDDL_OPTIONS)
			.setIdentifier(OptionsRDDL.RDDL_INT_RANGE)
			.setType(typeIntRange)
			.setCommandLine().setGui().setWeb().build();
		OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
		assert modelInputType != null;
		modelInputType.put(ModelRDDL.IDENTIFIER, ModelRDDL.class);
		Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
		assert propertyClasses != null;
		propertyClasses.put(PropertyRDDL.IDENTIFIER, PropertyRDDL.class);
	}
}
