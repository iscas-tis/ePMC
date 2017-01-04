package epmc.prism.model.convert;

import java.util.Map;

import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeEnum;
import epmc.options.Options;
import epmc.prism.model.PropertyPRISM;

public final class UtilPrismConverter {
	public static void addOptions(Options options) {
		assert options != null;
		Map<String,Class<?>> propertyClasses = options.get(OptionsModelChecker.PROPERTY_CLASS);
		assert propertyClasses != null;
		propertyClasses.put(PropertyPRISM.IDENTIFIER, PropertyPRISM.class);
        OptionTypeEnum typeRewardMethod = new OptionTypeEnum(RewardMethod.class);
        options.addOption().setBundleName(OptionsPRISMConverter.PRISM_CONVERTER_OPTIONS)
        	.setIdentifier(OptionsPRISMConverter.PRISM_CONVERTER_REWARD_METHOD)
        	.setType(typeRewardMethod).setDefault(RewardMethod.INTEGRATE)
        	.setCommandLine().setGui().setWeb().build();
        OptionTypeEnum typeSystemMethod = new OptionTypeEnum(SystemType.class);
        options.addOption().setBundleName(OptionsPRISMConverter.PRISM_CONVERTER_OPTIONS)
        	.setIdentifier(OptionsPRISMConverter.PRISM_CONVERTER_SYSTEM_METHOD)
        	.setType(typeSystemMethod).setDefault(SystemType.SYNCHRONISATION_VECTORS)
        	.setCommandLine().setGui().setWeb().build();
	}
	
	/**
	 * Private constructor to prevent instantiation of this class.
	 */
	private UtilPrismConverter() {
	}
}
