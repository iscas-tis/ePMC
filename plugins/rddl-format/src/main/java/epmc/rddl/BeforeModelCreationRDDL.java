package epmc.rddl;

import epmc.error.EPMCException;
import epmc.plugin.BeforeModelCreation;
import epmc.rddl.value.OperatorBooleanTolerant;
import epmc.rddl.value.OperatorDistributionBernoulli;
import epmc.rddl.value.OperatorDistributionDiracDelta;
import epmc.rddl.value.OperatorDistributionDiscrete;
import epmc.rddl.value.OperatorDistributionKronDelta;
import epmc.rddl.value.OperatorDistributionNormal;
import epmc.rddl.value.OperatorSwitch;
import epmc.value.ContextValue;
import epmc.value.OperatorAdd;
import epmc.value.OperatorEq;
import epmc.value.OperatorMultiply;

public final class BeforeModelCreationRDDL implements BeforeModelCreation {
	private final static String IDENTIFIER = "before-model-loading-rddl";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(ContextValue contextValue) throws EPMCException {
		assert contextValue != null;
        contextValue.addOrSetOperator(new OperatorBooleanTolerant(contextValue, OperatorAdd.IDENTIFIER));
        contextValue.addOrSetOperator(new OperatorBooleanTolerant(contextValue, OperatorMultiply.IDENTIFIER));
        contextValue.addOrSetOperator(new OperatorBooleanTolerant(contextValue, OperatorEq.IDENTIFIER));
        contextValue.addOrSetOperator(OperatorDistributionBernoulli.class);
        contextValue.addOrSetOperator(OperatorDistributionDiracDelta.class);
        contextValue.addOrSetOperator(OperatorDistributionDiscrete.class);
        contextValue.addOrSetOperator(OperatorDistributionKronDelta.class);
        contextValue.addOrSetOperator(OperatorDistributionNormal.class);
        contextValue.addOrSetOperator(OperatorSwitch.class);
	}

}
