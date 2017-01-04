package epmc.mpfr.plugin;

import epmc.error.EPMCException;
import epmc.mpfr.options.OptionsMPFR;
import epmc.mpfr.value.TypeMPFR;
import epmc.options.Options;
import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;
import epmc.value.TypeReal;

public final class BeforeModelCreationMPFR implements BeforeModelCreation {
	private final static String IDENTIFIER = "before-model-creation-mpfr";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
		Options options = contextValue.getOptions();
		boolean useMPFR = options.getBoolean(OptionsMPFR.MPFR_ENABLE);
		if (useMPFR) {
			TypeMPFR typeMPFR = new TypeMPFR(contextValue);
			TypeReal.set(typeMPFR);
		}
	}

}
