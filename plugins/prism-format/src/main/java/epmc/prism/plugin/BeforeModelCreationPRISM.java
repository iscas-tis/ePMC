package epmc.prism.plugin;

import epmc.error.EPMCException;
import epmc.plugin.BeforeModelCreation;
import epmc.prism.value.OperatorEvaluatorPRISMPow;
import epmc.prism.value.OperatorPRISMPow;
import epmc.value.ContextValue;

public final class BeforeModelCreationPRISM implements BeforeModelCreation {
	public final static String IDENTIFIER = "before-model-creation-prism";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process() throws EPMCException {
		ContextValue.get().addOrSetOperator(OperatorPRISMPow.IDENTIFIER, OperatorPRISMPow.class);
		ContextValue.get().addOperatorEvaluator(OperatorEvaluatorPRISMPow.INSTANCE);
	}

}
