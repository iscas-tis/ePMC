package epmc.imdp.plugin;

import epmc.imdp.value.OperatorEvaluatorInterval;
import epmc.plugin.AfterModelCreation;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.operatorevaluator.SimpleEvaluatorFactory;

/**
 * IMDP plugin class containing method to execute after model creation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class AfterModelCreationIMDP implements AfterModelCreation {
    /** Identifier of this class. */
    public final static String IDENTIFIER = "after-model-loading-imdp";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process() {
        SimpleEvaluatorFactory.get().add(OperatorEvaluatorInterval.Builder.class);
        TypeWeightTransition.set(TypeInterval.get());
    }
}
