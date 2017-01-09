package epmc.propertysolver;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationPCTL implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-options-creation-pctl";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
		solvers.put(PropertySolverExplicitPCTL.IDENTIFIER, PropertySolverExplicitPCTL.class);
		solvers.put(PropertySolverDDPCTL.IDENTIFIER, PropertySolverDDPCTL.class);
	}
}
