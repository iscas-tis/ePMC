package epmc.value.plugin;

import epmc.error.EPMCException;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.value.OptionsValue;

public final class AfterOptionsCreationValueBasic implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-value-basic";
    private final static String VALUE_FLOATING_POINT_DEFAULT = "%.7f";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
        assert options != null;
        OptionTypeString typeString = OptionTypeString.getInstance();
        options.addOption().setBundleName(OptionsValue.OPTIONS_VALUE)
            .setIdentifier(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_FORMAT)
            .setType(typeString).setDefault(VALUE_FLOATING_POINT_DEFAULT)
            .setCommandLine().setGui().setWeb().build();
	}


}
