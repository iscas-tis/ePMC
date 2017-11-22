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

package epmc.coalition.graphsolver;

import java.util.ArrayList;
import java.util.List;

import epmc.error.UtilError;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Player;
import epmc.graph.Scheduler;
import epmc.graph.Semantics;
import epmc.graph.SemanticsSMG;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.SchedulerSimple;
import epmc.graph.explicit.SchedulerSimpleArray;
import epmc.graph.explicit.SchedulerSimpleSettable;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.MessagesGraphSolverIterative;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDistance;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMultiply;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.JNATools;
import epmc.util.ProblemsUtil;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeBoolean;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueContentDoubleArray;
import epmc.value.ValueObject;
import epmc.value.ValueReal;
import epmc.value.ValueSetString;

/**
 * Iterative solver to solve game-related graph problems.
 * 
 * @author Ernst Moritz Hahn
 */
public final class GraphSolverIterativeCoalitionNative implements GraphSolverExplicit {
    /** Identifier of the iteration-based graph game solver. */
    public static String IDENTIFIER = "graph-solver-iterative-coalition-native";

    /**
     * Class to interface with native code implementing value iteration.
     * 
     * @author Ernst Moritz Hahn
     */
    private static final class IterationNative {
        native static int double_tpg_unbounded_gaussseidel(int relative,
                double precision, int maxEnd, int minEnd,
                int[] stateBounds, int[] nondetBounds,
                int[] targets, double[] weights, double[] values);

        native static int double_tpg_unbounded_jacobi(int relative,
                double precision, int maxEnd, int minEnd,
                int[] stateBounds, int[] nondetBounds,
                int[] targets, double[] weights, double[] values);

        private final static String NATIVE_LIBRARY_NAME = "valueiterationcoalition";
        private final static boolean LOADED =
                JNATools.registerLibrary(IterationNative.class, NATIVE_LIBRARY_NAME);

        /** Return code indicating success of a native method. */
        private final static int EPMC_ERROR_SUCCESS = 0;
        /** Return code indicating failure of a native method. */
        private final static int EPMC_ERROR_OUT_OF_MEMORY = 1;
    }

    /** Original graph. */
    private GraphExplicit origGraph;
    /** Graph used for iteration, derived from original graph. */
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
    /** Number of iterations needed TODO check if indeed used*/
    private int numIterations;
    private GraphSolverObjectiveExplicit objective;
    private GraphBuilderExplicit builder;
    private int maxEnd;

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
        Semantics semantics = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (!SemanticsSMG.isSMG(semantics)) {
            return false;
        }
        if (!(objective instanceof GraphSolverObjectiveExplicitUnboundedReachabilityGame)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        prepareIterGraph();
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachabilityGame) {
            unboundedReachability();
            prepareResultValues();
            GraphSolverObjectiveExplicitUnboundedReachabilityGame objUB = (GraphSolverObjectiveExplicitUnboundedReachabilityGame) objective;
            if (objUB.isComputeScheduler()) {
                SchedulerSimpleSettable strategy = new SchedulerSimpleArray(origGraph);
                computeStrategy(strategy, objUB.getTarget(), outputValues);
                objUB.setScheduler(strategy);
            }
        } else {
            assert false;
        }
    }

    private void prepareIterGraph() {
        assert origGraph != null;

        BitSet playerEven = UtilBitSet.newBitSetUnbounded();
        BitSet playerOdd = UtilBitSet.newBitSetUnbounded();
        BitSet playerStochastic = UtilBitSet.newBitSetUnbounded();
        NodeProperty playerProp = origGraph.getNodeProperty(CommonProperties.PLAYER);
        int origNumNodes = origGraph.getNumNodes();
        maxEnd = 0;
        for (int node = 0; node < origNumNodes; node++) {
            Player player = playerProp.getEnum(node);
            playerEven.set(node, player == Player.ONE);
            maxEnd += player == Player.ONE ? 1 : 0;
            playerOdd.set(node, player == Player.TWO);
            playerStochastic.set(node, player == Player.STOCHASTIC);
        }
        List<BitSet> parts = new ArrayList<>();
        parts.add(playerEven);
        parts.add(playerOdd);
        parts.add(playerStochastic);
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        builder.setParts(parts);
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        BitSet targets = null;
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedReachabilityGame) {
            GraphSolverObjectiveExplicitUnboundedReachabilityGame objectiveUnboundedReachability = (GraphSolverObjectiveExplicitUnboundedReachabilityGame) objective;
            targets = objectiveUnboundedReachability.getTarget();
        }
        assert this.inputValues == null;
        int numStates = iterGraph.computeNumStates();
        this.inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), numStates);
        for (int origNode = 0; origNode < origNumNodes; origNode++) {
            int iterNode = builder.inputToOutputNode(origNode);
            if (iterNode < 0) {
                continue;
            }
            this.inputValues.set(targets.get(origNode) ? 1 : 0, iterNode);
        }
    }

    private void prepareResultValues() {
        TypeAlgebra typeWeight = TypeWeight.get();
        TypeArrayAlgebra typeArrayWeight = typeWeight.getTypeArray();
        this.outputValues = UtilValue.newArray(typeArrayWeight, origGraph.getNumNodes());
        ValueAlgebra val = typeWeight.newValue();
        ValueAlgebra get = typeWeight.newValue();
        ValueAlgebra weighted = typeWeight.newValue();
        int origNumNodes = origGraph.getNumNodes();
        GraphSolverObjectiveExplicitUnboundedReachabilityGame objective = (GraphSolverObjectiveExplicitUnboundedReachabilityGame) this.objective;
        NodeProperty playerProp = origGraph.getNodeProperty(CommonProperties.PLAYER);
        EdgeProperty weightProp = origGraph.getEdgeProperty(CommonProperties.WEIGHT);
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        for (int origNode = 0; origNode < origNumNodes; origNode++) {
            Player player = playerProp.getEnum(origNode);
            int iterState = builder.inputToOutputNode(origNode);
            if (iterState == -1) {
                continue;
            }
            inputValues.get(val, iterState);
            outputValues.set(val, origNode);
        }
        for (int origNode = 0; origNode < origNumNodes; origNode++) {
            Player player = playerProp.getEnum(origNode);
            if (player == Player.STOCHASTIC) {
                val.set(0);
                int numSucc = origGraph.getNumSuccessors(origNode);

                for (int succ = 0; succ < numSucc; succ++) {
                    outputValues.get(get, origGraph.getSuccessorNode(origNode, succ));
                    multiply.apply(weighted, get, weightProp.get(origNode, succ));
                    add.apply(val, val, weighted);
                }
                outputValues.set(val, origNode);
            }
        }
        objective.setResult(outputValues);
    }

    private void unboundedReachability() {
        Options options = Options.get();
        Log log = options.get(OptionsMessages.LOG);
        StopWatch timer = new StopWatch(true);
        log.send(MessagesGraphSolverIterative.ITERATING);
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        numIterations = 0;
        double precision = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        if (isSparseTPGNative(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            tpgUnboundedJacobiNative(asSparseNondet(iterGraph), inputValues, stopCriterion, precision);
        } else if (isSparseTPGNative(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            //        	System.out.println(iterGraph);
            tpgUnboundedGaussseidelNative(asSparseNondet(iterGraph), inputValues, stopCriterion, precision);
        } else {
            assert false : iterGraph.getClass();
        }
        log.send(MessagesGraphSolverIterative.ITERATING_DONE, numIterations,
                timer.getTimeSeconds());
    }

    /* auxiliary methods */

    private static boolean isSparseNondet(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparseAlternate;
    }

    private static boolean isSparseTPGNative(GraphExplicit graph) {
        if (!isSparseNondet(graph)) {
            return false;
        }
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsSMG.isSMG(semantics)) {
            return false;
        }
        return true;
    }

    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }

    /* implementation/native call of/to iteration algorithms */    

    private void tpgUnboundedGaussseidelNative(
            GraphExplicitSparseAlternate graph,
            Value values, IterationStopCriterion stopCriterion,
            double precision) {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);

        int code = IterationNative.double_tpg_unbounded_gaussseidel(relative, precision, maxEnd,
                numStates, stateBounds, nondetBounds, targets, weights, valuesMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private void tpgUnboundedJacobiNative(
            GraphExplicitSparseAlternate graph,
            Value values, IterationStopCriterion stopCriterion,
            double precision) {
        int relative = stopCriterion == IterationStopCriterion.RELATIVE ? 1 : 0;
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);

        int code = IterationNative.double_tpg_unbounded_jacobi(relative, precision, maxEnd,
                numStates, stateBounds, nondetBounds, targets, weights,
                valuesMem);
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private void computeStrategy(SchedulerSimpleSettable strategy,
            BitSet target, ValueArrayAlgebra values)
    {
        assert strategy != null;
        assert target != null;
        NodeProperty playerProperty = origGraph.getNodeProperty(CommonProperties.PLAYER);

        origGraph.computePredecessors();

        BitSet newNodes = target.clone();
        BitSet previousNodes = UtilBitSet.newBitSetBounded(origGraph.getNumNodes());
        BitSet seen = UtilBitSet.newBitSetBounded(origGraph.getNumNodes());
        seen.or(target);
        ValueAlgebra nodeValue = newValueWeight();
        ValueAlgebra predValue = newValueWeight();
        ValueReal tolerance = TypeReal.get().newValue();
        ValueSetString.as(tolerance).set(Double.toString(Options.get().getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE) * 4));
        OperatorEvaluator distance = ContextValue.get().getEvaluator(OperatorDistance.DISTANCE, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator lt = ContextValue.get().getEvaluator(OperatorLt.LT, TypeReal.get(), TypeReal.get());
        ValueReal distanceValue = TypeReal.get().newValue();
        ValueBoolean cmp = TypeBoolean.get().newValue();
        do {
            BitSet swap = previousNodes;
            previousNodes = newNodes;
            newNodes = swap;
            newNodes.clear();
            for (int node = previousNodes.nextSetBit(0); node >= 0;
                    node = previousNodes.nextSetBit(node+1)) {
                Player player = playerProperty.getEnum(node);
                /* player even or odd node - predecessors are distributions */
                if (player == Player.ONE || player == Player.TWO) {
                    for (int predNr = 0; predNr < origGraph.getProperties().getNumPredecessors(node); predNr++) {
                        int pred = origGraph.getProperties().getPredecessorNode(node, predNr);
                        if (!seen.get(pred)) {
                            strategy.set(pred, origGraph.getSuccessorNumber(pred, node));
                            seen.set(pred);
                            newNodes.set(pred);
                        }
                    }
                } else if (player == Player.STOCHASTIC) {
                    /* distribution node - predecessors and successors are even or odd */
                    values.get(nodeValue, node);
                    /*
                	nodeValue.set(0);
                	for (int succNr = 0; succNr < origGraph.getNumSuccessors(); succNr++) {
                		int succNode = origGraph.getSuccessorNode(succNr);
                    	values.get(succValue, succNode);
                		Value weight = weightProperty.get(succNr);
                		weighted.multiply(weight, succValue);
                		nodeValue.add(nodeValue, weighted);
                	}
                     */
                    for (int predNr = 0; predNr < origGraph.getProperties().getNumPredecessors(node); predNr++) {
                        int pred = origGraph.getProperties().getPredecessorNode(node, predNr);
                        values.get(predValue, pred);
                        distance.apply(distanceValue, predValue, nodeValue);
                        lt.apply(cmp, distanceValue, tolerance);
                        if (!seen.get(pred) && cmp.getBoolean()) {
                            strategy.set(pred, origGraph.getSuccessorNumber(pred, node));
                            seen.set(pred);
                            newNodes.set(pred);
                        }
                    }
                } else {
                    assert false;
                }
            }
        } while (!newNodes.isEmpty());
        for (int node = 0; node < origGraph.getNumNodes(); node++) {
            Player player = playerProperty.getEnum(node);
            if ((player == Player.ONE || player == Player.TWO)
                    && !seen.get(node) && !target.get(node)) {
                values.get(nodeValue, node);
                strategy.set(node, 0);
            }
        }
        assert assertStrategyOK(strategy, target);
    }

    private boolean assertStrategyOK(SchedulerSimple strategy, BitSet target) {
        /* make sure that we indeed computed the strategy correctly */
        NodeProperty playerProperty = origGraph.getNodeProperty(CommonProperties.PLAYER);
        for (int node = 0; node < origGraph.getNumNodes(); node++) {
            Player player = playerProperty.getEnum(node);
            assert player == Player.STOCHASTIC || target.get(node) || strategy.getDecision(node) != Scheduler.UNSET : node;
        }
        return true;
    }

    private ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
