package epmc.jani.extensions.hyperbolicfunctions;

import epmc.error.EPMCException;
import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;

public final class BeforeModelCreationJANIHyperbolicFunctions implements BeforeModelCreation {
	/** Identifier of this class. */
	public final static String IDENTIFIER = "before-model-loading-jani-hyperbolic-functions";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
		contextValue.addOrSetOperator(OperatorSinh.class);
		contextValue.addOrSetOperator(OperatorCosh.class);
		contextValue.addOrSetOperator(OperatorTanh.class);
		contextValue.addOrSetOperator(OperatorAsinh.class);
		contextValue.addOrSetOperator(OperatorAcosh.class);
		contextValue.addOrSetOperator(OperatorAtanh.class);
	}
}
