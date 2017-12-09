/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.multiobjective.graphsolver;

import java.util.Arrays;

import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.MessagesGraphSolverIterative;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.multiobjective.graphsolver.GraphSolverObjectiveExplicitMultiObjectiveScheduled;
import epmc.options.Options;
import epmc.util.StopWatch;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeWeight;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueContentDoubleArray;

public final class GraphSolverIterativeMultiObjectiveScheduledJavaDouble implements GraphSolverExplicit {
    @FunctionalInterface
    private static interface Diff {
        double diff(double value1, double value2);
    }

    public static final String IDENTIFIER = "graph-solver-iterative-multiobjective-scheduled-java-double";

    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArray inputValues;
    private ValueArray outputValues;
    private SchedulerSimpleMultiobjectiveJava scheduler;
    private GraphSolverObjectiveExplicit objective;
    private GraphBuilderExplicit builder;

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
        Type typeWeight = TypeWeight.get();
        if (!TypeDouble.is(typeWeight)) {
            return false;
        }
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objMulti = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        if (!(objMulti.getScheduler() instanceof SchedulerSimpleMultiobjectiveJava)) {
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
        Log log = options.get(OptionsMessages.LOG);
        StopWatch timer = new StopWatch(true);
        log.send(MessagesGraphSolverIterative.ITERATING);
        double tolerance = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveMultiObjectiveScheduled = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        scheduler = (SchedulerSimpleMultiobjectiveJava) objectiveMultiObjectiveScheduled.getScheduler();
        Value stopStateRewards = objectiveMultiObjectiveScheduled.getStopStateRewards();
        Value cumulativeTransitionRewards = objectiveMultiObjectiveScheduled.getTransitionRewards();
        inputValues = objectiveMultiObjectiveScheduled.getValues();
        int[] numIterations = new int[1];
        if (iterMethod == IterationMethod.JACOBI) {
            mdpMultiobjectivescheduledJacobiJavaDouble(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler, numIterations);
        } else if (iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpMultiobjectivescheduledGaussseidelJavaDouble(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler, numIterations);
        } else {
            assert false;
        }
        log.send(MessagesGraphSolverIterative.ITERATING_DONE, numIterations[0],
                timer.getTimeSeconds());
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
            Value valuesV, SchedulerSimpleMultiobjectiveJava scheduler,
            int[] numIterationsResult) {
        Diff diffOp = getDiff();
        double[] values = ValueContentDoubleArray.getContent(valuesV);
        Arrays.fill(values, 0.0);
        double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
        double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        int numStates = graph.computeNumStates();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = scheduler.getDecisions();
        double weights[] = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT)
                .asSparseNondetOnlyNondet()
                .getContent());
        double[] presValues = values;
        double[] nextValues = new double[numStates];
        double distance;
        int iterations = 0;
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
            iterations++;
        } while (distance > tolerance / 2);
        System.arraycopy(presValues, 0, values, 0, numStates);
        numIterationsResult[0] = iterations;
    }

    private void mdpMultiobjectivescheduledGaussseidelJavaDouble(
            GraphExplicitSparseAlternate graph, Value stopRewardsV,
            Value transRewardsV,
            IterationStopCriterion stopCriterion, double tolerance,
            Value valuesV, SchedulerSimpleMultiobjectiveJava scheduler,
            int[] numIterationsResult) {
        Diff diffOp = getDiff();
        int numStates = graph.computeNumStates();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = scheduler.getDecisions();
        double[] stopRewards = ValueContentDoubleArray.getContent(stopRewardsV);
        double[] transRewards = ValueContentDoubleArray.getContent(transRewardsV);
        double[] values = ValueContentDoubleArray.getContent(valuesV);
        Arrays.fill(values, 0.0);
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT)
                .asSparseNondetOnlyNondet()
                .getContent());
        double distance;
        int iterations = 0;
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
            iterations++;
        } while (distance > tolerance / 2);
        numIterationsResult[0] = iterations;
    }
}
