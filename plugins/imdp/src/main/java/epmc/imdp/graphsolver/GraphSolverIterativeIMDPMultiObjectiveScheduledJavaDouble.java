package epmc.imdp.graphsolver;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.multiobjective.graphsolver.GraphSolverObjectiveExplicitMultiObjectiveScheduled;
import epmc.options.Options;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueContentDoubleArray;
import epmc.value.ValueContentIntArray;

public final class GraphSolverIterativeIMDPMultiObjectiveScheduledJavaDouble implements GraphSolverExplicit {
	public static String IDENTIFIER = "graph-solver-iterative-imdp-multiobjective-scheduled-java-double";

    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArray inputValues;
    private ValueArray outputValues;
    private Value scheduler;
    private GraphSolverObjectiveExplicit objective;
    private GraphBuilderExplicit builder;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setGraphSolverObjective(GraphSolverObjectiveExplicit objective) {
    	this.objective = objective;
        origGraph = objective.getGraph();
    }

    @Override
    public boolean canHandle() {
    	if (!(objective instanceof GraphSolverObjectiveExplicitMultiObjectiveScheduled)) {
            return false;
        }
    	Semantics semantics = origGraph.getGraphPropertyObject(CommonProperties.SEMANTICS);
    	if (!SemanticsMDP.isMDP(semantics)) {
    		return false;
    	}
        Type typeWeightTransition = TypeWeightTransition.get(origGraph.getContextValue());
        if (!TypeInterval.isInterval(typeWeightTransition)) {
        	return false;
        }
        Options options = origGraph.getOptions();
        if (options.getBoolean(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)) {
        	return false;
        }

        return true;
    }

    @Override
    public void solve() throws EPMCException {
    	prepareIterGraph();
    	multiobjectiveScheduled();
        prepareResultValues();
    }

    private void prepareIterGraph() throws EPMCException {
        assert origGraph != null;
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedNodeProperties(origGraph.getNodeProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        builder.setForNative(false);
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveMultiObjectiveScheduled = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        inputValues = objectiveMultiObjectiveScheduled.getValues();
    }

    private void prepareResultValues() throws EPMCException {
    	this.outputValues = inputValues;
    	objective.setResult(outputValues);
    }

    private void multiobjectiveScheduled() throws EPMCException {
        Options options = iterGraph.getOptions();
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        double tolerance = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveMultiObjectiveScheduled = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        scheduler = objectiveMultiObjectiveScheduled.getScheduler();
        Value stopStateRewards = objectiveMultiObjectiveScheduled.getStopStateRewards();
        Value cumulativeTransitionRewards = objectiveMultiObjectiveScheduled.getTransitionRewards();
        inputValues = objectiveMultiObjectiveScheduled.getValues();
        if (iterMethod == IterationMethod.JACOBI) {
            mdpMultiobjectivescheduledJacobiJavaDouble(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else if (iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpMultiobjectivescheduledGaussseidelJavaDouble(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else {
            assert false;
        }
    }

    /* auxiliary methods */
    
    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }
    
    /* implementation of iteration algorithms */    
    
    private void mdpMultiobjectivescheduledJacobiJavaDouble(
            GraphExplicitSparseAlternate graph,
            Value stopRewardsV,
            Value transRewardsV,
            IterationStopCriterion stopCriterion, double tolerance,
            Value valuesV, Value scheduler) throws EPMCException {
		Diff diffOp = UtilIMDPGraphSolver.getDiff(getOptions());
		double[] values = ValueContentDoubleArray.getContent(valuesV);
		Arrays.fill(values, 0.0);
		double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
		double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        int numStates = graph.computeNumStates();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = ValueContentIntArray.getContent(scheduler);
        double weights[] = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT)
        		.asSparseNondetOnlyNondet()
        		.getContent());
        double[] presValues = values;
        double[] nextValues = new double[numStates];
        double distance;
        do {
            distance = 0.0;
            for (int state = 0; state < numStates; state++) {
            	double stopReward = stopRewards[state];
            	double presStateProb = presValues[state];
            	double nextStateProb = Double.NEGATIVE_INFINITY;
                int nondetNr = schedulerJava[state];
                if (nondetNr == -1) {
                	nextStateProb = stopReward;
                } else {
                	double transReward = transRewards[nondetNr];
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    nextStateProb = transReward;
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    	double weight = weights[stateSucc];
                        int succState = targets[stateSucc];
                        double succStateProb = presValues[succState];
                        double weighted = weight * succStateProb;
                        nextStateProb = nextStateProb + weighted;
                    }
                }
                distance = Math.max(distance, diffOp.diff(presStateProb, nextStateProb));
                nextValues[state] = nextStateProb;
            }
            double[] swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        } while (distance > tolerance / 2);
        System.arraycopy(presValues, 0, values, 0, numStates);
    }
    
    private void mdpMultiobjectivescheduledGaussseidelJavaDouble(
            GraphExplicitSparseAlternate graph, Value stopRewardsV,
            Value transRewardsV,
            IterationStopCriterion stopCriterion, double tolerance,
            Value valuesV, Value scheduler) throws EPMCException {
		Diff diffOp = UtilIMDPGraphSolver.getDiff(getOptions());
        int numStates = graph.computeNumStates();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = ValueContentIntArray.getContent(scheduler);
        double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
        double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        double[] values = ValueContentDoubleArray.getContent(valuesV);
		Arrays.fill(values, 0.0);
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT)
        		.asSparseNondetOnlyNondet()
        		.getContent());
        double distance;
        do {
            distance = 0.0;
            for (int state = 0; state < numStates; state++) {
            	double stopReward = stopRewards[state];
            	double presStateProb = values[state];
                double nextStateProb = Double.NEGATIVE_INFINITY;
                int nondetNr = schedulerJava[state];
                if (nondetNr == -1) {
                	nextStateProb = stopReward;
                } else {
                	double transReward = transRewards[nondetNr];
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    nextStateProb = transReward;
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                    	double weight = weights[stateSucc];
                        int succState = targets[stateSucc];
                        double succStateProb = values[succState];
                        double weighted = weight * succStateProb;
                        nextStateProb = nextStateProb + weighted;
                    }
                }
                distance = Math.max(distance, diffOp.diff(presStateProb, nextStateProb));
                values[state] = nextStateProb;
            }
        } while (distance > tolerance / 2);
    }
    
    private Options getOptions() {
    	return origGraph.getOptions();
    }
}
