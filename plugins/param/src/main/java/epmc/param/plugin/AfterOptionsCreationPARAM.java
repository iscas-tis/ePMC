package epmc.param.plugin;

import epmc.error.EPMCException;
import epmc.options.OptionTypeStringList;
import epmc.options.Options;
import epmc.param.options.OptionsParam;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationPARAM implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-param";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		OptionTypeStringList typeParamList = new OptionTypeStringList("parameters");
		options.addOption()
			.setBundleName(OptionsParam.PARAM_OPTIONS)
			.setIdentifier(OptionsParam.PARAM_PARAMETER)
			.setType(typeParamList)
			.setCommandLine().setGui().setWeb().build();
	}
}
