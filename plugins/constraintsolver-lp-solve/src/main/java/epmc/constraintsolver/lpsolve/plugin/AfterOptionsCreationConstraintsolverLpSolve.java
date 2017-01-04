package epmc.constraintsolver.lpsolve.plugin;

import java.util.Map;

import epmc.constraintsolver.lpsolve.ConstraintSolverLPSolve;
import epmc.constraintsolver.options.OptionsConstraintsolver;
import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationConstraintsolverLpSolve implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-constraintsolver-lp-solve";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String,Class<?>> solvers = options.get(OptionsConstraintsolver.CONSTRAINTSOLVER_SOLVER_CLASS);
		assert solvers != null;
        solvers.put(ConstraintSolverLPSolve.IDENTIFIER, ConstraintSolverLPSolve.class);
	}

}
