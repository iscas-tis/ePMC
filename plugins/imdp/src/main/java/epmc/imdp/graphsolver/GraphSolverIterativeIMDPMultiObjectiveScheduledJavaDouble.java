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
import epmc.multiobjective.graphsolver.GraphSolverObjectiveExplicitMultiObjectiveScheduled;
import epmc.multiobjective.graphsolver.SchedulerSimpleMultiobjectiveJava;
import epmc.options.Options;
import epmc.value.Type;
import epmc.value.TypeInterval;
import epmc.value.TypeWeightTransition;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueContentDoubleArray;

public final class GraphSolverIterativeIMDPMultiObjectiveScheduledJavaDouble implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-imdp-multiobjective-scheduled-java-double";

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
        if (!(objective instanceof GraphSolverObjectiveExplicitMultiObjectiveScheduled)) {
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
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objMu = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        if (!(objMu.getScheduler() instanceof SchedulerSimpleMultiobjectiveJava)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        prepareIterGraph();
        multiobjectiveScheduled();
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
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveMultiObjectiveScheduled = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        inputValues = objectiveMultiObjectiveScheduled.getValues();
    }

    private void prepareResultValues() {
        this.outputValues = inputValues;
        objective.setResult(outputValues);
    }

    private void multiobjectiveScheduled() {
        Options options = Options.get();
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        double tolerance = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveMultiObjectiveScheduled = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        scheduler = (SchedulerSimpleMultiobjectiveJava) objectiveMultiObjectiveScheduled.getScheduler();
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
            Value valuesV, SchedulerSimpleMultiobjectiveJava scheduler) {
        Diff diffOp = UtilIMDPGraphSolver.getDiff();
        double[] values = ValueContentDoubleArray.getContent(valuesV);
        Arrays.fill(values, 0.0);
        double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
        double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        int numStates = graph.computeNumStates();
        int[] schedulerJava = scheduler.getDecisions();
        double[] presValues = values;
        double[] nextValues = new double[numStates];
        IteratorJavaDouble iterator = buildIterator((GraphExplicitSparseAlternate) iterGraph, values);

        double distance;
        do {
            distance = 0.0;
            for (int state = 0; state < numStates; state++) {
                double stopReward = stopRewards[state];
                double presStateProb = presValues[state];
                double nextStateProb = Double.NEGATIVE_INFINITY;
                int nondet = schedulerJava[state];
                if (nondet == -1) {
                    nextStateProb = stopReward;
                } else {
                    double transReward = transRewards[nondet];
                    nextStateProb = transReward
                            + iterator.nondetStep(nondet);
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

    private void mdpMultiobjectivescheduledGaussseidelJavaDouble(
            GraphExplicitSparseAlternate graph, Value stopRewardsV,
            Value transRewardsV,
            IterationStopCriterion stopCriterion, double tolerance,
            Value valuesV, SchedulerSimpleMultiobjectiveJava scheduler) {
        Diff diffOp = UtilIMDPGraphSolver.getDiff();
        int numStates = graph.computeNumStates();
        int[] schedulerJava = scheduler.getDecisions();
        double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
        double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        double[] values = ValueContentDoubleArray.getContent(valuesV);
        IteratorJavaDouble iterator = buildIterator((GraphExplicitSparseAlternate) iterGraph, values);
        Arrays.fill(values, 0.0);
        double distance;
        do {
            distance = 0.0;
            for (int state = 0; state < numStates; state++) {
                double stopReward = stopRewards[state];
                double presStateProb = values[state];
                double nextStateProb = Double.NEGATIVE_INFINITY;
                int nondet = schedulerJava[state];
                if (nondet == -1 || nondet == -2) {
                    nextStateProb = stopReward;
                } else {
                    double transReward = transRewards[nondet];
                    nextStateProb = transReward
                            + iterator.nondetStep(nondet);
                }
                distance = Math.max(distance, diffOp.diff(presStateProb, nextStateProb));
                values[state] = nextStateProb;
            }
        } while (distance > tolerance / 2);
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
