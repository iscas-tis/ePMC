package epmc.petl.plugin;

import epmc.plugin.BeforeModelCreation;
import epmc.prism.value.OperatorEvaluatorPRISMPow;
import epmc.value.operatorevaluator.SimpleEvaluatorFactory;

public class BeforeModelCreationPETL implements BeforeModelCreation {
	public final static String IDENTIFIER = "before-model-creation-petl";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process() {
		SimpleEvaluatorFactory.get().add(OperatorEvaluatorPRISMPow.Builder.class);
	}

}
