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

public final class GraphSolverIterativeMultiObjectiveWeightedNative implements GraphSolverExplicit {
    public static final String IDENTIFIER = "graph-solver-iterative-multiobjective-weighted-native";

    private static final class IterationNative {
        native static int double_mdp_multiobjectiveweighted_jacobi(int relative,
                double precision, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, double[] stopRewards, double[] transRewardsMem,
                double[] values, int[] scheduler, int[] numIterations);

        native static int double_mdp_multiobjectiveweighted_gaussseidel(int relative,
                double precision, int numStates,
                int[] stateBounds, int[] nondetBounds, int[] targets,
                double[] weights, double[] stopRewards, double[] transRewards,
                double[] values, int[] scheduler, int[] numIterations);

        private final static boolean loaded =
                JNATools.registerLibrary(IterationNative.class, "valueiterationmultiobjective");

        private final static int EPMC_ERROR_SUCCESS = 0;
        private final static int EPMC_ERROR_OUT_OF_ByteBuffer = 1;
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
        if (!(objective instanceof GraphSolverObjectiveExplicitMultiObjectiveWeighted)) {
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
        Log log = options.get(OptionsMessages.LOG);
        StopWatch timer = new StopWatch(true);
        log.send(MessagesGraphSolverIterative.ITERATING);
        double tolerance = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        GraphSolverObjectiveExplicitMultiObjectiveWeighted objectiveMultiObjectiveWeighted = (GraphSolverObjectiveExplicitMultiObjectiveWeighted) objective;
        Value cumulativeTransitionRewards = objectiveMultiObjectiveWeighted.getTransitionRewards();
        Value stopStateRewards = objectiveMultiObjectiveWeighted.getStopStateReward();
        scheduler = new SchedulerSimpleMultiobjectiveJava((GraphExplicitSparseAlternate) iterGraph);
        objectiveMultiObjectiveWeighted.setScheduler(scheduler);
        inputValues = objectiveMultiObjectiveWeighted.getValues();
        int[] numIterations = new int[1];
        if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpMultiobjectiveweightedJacobiNative(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler, numIterations);
        } else if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpMultiobjectiveweightedGaussseidelNative(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler, numIterations);
        } else {
            assert false;
        }
        log.send(MessagesGraphSolverIterative.ITERATING_DONE, numIterations[0],
                timer.getTimeSeconds());
    }

    /* auxiliary methods */

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

    private static void mdpMultiobjectiveweightedJacobiNative(
            GraphExplicitSparseAlternate graph, Value stopRewards,
            Value transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            Value values, SchedulerSimpleMultiobjectiveJava scheduler,
            int[] numIterationsResult) {
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

        int code = IterationNative.double_mdp_multiobjectiveweighted_jacobi(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                stopRewardsMem, transRewardsMem, valuesMem, schedulerMem, numIterationsResult);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_ByteBuffer, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpMultiobjectiveweightedGaussseidelNative(
            GraphExplicitSparseAlternate graph, Value stopRewards,
            Value transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            Value values, SchedulerSimpleMultiobjectiveJava scheduler,
            int[] numIterationsResult) {
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

        int code = IterationNative.double_mdp_multiobjectiveweighted_gaussseidel(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                stopRewardsMem, transRewardsMem, valuesMem, schedulerMem,
                numIterationsResult);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_ByteBuffer, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;        
    }
}
