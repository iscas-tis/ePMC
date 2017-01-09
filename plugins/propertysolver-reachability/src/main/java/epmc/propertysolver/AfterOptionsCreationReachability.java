package epmc.propertysolver;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationReachability implements AfterOptionsCreation {
	// set identifier for the class
	private final static String IDENTIFIER = "after-options-creation-reachability";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		// get the solver map from options
		Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
		
		// put our self-defined solvers into the available solvers (both explicit and dd implementation)
		solvers.put(PropertySolverExplicitReachability.IDENTIFIER, PropertySolverExplicitReachability.class);
		solvers.put(PropertySolverDDReachability.IDENTIFIER, PropertySolverDDReachability.class);
	}
}
