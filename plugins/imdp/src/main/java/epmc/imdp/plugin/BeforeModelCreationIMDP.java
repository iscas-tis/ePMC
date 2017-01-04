package epmc.imdp.plugin;

import epmc.error.EPMCException;
import epmc.imdp.value.OperatorInterval;
import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;

/**
 * IMDP plugin class containing method to execute after model creation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class BeforeModelCreationIMDP implements BeforeModelCreation {
	/** Identifier of this class. */
	public final static String IDENTIFIER = "before-model-loading-imdp";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
		contextValue.addOrSetOperator(OperatorInterval.class);
	}

}
