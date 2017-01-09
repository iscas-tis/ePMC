package epmc.graphsolver.lp;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationGraphSolverLP implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-graph-solver-lp";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        Map<String, Class<?>> graphSolverMap = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        assert graphSolverMap != null;
        graphSolverMap.put(GraphSolverLP.IDENTIFIER, GraphSolverLP.class);
    }
}
