package epmc.imdp.graphsolver;

import java.util.Arrays;

import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsIMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.GraphExplicitSparseAlternate.EdgePropertySparseNondetOnlyNondet;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.multiobjective.graphsolver.GraphSolverObjectiveExplicitMultiObjectiveWeighted;
import epmc.multiobjective.graphsolver.SchedulerSimpleMultiobjectiveJava;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueContentDoubleArray;

public final class GraphSolverIterativeIMDPMultiObjectiveWeightedJavaDouble implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-imdp-multiobjective-weighted-java-double";

    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArray inputValues;
    private ValueArray outputValues;
    private SchedulerSimpleMultiobjectiveJava scheduler;
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
        if (!SemanticsIMDP.isIMDP(semantics)) {
            return false;
        }
        Type typeWeightTransition = TypeWeightTransition.get();
        if (!TypeInterval.is(typeWeightTransition)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        prepareIterGraph();
        multiobjectiveWeighted();
        prepareResultValues();
    }

    private void prepareIterGraph() {
        assert origGraph != null;
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedNodeProperties(origGraph.getNodeProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        GraphSolverObjectiveExplicitMultiObjectiveWeighted objectiveMultiObjectiveWeighted = (GraphSolverObjectiveExplicitMultiObjectiveWeighted) objective;
        inputValues = objectiveMultiObjectiveWeighted.getValues();
    }

    private void prepareResultValues() {
        this.outputValues = inputValues;
        objective.setResult(outputValues);
    }

    private void multiobjectiveWeighted() {
        Options options = Options.get();
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        double tolerance = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        GraphSolverObjectiveExplicitMultiObjectiveWeighted objectiveMultiObjectiveWeighted = (GraphSolverObjectiveExplicitMultiObjectiveWeighted) objective;
        Value cumulativeTransitionRewards = objectiveMultiObjectiveWeighted.getTransitionRewards();
        Value stopStateRewards = objectiveMultiObjectiveWeighted.getStopStateReward();
        scheduler = new SchedulerSimpleMultiobjectiveJava((GraphExplicitSparseAlternate) iterGraph);
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
            Value valuesV, SchedulerSimpleMultiobjectiveJava scheduler) {
        double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
        double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        double[] values = ValueContentDoubleArray.getContent(valuesV);
        Arrays.fill(values, 0.0);
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] schedulerJava = scheduler.getDecisions();
        Arrays.fill(schedulerJava, -2);
        fixNegative(graph, transRewards, stopRewards, values, schedulerJava);
        Diff diffOp = UtilIMDPGraphSolver.getDiff();
        double[] presValues = values;
        double[] nextValues = new double[numStates];
        IteratorJavaDouble iterator = buildIterator((GraphExplicitSparseAlternate) iterGraph, values);
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
            SchedulerSimpleMultiobjectiveJava scheduler) {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] schedulerJava = scheduler.getDecisions();
        double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
        double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        double[] values = ValueContentDoubleArray.getContent(valuesV);
        assert values != null;
        Arrays.fill(values, 0.0);
        Diff diffOp = getDiff();
        Arrays.fill(schedulerJava, -2);
        IteratorJavaDouble iterator = buildIterator((GraphExplicitSparseAlternate) iterGraph, values);
        Arrays.fill(values, 0);
        double distance;

        fixNegative(graph, transRewards, stopRewards, values, schedulerJava);
        do {
            distance = 0.0;
            for (int state = 0; state < numStates; state++) {
                double stopReward = stopRewards[state];
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
                if (stopReward > nextStateProb) {
                    nextStateProb = stopReward;
                    if (nextStateProb > presStateProb) {
                        schedulerJava[state] = -1;
                    }
                }
                distance = Math.max(distance, diffOp.diff(presStateProb, nextStateProb));
                values[state] = nextStateProb;
            }
        } while (distance > tolerance / 2);
    }

    private void fixNegative(GraphExplicitSparseAlternate graph, double[] transRewards, double[] stopRewards, double[] values, int[] schedulerJava) {
        assert graph != null;

        if (!fixNegativeNeeded(transRewards)) {
            return;
        }

        /* find states which only have non-zero reward transitions */
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] succStates = graph.getTargetsJava();
        BitSet zeroStates = new BitSetBoundedLongArray(numStates);
        zeroStates.set(0, numStates);
        for (int state = 0; state < numStates; state++) {
            double stopReward = stopRewards[state];
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            boolean zeroTransFound = false;
            for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                double transReward = transRewards[nondet];
                if (transReward == 0.0) {
                    zeroTransFound = true;
                }
            }
            if (stopReward == 0.0) {
                zeroTransFound = true;
            }
            if (!zeroTransFound) {
                zeroStates.set(state, false);
            }
        }    	
        boolean changed;
        do {
            changed = false;
            for (int state = 0; state < numStates; state++) {
                if (!zeroStates.get(state)) {
                    continue;
                }
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                boolean zeroTransFound = false;        		
                for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                    double transReward = transRewards[nondet];
                    if (transReward != 0.0) {
                        continue;
                    }
                    int nondetFrom = nondetBounds[nondet];
                    int nondetTo = nondetBounds[nondet + 1];
                    boolean isZeroTrans = true;
                    for (int succNr = nondetFrom; succNr < nondetTo; succNr++) {
                        int succ = succStates[succNr];
                        if (!zeroStates.get(succ)) {
                            isZeroTrans = false;
                            break;
                        }
                    }
                    if (isZeroTrans) {
                        zeroTransFound = true;
                        break;
                    }
                }
                if (!zeroTransFound) {
                    zeroStates.set(state, false);
                    changed = true;
                }
            }
        } while (changed);
        Arrays.fill(values, -100);
        for (int state = 0; state < numStates; state++) {
            if (!zeroStates.get(state)) {
                continue;
            }
            values[state] = 0.0;
            int stateFrom = stateBounds[state];
            int stateTo = stateBounds[state + 1];
            for (int nondet = stateFrom; nondet < stateTo; nondet++) {
                double transReward = transRewards[nondet];
                if (transReward == 0.0) {
                    schedulerJava[state] = nondet;
                    break;
                }
            }
            double stopReward = stopRewards[state];
            if (stopReward == 0.0) {
                schedulerJava[state] = -1;
            }
        }
    }

    private boolean fixNegativeNeeded(double[] transRewards) {
        boolean isNegative = false;
        for (int nd = 0; nd < transRewards.length; nd++) {
            if (transRewards[nd] < 0.0) {
                isNegative =  true;
                break;
            }
        }
        return isNegative;
    }

    private Diff getDiff() {
        IterationStopCriterion stopCriterion =
                Options.get().getEnum(OptionsGraphSolverIterative
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
        IteratorJavaDouble iterator = new IteratorJavaDouble.Builder()
                .setNumStates(numStates)
                .setStateBounds(stateBounds)
                .setMin(min)
                .setNondetBounds(nondetBounds)
                .setSuccessors(successors)
                .setValues(values)
                .setWeights(weights)
                .build();
        return iterator;
    }
}
