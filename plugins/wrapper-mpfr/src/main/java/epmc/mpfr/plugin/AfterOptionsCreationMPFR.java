package epmc.mpfr.plugin;

import epmc.error.EPMCException;
import epmc.mpfr.MPFR;
import epmc.mpfr.options.OptionsMPFR;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeIntegerInterval;
import epmc.options.OptionTypeString;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationMPFR implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-mpfr";
	private final static String DEFAULT_OUTPUT_FORMAT = "%Rg";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		OptionTypeBoolean booleanType = OptionTypeBoolean.getInstance();
		options.addOption().setBundleName(OptionsMPFR.OPTIONS_MPFR)
			.setIdentifier(OptionsMPFR.MPFR_ENABLE)
			.setType(booleanType).setDefault(false)
			.setCommandLine().setGui().setWeb().build();
		
		OptionTypeIntegerInterval precisionType = new OptionTypeIntegerInterval(2, ((~0) >>> 1) >>> 10);
		int defaultPrecision = MPFR.mpfr_get_default_prec().intValue();
		options.addOption().setBundleName(OptionsMPFR.OPTIONS_MPFR)
			.setIdentifier(OptionsMPFR.MPFR_PRECISION)
			.setType(precisionType).setDefault(defaultPrecision)
			.setCommandLine().setGui().setWeb().build();
		
		OptionTypeString formatType = OptionTypeString.getInstance();
		options.addOption().setBundleName(OptionsMPFR.OPTIONS_MPFR)
			.setIdentifier(OptionsMPFR.MPFR_OUTPUT_FORMAT)
			.setType(formatType).setDefault(DEFAULT_OUTPUT_FORMAT)
			.setCommandLine().setGui().setWeb().build();
	}

}
