package epmc.guardedcommand.plugin;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.guardedcommand.model.ModelGuardedCommand;
import epmc.guardedcommand.model.PropertyGuardedCommand;
import epmc.guardedcommand.model.convert.UtilGuardedCommandConverter;
import epmc.guardedcommand.options.OptionsGuardedCommand;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationGuardedCommand implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-guardedcommand";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
		assert modelInputType != null;
		modelInputType.put(ModelGuardedCommand.IDENTIFIER, ModelGuardedCommand.class);
		Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
		assert propertyClasses != null;
		propertyClasses.put(PropertyGuardedCommand.IDENTIFIER, PropertyGuardedCommand.class);
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
        options.addOption().setBundleName(OptionsGuardedCommand.GUARDEDCOMMAND_OPTIONS)
        	.setIdentifier(OptionsGuardedCommand.GUARDEDCOMMAND_FLATTEN)
        	.setType(typeBoolean).setDefault(true)
        	.setCommandLine().setGui().setWeb().build();
        UtilGuardedCommandConverter.addOptions(options);
	}
}
