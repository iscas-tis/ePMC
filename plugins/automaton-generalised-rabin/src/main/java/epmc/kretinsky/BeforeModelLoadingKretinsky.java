package epmc.kretinsky;

import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.plugin.BeforeModelCreation;

public final class BeforeModelLoadingKretinsky implements BeforeModelCreation {
	public final static String IDENTIFIER = "before-model-loading-kretinsky";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
	}

}
