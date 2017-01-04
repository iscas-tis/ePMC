package epmc.predtransform;

import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.plugin.BeforeModelLoading;
import epmc.predtransform.options.CommandPredtransform;

public class BeforeModelLoadingPredtransform implements BeforeModelLoading {
	private final static String IDENTIFIER = "before-model-loading-predtransform";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		options.addCommand(new CommandPredtransform());
	}

}
