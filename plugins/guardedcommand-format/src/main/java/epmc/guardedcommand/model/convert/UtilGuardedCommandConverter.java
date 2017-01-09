package epmc.guardedcommand.model.convert;

import java.util.Map;

import epmc.guardedcommand.model.PropertyGuardedCommand;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeEnum;
import epmc.options.Options;

public final class UtilGuardedCommandConverter {
	public static void addOptions(Options options) {
		assert options != null;
		Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
		assert propertyClasses != null;
		propertyClasses.put(PropertyGuardedCommand.IDENTIFIER, PropertyGuardedCommand.class);
        OptionTypeEnum typeRewardMethod = new OptionTypeEnum(RewardMethod.class);
        options.addOption().setBundleName(OptionsGuardedCommandConverter.GUARDEDCOMMAND_CONVERTER_OPTIONS)
        	.setIdentifier(OptionsGuardedCommandConverter.GUARDEDCOMMAND_CONVERTER_REWARD_METHOD)
        	.setType(typeRewardMethod).setDefault(RewardMethod.INTEGRATE)
        	.setCommandLine().setGui().setWeb().build();
        OptionTypeEnum typeSystemMethod = new OptionTypeEnum(SystemType.class);
        options.addOption().setBundleName(OptionsGuardedCommandConverter.GUARDEDCOMMAND_CONVERTER_OPTIONS)
        	.setIdentifier(OptionsGuardedCommandConverter.GUARDEDCOMMAND_CONVERTER_SYSTEM_METHOD)
        	.setType(typeSystemMethod).setDefault(SystemType.SYNCHRONISATION_VECTORS)
        	.setCommandLine().setGui().setWeb().build();
	}
	
	/**
	 * Private constructor to prevent instantiation of this class.
	 */
	private UtilGuardedCommandConverter() {
	}
}
