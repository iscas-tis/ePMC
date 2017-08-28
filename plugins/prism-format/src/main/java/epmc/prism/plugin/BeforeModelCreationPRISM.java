package epmc.prism.plugin;

import epmc.plugin.BeforeModelCreation;
import epmc.prism.value.OperatorEvaluatorPRISMPow;
import epmc.value.ContextValue;

public final class BeforeModelCreationPRISM implements BeforeModelCreation {
    public final static String IDENTIFIER = "before-model-creation-prism";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process() {
        ContextValue.get().addOperatorEvaluator(OperatorEvaluatorPRISMPow.INSTANCE);
    }

}
