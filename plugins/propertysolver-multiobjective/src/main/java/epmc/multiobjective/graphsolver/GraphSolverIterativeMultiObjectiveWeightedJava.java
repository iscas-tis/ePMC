package epmc.multiobjective.graphsolver;

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
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeInteger;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueContentIntArray;

public final class GraphSolverIterativeMultiObjectiveWeightedJava implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-multiobjective-weighted-java";
    
    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
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
        Options options = origGraph.getOptions();
        Type typeWeight = TypeWeight.get(origGraph.getContextValue());
        if (options.getBoolean(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)
                && TypeDouble.isDouble(typeWeight)) {
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
        ValueArrayAlgebra cumulativeTransitionRewards = objectiveMultiObjectiveWeighted.getTransitionRewards();
        ValueArrayAlgebra stopStateRewards = objectiveMultiObjectiveWeighted.getStopStateReward();
        ContextValue contextValue = origGraph.getContextValue();
        scheduler = UtilValue.newArray(TypeInteger.get(contextValue).getTypeArray(), origGraph.computeNumStates());
        objectiveMultiObjectiveWeighted.setScheduler(scheduler);
        inputValues = objectiveMultiObjectiveWeighted.getValues();
        if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpMultiobjectiveweightedJacobiJava(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpMultiobjectiveweightedGaussseidelJava(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else {
            assert false;
        }
    }
    
    /* auxiliary methods */
    
    private static void compDiff(double[] distance, ValueAlgebra previous,
            Value current, IterationStopCriterion stopCriterion) throws EPMCException {
        if (stopCriterion == null) {
            return;
        }
        double thisDistance = previous.distance(current);
        if (stopCriterion == IterationStopCriterion.RELATIVE) {
            double presNorm = previous.norm();
            if (presNorm != 0.0) {
                thisDistance /= presNorm;
            }
        }
        distance[0] = Math.max(distance[0], thisDistance);
    }
    
    private static boolean isSparseNondet(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparseAlternate;
    }
    
    private static boolean isSparseMDPJava(GraphExplicit graph) {
        if (!isSparseNondet(graph)) {
            return false;
        }
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        GraphExplicitSparseAlternate sparseNondet = asSparseNondet(graph);
        return !sparseNondet.isNative();
    }
    
    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }
    
    /* implementation/native call of/to iteration algorithms */    
    
    private void mdpMultiobjectiveweightedJacobiJava(
            GraphExplicitSparseAlternate graph, ValueArrayAlgebra stopRewards,
            ValueArrayAlgebra transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            ValueArrayAlgebra values, Value scheduler) throws EPMCException {
        ContextValue contextValue = graph.getContextValue();
        TypeWeight typeWeight = TypeWeight.get(contextValue);
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = ValueContentIntArray.getContent(scheduler);
        Arrays.fill(schedulerJava, -1);
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT)
        		.asSparseNondetOnlyNondet()
        		.getContent());
        ValueAlgebra stopReward = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        ValueAlgebra zero = values.getType().getEntryType().getZero();
        ValueAlgebra optInitValue = typeWeight.getNegInf();
        int valuesTotalSize = values.getTotalSize();
        for (int index = 0; index < valuesTotalSize; index++) {
            values.set(zero, index);
        }

        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        ValueAlgebra transReward = newValueWeight();
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                stopRewards.get(stopReward, state);
                presValues.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    transRewards.get(transReward, nondetNr);
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    choiceNextStateProb.set(transReward);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
                    }
                    if (choiceNextStateProb.isGt(nextStateProb)) {
                        nextStateProb.set(choiceNextStateProb);
                        if (nextStateProb.isGt(presStateProb)) {
                            schedulerJava[state] = nondetNr;
                        }
                    }
                }
                if (stopReward.isGt(nextStateProb)) {
                    nextStateProb.set(stopReward);
                    if (nextStateProb.isGt(presStateProb)) {
                        schedulerJava[state] = -1;
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        } while (distance[0] > tolerance / 2);
        values.set(presValues);
    }

    private void mdpMultiobjectiveweightedGaussseidelJava(
            GraphExplicitSparseAlternate graph, ValueArrayAlgebra stopRewards,
            ValueArrayAlgebra transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            ValueArrayAlgebra values, Value scheduler) throws EPMCException {
        ContextValue contextValue = graph.getContextValue();
        TypeWeight typeWeight = TypeWeight.get(contextValue);
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = ValueContentIntArray.getContent(scheduler);
        Arrays.fill(schedulerJava, -1);
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra objWeight = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        Value zero = values.getType().getEntryType().getZero();
        Value optInitValue = typeWeight.getNegInf();
        int valuesTotalSize = values.getTotalSize();
        for (int index = 0; index < valuesTotalSize; index++) {
            values.set(zero, index);
        }

        Value transReward = newValueWeight();
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                stopRewards.get(objWeight, state);
                values.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    transRewards.get(transReward, nondetNr);
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    choiceNextStateProb.set(transReward);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        values.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
                    }
                    if (choiceNextStateProb.isGt(nextStateProb)) {
                        nextStateProb.set(choiceNextStateProb);
                        if (nextStateProb.isGt(presStateProb)) {
                            schedulerJava[state] = nondetNr;
                        }
                    }
                }
                if (objWeight.isGt(nextStateProb)) {
                    nextStateProb.set(objWeight);
                    if (nextStateProb.isGt(presStateProb)) {
                        schedulerJava[state] = -1;
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                values.set(nextStateProb, state);
            }
        } while (distance[0] > tolerance / 2);
    }    
    
    private ValueAlgebra newValueWeight() {
    	return TypeWeight.get(getContextValue()).newValue();
    }
    
    private ContextValue getContextValue() {
    	return origGraph.getContextValue();
    }
}
