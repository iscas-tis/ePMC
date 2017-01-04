package epmc.filter.plugin;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.filter.propertysolver.PropertySolverDDFilter;
import epmc.filter.propertysolver.PropertySolverExplicitFilter;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

/**
 * Filter plugin class containing method to execute after options creation.
 * 
 * @author Ernst Moritz Hahn
 */
public final class AfterOptionsCreationFilter implements AfterOptionsCreation {
	/** Identifier of this class. */
	private final static String IDENTIFIER = "after-options-filter";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
		assert solvers != null;
		solvers.put(PropertySolverDDFilter.IDENTIFIER, PropertySolverDDFilter.class);
		solvers.put(PropertySolverExplicitFilter.IDENTIFIER, PropertySolverExplicitFilter.class);
	}
}
