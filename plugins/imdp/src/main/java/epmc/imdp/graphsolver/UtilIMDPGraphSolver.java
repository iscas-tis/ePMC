package epmc.imdp.graphsolver;

import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.options.Options;

final class UtilIMDPGraphSolver {
	
	static Diff getDiff(Options options) {
		assert options != null;
	    IterationStopCriterion stopCriterion =
	    		options.getEnum(OptionsGraphSolverIterative
	    				.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
	    switch (stopCriterion) {
		case ABSOLUTE:
			return (a,b) -> Math.abs(a - b);
		case RELATIVE:
			return (a,b) -> Math.abs(a - b) / a;
		default:
			break;
	    }
		return null;
	}


	private UtilIMDPGraphSolver() {
	}
}
