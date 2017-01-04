package epmc.graphsolver.iterative;

import java.util.Map;

import epmc.error.EPMCException;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.options.Category;
import epmc.options.OptionTypeBoolean;
import epmc.options.OptionTypeEnum;
import epmc.options.OptionTypeRealNonnegative;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;

public class AfterOptionsCreationGraphSolverIterative implements AfterOptionsCreation {
    private final static String IDENTIFIER = "after-object-creation-graph-solver-iterative";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void process(Options options) throws EPMCException {
        assert options != null;
        
        Category category = options.addCategory()
        		.setBundleName(OptionsGraphSolverIterative.OPTIONS_GRAPH_SOLVER_ITERATIVE)
        		.setIdentifier(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_CATEGORY)
        		.setParent(OptionsGraphsolver.GRAPHSOLVER_CATEGORY)
        		.build();
        OptionTypeBoolean typeBoolean = OptionTypeBoolean.getInstance();        
        OptionTypeRealNonnegative typeRealNonnegative = OptionTypeRealNonnegative.getInstance();
        options.addOption().setBundleName(OptionsGraphSolverIterative.OPTIONS_GRAPH_SOLVER_ITERATIVE)
        	.setIdentifier(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)
        	.setType(typeBoolean)
        	.setDefault(true)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();

        options.addOption().setBundleName(OptionsGraphSolverIterative.OPTIONS_GRAPH_SOLVER_ITERATIVE)
        	.setIdentifier(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD)
        	.setType(new OptionTypeEnum(IterationMethod.class))
        	.setDefault(IterationMethod.GAUSS_SEIDEL)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsGraphSolverIterative.OPTIONS_GRAPH_SOLVER_ITERATIVE)
        	.setIdentifier(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION)
        	.setType(new OptionTypeEnum(IterationStopCriterion.class))
        	.setDefault(IterationStopCriterion.ABSOLUTE)
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        options.addOption().setBundleName(OptionsGraphSolverIterative.OPTIONS_GRAPH_SOLVER_ITERATIVE)
        	.setIdentifier(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE)
        	.setType(typeRealNonnegative)
        	.setDefault("1.0E-10")
        	.setCommandLine().setGui().setWeb()
        	.setCategory(category).build();
        Map<String, Class<?>> graphSolverMap = options.get(OptionsGraphsolver.GRAPHSOLVER_SOLVER_CLASS);
        assert graphSolverMap != null;
        graphSolverMap.put(GraphSolverIterative.IDENTIFIER, GraphSolverIterative.class);
    }
}
