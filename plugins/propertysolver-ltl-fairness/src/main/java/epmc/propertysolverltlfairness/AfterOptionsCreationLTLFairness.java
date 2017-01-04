package epmc.propertysolverltlfairness;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeBoolean;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationLTLFairness implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-ltl-fairness";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
		assert solvers != null;
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();
		solvers.put(PropertySolverDDLTLFairness.IDENTIFIER, PropertySolverDDLTLFairness.class);
		solvers.put(PropertySolverExplicitLTLFairness.IDENTIFIER, PropertySolverExplicitLTLFairness.class);
		options.addOption().setBundleName(OptionsLTLFairness.OPTIONS_LTL_FAIRNESS)
			.setIdentifier(OptionsLTLFairness.LTL_FAIRNESS_SCC_SKIP_TRANSIENT)
			.setType(typeBoolean).setDefault(true)
			.setCommandLine().setGui().setWeb().build();
	}
}
