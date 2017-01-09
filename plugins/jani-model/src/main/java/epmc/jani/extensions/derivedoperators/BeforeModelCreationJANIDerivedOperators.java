package epmc.jani.extensions.derivedoperators;

import epmc.error.EPMCException;
import epmc.plugin.BeforeModelCreation;
import epmc.value.ContextValue;

public final class BeforeModelCreationJANIDerivedOperators implements BeforeModelCreation {
	/** Identifier of this class. */
	public final static String IDENTIFIER = "before-model-loading-jani-derived-operators";
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
		contextValue.addOrSetOperator(OperatorSgn.class);
		contextValue.addOrSetOperator(OperatorAbs.class);
		contextValue.addOrSetOperator(OperatorTrunc.class);
	}
}
