package epmc.specialiseqmc;

import epmc.algorithms.OptionsAlgorithm;
import epmc.dd.OptionsDD;
import epmc.error.EPMCException;
import epmc.expression.standard.OptionsExpressionBasic;
import epmc.graph.options.OptionsGraph;
import epmc.graphsolver.OptionsGraphsolver;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.main.options.OptionsEPMC;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.plugin.AfterOptionsCreation;
import epmc.plugin.OptionsPlugin;
import epmc.value.OptionsValue;

public final class AfterOptionsCreationSpecialiseQMC implements AfterOptionsCreation {
	public final static String IDENTIFIER = "after-options-creation-specialise-qmc";

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void process(Options options) throws EPMCException {
		assert options != null;
		// TODO adapt for QMC
		options.disableOption(OptionsEPMC.PORT);
		options.disableOption(OptionsModelChecker.MODEL_INPUT_TYPE);
		options.disableOption(OptionsModelChecker.PROPERTY_INPUT_TYPE);
		options.disableOption(OptionsEPMC.SERVER_NAME);
		/* currently buggy */
		options.disableOption(OptionsModelChecker.CONST);
		options.disableOption("iteration-method");
		options.disableOption("iteration-stop-criterion");
		options.disableOption("iteration-tolerance");
		options.disableOption(OptionsMessages.TRANSLATE_MESSAGES);
		options.disableOption(OptionsModelChecker.ENGINE);
		options.disableOption(OptionsDD.DD_AND_EXIST);
		options.disableOption(OptionsDD.DD_BINARY_ENGINE);
		options.disableOption(OptionsDD.DD_DEBUG);
		options.disableOption(OptionsDD.DD_LEAK_CHECK);
		options.disableOption(OptionsDD.DD_LIBRARY_CLASS);
		options.disableOption(OptionsDD.DD_MT_LIBRARY_CLASS);
		options.disableOption(OptionsDD.DD_MULTI_ENGINE);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_DD_LUMPER_CLASS);
		options.disableOption(OptionsAlgorithm.DD_SCC_ALGORITHM);
		options.disableOption(OptionsExpressionBasic.DD_EXPRESSION_CACHE);
		options.disableOption(OptionsExpressionBasic.DD_EXPRESSION_VECTOR);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_LUMP_BEFORE_GRAPH_SOLVING);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_LUMPER_EXPLICIT);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_SOLVER);
		options.disableOption(OptionsModelChecker.PROPERTY_SOLVER);
		options.disableOption(OptionsGraphsolver.GRAPHSOLVER_LUMPER_DD);
		options.disableOption(OptionsPlugin.PLUGIN);
		options.disableOption(OptionsMessages.TIME_STAMPS);
		options.disableOption(OptionsGraph.STATE_STORAGE);
		options.disableOption(OptionsGraph.WRAPPER_GRAPH_SUCCESSORS_SIZE);
		options.disableOption("prism-qmc-flatten");
		options.disableOption(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE);
		options.disableOption(OptionsValue.VALUE_FLOATING_POINT_OUTPUT_FORMAT);
		options.setToolName("EPMC QMC");
		options.setToolDescription("Model checking for quantum Markov chains.");
	}
}
