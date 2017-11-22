package epmc.prism.plugin;

import epmc.plugin.BeforeModelCreation;
import epmc.prism.value.OperatorEvaluatorPRISMPow;
import epmc.value.operatorevaluator.SimpleEvaluatorFactory;

public final class BeforeModelCreationPRISM implements BeforeModelCreation {
    public final static String IDENTIFIER = "before-model-creation-prism";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process() {
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorPRISMPow.Builder.class);
    }

}
