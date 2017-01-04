package epmc.cuda.graphsolver;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.options.Options;
import epmc.options.OptionsEPMC;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationCUDA implements AfterOptionsCreation {
	private final static String IDENTIFIER = "after-objects-creation-cuda";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		Map<String, Class<?>> graphSolvers = options.get(OptionsEPMC.GRAPH_SOLVER_CLASS);
		assert graphSolvers != null;
		graphSolvers.put(GraphSolverCUDA.IDENTIFIER, GraphSolverCUDA.class);
	}
}
