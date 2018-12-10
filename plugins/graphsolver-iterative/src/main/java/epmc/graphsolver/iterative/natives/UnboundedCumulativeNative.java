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

package epmc.graphsolver.iterative.natives;

import java.util.List;

import epmc.error.UtilError;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Player;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsMDP;
import epmc.graph.SemanticsNonDet;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitModifier;
import epmc.graph.explicit.GraphExplicitSparse;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.NodeProperty;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.MessagesGraphSolverIterative;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedCumulative;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorSet;
import epmc.operator.OperatorSubtract;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.ProblemsUtil;
import epmc.util.StopWatch;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeDouble;
import epmc.value.TypeWeight;
import epmc.value.TypeWeightTransition;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueContentDoubleArray;
import epmc.value.ValueObject;

import static epmc.graphsolver.iterative.UtilGraphSolverIterative.startWithInfoUnbounded;

// TODO reward-based stuff should be moved to rewards plugin

/**
 * Commonly used routines to solve graph-based problems using value iteration.
 * The routines provided here only work in the case that reals are implemented
 * using IEEE doubles. They are implemented in native code, and should run
 * relatively fast.
 * 
 * @author Ernst Moritz Hahn
 */
public final class UnboundedCumulativeNative implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-unbounded-cumulative-native";
    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
    private GraphSolverObjectiveExplicit objective;
    private GraphBuilderExplicit builder;
    private ValueArrayAlgebra cumulativeStateRewards;

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
        assert origGraph != null;
        Semantics semantics = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (!SemanticsCTMC.isCTMC(semantics)
                && !SemanticsDTMC.isDTMC(semantics)
                && !SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        if (!(objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative)) {
            return false;
        }
        if (!TypeDouble.is(TypeWeight.get())) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        prepareIterGraph();
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
            unboundedCumulative();
        } else {
            assert false;
        }
        prepareResultValues();
    }

    private void prepareIterGraph() {
        assert origGraph != null;
        Semantics semanticsType = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        boolean embed = SemanticsContinuousTime.isContinuousTime(semanticsType) && (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative);
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedNodeProperties(origGraph.getNodeProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        List<BitSet> sinks = null;
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
            GraphSolverObjectiveExplicitUnboundedCumulative objectiveUnboundedCumulative = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
            sinks = objectiveUnboundedCumulative.getSinks();
        }

        if (sinks != null) {
            builder.addSinks(sinks);
        }
        builder.setUniformise(false);
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;

        GraphSolverObjectiveExplicitUnboundedCumulative objectiveUnboundedCumulative = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
        cumulativeStateRewards = objectiveUnboundedCumulative.getStateRewards();
        if (cumulativeStateRewards != null) {
            if (SemanticsNonDet.isNonDet(semanticsType)) {
                // TODO
            } else {
                int size = 0;
                for (int origNode = 0; origNode < origGraph.getNumNodes(); origNode++) {
                    int iterNode = builder.inputToOutputNode(origNode);
                    if (iterNode < 0) {
                        continue;
                    }
                    size++;
                }
                ValueArrayAlgebra cumulativeStateRewardsNew = UtilValue.newArray(cumulativeStateRewards.getType(), size);
                Value value = cumulativeStateRewards.getType().getEntryType().newValue();
                for (int origNode = 0; origNode < origGraph.getNumNodes(); origNode++) {
                    int iterNode = builder.inputToOutputNode(origNode);
                    if (iterNode < 0) {
                        continue;
                    }
                    cumulativeStateRewards.get(value, origNode);
                    cumulativeStateRewardsNew.set(value, iterNode);
                }
                cumulativeStateRewards = cumulativeStateRewardsNew;
            }
            if (embed) {
                fixRewards();
            }
        }
        if (embed) {
            GraphExplicitModifier.embed(iterGraph);
        }
    }

    private void fixRewards() {
        // TODO fix also for ctmdps
        ValueAlgebra zero = UtilValue.newValue(TypeWeightTransition.get(), 0);
        ValueAlgebra sum = TypeWeightTransition.get().newValue();
        OperatorEvaluator divide = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeWeight.get(), TypeWeight.get());
        ValueAlgebra weight = TypeWeightTransition.get().newValue();
        NodeProperty playerProp = iterGraph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty weightProp = iterGraph.getEdgeProperty(CommonProperties.WEIGHT);
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        for (int node = 0; node < iterGraph.getNumNodes(); node++) {
            Player player = playerProp.getEnum(node);
            if (player == Player.STOCHASTIC) {
                set.apply(sum, zero);
                for (int succNr = 0; succNr < iterGraph.getNumSuccessors(node); succNr++) {
                    add.apply(sum, sum, weightProp.get(node, succNr));
                }
                cumulativeStateRewards.get(weight, node);
                divide.apply(weight, weight, sum);
                cumulativeStateRewards.set(weight, node);                
            }
        }

        // TODO Auto-generated method stub
        
    }

    private void prepareResultValues() {
        TypeAlgebra typeWeight = TypeWeight.get();
        TypeArrayAlgebra typeArrayWeight = typeWeight.getTypeArray();
        this.outputValues = UtilValue.newArray(typeArrayWeight, origGraph.computeNumStates());
        Value val = typeWeight.newValue();
        int origStateNr = 0;
        for (int i = 0; i < origGraph.getNumNodes(); i++) {
            int iterState = builder.inputToOutputNode(i);
            if (iterState == -1) {
                continue;
            }
            inputValues.get(val, iterState);
            outputValues.set(val, origStateNr);
            origStateNr++;
        }
        objective.setResult(outputValues);
    }

    private void unboundedCumulative() {
        Options options = Options.get();
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        Log log = options.get(OptionsMessages.LOG);
        StopWatch timer = new StopWatch(true);
        log.send(MessagesGraphSolverIterative.ITERATING);
        int[] numIterations = new int[1];
        GraphSolverObjectiveExplicitUnboundedCumulative graphSolverObjectiveUnbounded = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
        inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), iterGraph.computeNumStates());
        boolean min = graphSolverObjectiveUnbounded.isMin();
        double precision = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        if (isSparseMarkovNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            dtmcUnboundedCumulativeJacobiNative(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision, cumulativeStateRewards, numIterations);
        } else if (isSparseMarkovNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            dtmcUnboundedCumulativeGaussseidelNative(asSparseMarkov(iterGraph), inputValues, stopCriterion, precision, cumulativeStateRewards, numIterations);
        } else if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpUnboundedCumulativeJacobiNative(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision, cumulativeStateRewards, numIterations);
        } else if (isSparseMDPNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpUnboundedCumulativeGaussseidelNative(asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision, cumulativeStateRewards, numIterations);
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

    private static boolean isSparseMarkov(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparse;
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

    private static boolean isSparseMarkovNative(GraphExplicit graph) {
        if (!isSparseMarkov(graph)) {
            return false;
        }
        return true;
    }

    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }

    private static GraphExplicitSparse asSparseMarkov(GraphExplicit graph) {
        return (GraphExplicitSparse) graph;
    }

    /* implementation/native call of/to iteration algorithms */    

    private static void dtmcUnboundedCumulativeJacobiNative(GraphExplicitSparse graph,
            Value values,
            IterationStopCriterion stopCriterion, double tolerance, Value cumul, int[] numIterations)
    {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);
        int code = startWithInfoUnbounded(info -> {
            return IterationNative.double_dtmc_unbounded_cumulative_jacobi(relative, tolerance, numStates, stateBounds, targets, weights, valuesMem, cumulMem, numIterations,
                    info.createNumIterations(), info.createDifference());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void dtmcUnboundedCumulativeGaussseidelNative(
            GraphExplicitSparse graph, Value values,
            IterationStopCriterion stopCriterion, double tolerance, Value cumul, int[] numIterations) {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);
        int code = startWithInfoUnbounded(info -> {
            return IterationNative.double_dtmc_unbounded_cumulative_gaussseidel
                    (relative, tolerance, numStates, stateBounds, targets, weights, valuesMem, cumulMem, numIterations,
                    info.createNumIterations(), info.createDifference());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpUnboundedCumulativeJacobiNative(
            GraphExplicitSparseAlternate graph, boolean min,
            Value values, IterationStopCriterion stopCriterion,
            double tolerance, Value cumul, int[] numIterations) {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);

        int code = startWithInfoUnbounded(info -> {
            return IterationNative.double_mdp_unbounded_cumulative_jacobi(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                min ? 1 : 0, valuesMem, cumulMem, numIterations,
                info.createNumIterations(), info.createDifference());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpUnboundedCumulativeGaussseidelNative(
            GraphExplicitSparseAlternate graph, boolean min,
            Value values, IterationStopCriterion stopCriterion,
            double tolerance, Value cumul, int[] numIterations) {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);

        int code = startWithInfoUnbounded(info -> {
            return IterationNative.double_mdp_unbounded_cumulative_gaussseidel(relative, tolerance,
                numStates, stateBounds, nondetBounds, targets, weights,
                min ? 1 : 0, valuesMem, cumulMem, numIterations,
                info.createNumIterations(), info.createDifference());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }
}
