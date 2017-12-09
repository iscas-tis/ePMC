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

import epmc.error.UtilError;
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
import epmc.options.Options;
import epmc.util.JNATools;
import epmc.util.ProblemsUtil;
import epmc.util.StopWatch;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeWeight;
import epmc.value.Value;
import epmc.value.ValueArray;
import epmc.value.ValueContentDoubleArray;

public final class GraphSolverIterativeMultiObjectiveScheduledNative implements GraphSolverExplicit {
    public static final String IDENTIFIER = "graph-solver-iterative-multiobjective-scheduled-native";

    private static final class IterationNative {
        native static int double_mdp_multiobjectivescheduled_jacobi(int relative,
                double precision, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, double[] stopRewards, double[] transRewardsMem,
                double[] values, int[] scheduler, int[] numIterations);

        native static int double_mdp_multiobjectivescheduled_gaussseidel(int relative,
                double precision, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, double[] stopRewards, double[] transRewards,
                double[] values, int[] scheduler, int[] numIterations);
        private final static boolean loaded =
                JNATools.registerLibrary(IterationNative.class, "valueiterationmultiobjective");

        private final static int EPMC_ERROR_SUCCESS = 0;
        private final static int EPMC_ERROR_OUT_OF_MEMORY = 1;
    }

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
        if (!SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        Type typeWeight = TypeWeight.get();
        if (TypeDouble.is(typeWeight)) {
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
        if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpMultiobjectivescheduledJacobiNative(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler, numIterations);
        } else if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpMultiobjectivescheduledGaussseidelNative(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler, numIterations);
        } else {
            assert false;
        }
        log.send(MessagesGraphSolverIterative.ITERATING_DONE, numIterations[0],
                timer.getTimeSeconds());
    }

    private static boolean isSparseNondet(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparseAlternate;
    }

    private static boolean isSparseMDPNative(GraphExplicit graph) {
        if (!isSparseNondet(graph)) {
            return false;
        }
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        return true;
    }


    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }

    /* native call to iteration algorithms */    

    private static void mdpMultiobjectivescheduledJacobiNative(
            GraphExplicitSparseAlternate graph, Value stopRewards,
            Value transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            Value values, SchedulerSimpleMultiobjectiveJava scheduler, int[] numIterations) {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] stopRewardsMem = ValueContentDoubleArray.getContent(stopRewards);
        int[] schedulerMem = scheduler.getDecisions();
        double[] transRewardsMem = ValueContentDoubleArray.getContent(transRewards);

        int code = IterationNative.double_mdp_multiobjectivescheduled_jacobi(
                relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                stopRewardsMem, transRewardsMem, valuesMem, schedulerMem, numIterations);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;        
    }

    private static void mdpMultiobjectivescheduledGaussseidelNative(
            GraphExplicitSparseAlternate graph, Value stopRewards,
            Value transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            Value values, SchedulerSimpleMultiobjectiveJava scheduler, int[] numIterations) {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] stopRewardsMem = ValueContentDoubleArray.getContent(stopRewards);
        int[] schedulerMem = scheduler.getDecisions();
        double[] transRewardsMem = ValueContentDoubleArray.getContent(transRewards);

        int code = IterationNative.double_mdp_multiobjectivescheduled_gaussseidel(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                stopRewardsMem, transRewardsMem, valuesMem, schedulerMem, numIterations);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;        
    }
}
