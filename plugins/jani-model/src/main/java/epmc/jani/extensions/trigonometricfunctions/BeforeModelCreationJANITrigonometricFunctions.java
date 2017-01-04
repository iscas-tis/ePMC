package epmc.jani.extensions.trigonometricfunctions;

import epmc.error.EPMCException;
import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;

public final class BeforeModelCreationJANITrigonometricFunctions implements BeforeModelCreation {
	/** Identifier of this class. */
	public final static String IDENTIFIER = "before-model-loading-jani-trigonometric-functions";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
		contextValue.addOrSetOperator(OperatorSin.class);
		contextValue.addOrSetOperator(OperatorCos.class);
		contextValue.addOrSetOperator(OperatorTan.class);
		contextValue.addOrSetOperator(OperatorAsin.class);
		contextValue.addOrSetOperator(OperatorAcos.class);
		contextValue.addOrSetOperator(OperatorAtan.class);
	}
}
