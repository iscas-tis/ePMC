package epmc.imdp.bio;

import java.util.Map;

import epmc.graphsolver.OptionsGraphsolver;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.OptionTypeMap;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public final class AfterOptionsCreationIMDPBio implements AfterOptionsCreation {
    public final static String IDENTIFIER = "after-options-creation-imdp-robot";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) {
        assert options != null;
        OptionTypeMap<Class<?>> modelInputType = options.getType(OptionsModelChecker.MODEL_INPUT_TYPE);
        modelInputType.put(ModelBio.IDENTIFIER, ModelBio.class);
        Map<String,Class<?>> solvers = options.get(OptionsModelChecker.PROPERTY_SOLVER_CLASS);
        assert solvers != null;
        solvers.put(PropertySolverSteadyStateMDP.IDENTIFIER, PropertySolverSteadyStateMDP.class);
        Map<String, Class<?>> graphSolverMap = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        assert graphSolverMap != null;
        graphSolverMap.put(GraphSolverLongRunAverageStateOnlyMDP.IDENTIFIER, GraphSolverLongRunAverageStateOnlyMDP.class);
    }
}
