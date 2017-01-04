package epmc.imdp.graphsolver;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.GraphExplicitSparseAlternate.EdgePropertySparseNondetOnlyNondet;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.multiobjective.graphsolver.GraphSolverObjectiveExplicitMultiObjectiveWeighted;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeInteger;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueContentDoubleArray;
import epmc.value.ValueContentIntArray;

public final class GraphSolverIterativeIMDPMultiObjectiveWeightedJavaDouble implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-imdp-multiobjective-weighted-java-double";
    
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
    	if (!(objective instanceof GraphSolverObjectiveExplicitMultiObjectiveWeighted)) {
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
    	multiobjectiveWeighted();
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
        GraphSolverObjectiveExplicitMultiObjectiveWeighted objectiveMultiObjectiveWeighted = (GraphSolverObjectiveExplicitMultiObjectiveWeighted) objective;
        inputValues = objectiveMultiObjectiveWeighted.getValues();
    }

    private void prepareResultValues() throws EPMCException {
    	this.outputValues = inputValues;
    	objective.setResult(outputValues);
    }

    private void multiobjectiveWeighted() throws EPMCException {
        Options options = iterGraph.getOptions();
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        double tolerance = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        GraphSolverObjectiveExplicitMultiObjectiveWeighted objectiveMultiObjectiveWeighted = (GraphSolverObjectiveExplicitMultiObjectiveWeighted) objective;
        Value cumulativeTransitionRewards = objectiveMultiObjectiveWeighted.getTransitionRewards();
        Value stopStateRewards = objectiveMultiObjectiveWeighted.getStopStateReward();
        ContextValue contextValue = origGraph.getContextValue();
        scheduler = UtilValue.newArray(TypeInteger.get(contextValue).getTypeArray(), origGraph.computeNumStates());
        objectiveMultiObjectiveWeighted.setScheduler(scheduler);
        inputValues = objectiveMultiObjectiveWeighted.getValues();
        if (iterMethod == IterationMethod.JACOBI) {
            mdpMultiobjectiveweightedJacobiJavaDouble(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else if (iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpMultiobjectiveweightedGaussseidelJava(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else {
            assert false;
        }
    }
    
    /* auxiliary methods */
    
    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }
    
    /* implementation of iteration algorithms */    
    
    private void mdpMultiobjectiveweightedJacobiJavaDouble(
            GraphExplicitSparseAlternate graph, Value stopRewardsV,
            Value transRewardsV,
            IterationStopCriterion stopCriterion, double tolerance,
            Value valuesV, Value scheduler) throws EPMCException {
        double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
        double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        double[] values = ValueContentDoubleArray.getContent(valuesV);
		Arrays.fill(values, 0.0);
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] schedulerJava = ValueContentIntArray.getContent(scheduler);
        Arrays.fill(schedulerJava, -1);
		Diff diffOp = UtilIMDPGraphSolver.getDiff(getOptions());
        double[] presValues = values;
        double[] nextValues = new double[numStates];
		IteratorJavaDouble iterator = buildIterator((GraphExplicitSparseAlternate) iterGraph, values);
        double transReward;
        double distance;
        do {
            distance = 0.0;
            for (int state = 0; state < numStates; state++) {
            	double stopReward = stopRewards[state];
                double presStateProb = presValues[state];
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                double nextStateProb = Double.NEGATIVE_INFINITY;
                for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                	transReward = transRewards[nondet];
                    double choiceNextStateProb = transReward
                    		+ iterator.nondetStep(nondet);
                    if (choiceNextStateProb > nextStateProb) {
                    	nextStateProb = choiceNextStateProb;
                        if (nextStateProb > presStateProb) {
                            schedulerJava[state] = nondet;
                        }
                    }
                }
                if (stopReward > nextStateProb) {
                    nextStateProb = stopReward;
                    if (nextStateProb > presStateProb) {
                        schedulerJava[state] = -1;
                    }
                }
                distance = Math.max(distance, diffOp.diff(presStateProb, nextStateProb));
                nextValues[state] = nextStateProb;
            }
            double[] swap = nextValues;
            nextValues = presValues;
            presValues = swap;
            iterator.setValues(presValues);
        } while (distance > tolerance / 2);
        System.arraycopy(presValues, 0, values, 0, numStates);
    }

    private void mdpMultiobjectiveweightedGaussseidelJava(
            GraphExplicitSparseAlternate graph,
            Value stopRewardsV,
            Value transRewardsV,
            IterationStopCriterion stopCriterion,
            double tolerance,
            Value valuesV,
            Value scheduler) throws EPMCException {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] schedulerJava = ValueContentIntArray.getContent(scheduler);
        double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
        double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        double[] values = ValueContentDoubleArray.getContent(valuesV);
        assert values != null;
		Arrays.fill(values, 0.0);
		Diff diffOp = getDiff();
        Arrays.fill(schedulerJava, -1);
		IteratorJavaDouble iterator = buildIterator((GraphExplicitSparseAlternate) iterGraph, values);
        double distance;
        do {
            distance = 0.0;
            for (int state = 0; state < numStates; state++) {
            	double objWeight = stopRewards[state];
                double presStateProb = values[state];
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                double nextStateProb = Double.NEGATIVE_INFINITY;
                for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                	double transReward = transRewards[nondet];
                    double choiceNextStateProb = transReward
                    		+ iterator.nondetStep(nondet);
                    if (choiceNextStateProb > nextStateProb) {
                        nextStateProb = choiceNextStateProb;
                        if (nextStateProb > presStateProb) {
                            schedulerJava[state] = nondet;
                        }
                    }
                }
                if (objWeight > nextStateProb) {
                    nextStateProb = objWeight;
                    if (nextStateProb > presStateProb) {
                        schedulerJava[state] = -1;
                    }
                }
                distance = Math.max(distance, diffOp.diff(presStateProb, nextStateProb));
                values[state] = nextStateProb;
            }
        } while (distance > tolerance / 2);
    }
    
	private Diff getDiff() {
	    IterationStopCriterion stopCriterion =
	    		getOptions().getEnum(OptionsGraphSolverIterative
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
	
	private static IteratorJavaDouble buildIterator(GraphExplicitSparseAlternate graph, double[] values) {
		int numStates = graph.computeNumStates();
		boolean min = false;
		int[] stateBounds = graph.getStateBoundsJava();
		int[] nondetBounds = graph.getNondetBoundsJava();
		int[] successors = graph.getTargetsJava();
		EdgePropertySparseNondetOnlyNondet weightProp = (EdgePropertySparseNondetOnlyNondet) graph.getEdgeProperty(CommonProperties.WEIGHT);
		double[] weights = ValueContentDoubleArray.getContent(weightProp.getContent());
		Options options = graph.getOptions();
		IteratorJavaDouble iterator = new IteratorJavaDouble.Builder()
				.setNumStates(numStates)
				.setStateBounds(stateBounds)
				.setMin(min)
				.setNondetBounds(nondetBounds)
				.setSuccessors(successors)
				.setValues(values)
				.setWeights(weights)
				.setOptions(options)
				.build();
		return iterator;
	}
	
	private Options getOptions() {
		return origGraph.getOptions();
	}
}
