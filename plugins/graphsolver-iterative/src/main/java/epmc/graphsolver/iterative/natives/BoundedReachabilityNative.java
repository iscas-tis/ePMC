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

import java.util.ArrayList;
import java.util.List;

import epmc.algorithms.FoxGlynn;
import epmc.error.UtilError;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitModifier;
import epmc.graph.explicit.GraphExplicitSparse;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedReachability;
import epmc.operator.OperatorMultiply;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.ProblemsUtil;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeDouble;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueContentDoubleArray;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;
import epmc.value.ValueReal;

import static epmc.graphsolver.iterative.UtilGraphSolverIterative.startWithInfoBounded;

// TODO reward-based stuff should be moved to rewards plugin

/**
 * Commonly used routines to solve graph-based problems using value iteration.
 * The routines provided here only work in the case that reals are implemented
 * using IEEE doubles. They are implemented in native code, and should run
 * relatively fast.
 * 
 * @author Ernst Moritz Hahn
 */
public final class BoundedReachabilityNative implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-bounded-reachability-native";
    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
    private ValueReal lambda;
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
        assert origGraph != null;
        Semantics semantics = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (!SemanticsCTMC.isCTMC(semantics)
                && !SemanticsDTMC.isDTMC(semantics)
                && !SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        if (!(objective instanceof GraphSolverObjectiveExplicitBoundedReachability)) {
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
        Semantics semantics = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (SemanticsContinuousTime.isContinuousTime(semantics)) {
            ctBoundedReachability();
        } else {
            dtBoundedReachability();
        }
        prepareResultValues();
    }

    private void prepareIterGraph() {
        assert origGraph != null;
        Semantics semanticsType = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        boolean uniformise = SemanticsContinuousTime.isContinuousTime(semanticsType) && (objective instanceof GraphSolverObjectiveExplicitBoundedReachability);
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedNodeProperties(origGraph.getNodeProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        List<BitSet> sinks = null;
        sinks = new ArrayList<>();
        GraphSolverObjectiveExplicitBoundedReachability bounded = (GraphSolverObjectiveExplicitBoundedReachability) objective;
        if (bounded.getZeroSet() != null) {
            sinks.add(bounded.getZeroSet());
        }
        sinks.add(bounded.getTarget());

        if (sinks != null) {
            builder.addSinks(sinks);
        }
        builder.setUniformise(uniformise);
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        Value unifRate = newValueWeight();
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeReal.get(), TypeReal.get());
        if (uniformise) {
            GraphExplicitModifier.uniformise(iterGraph, unifRate);
        }
        if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
            lambda = TypeReal.get().newValue();
            GraphSolverObjectiveExplicitBoundedReachability objectiveBounded = (GraphSolverObjectiveExplicitBoundedReachability) objective;
            Value time = objectiveBounded.getTime();
            multiply.apply(lambda, time, unifRate);
        }
        BitSet targets = null;
        if (objective instanceof GraphSolverObjectiveExplicitBoundedReachability) {
            GraphSolverObjectiveExplicitBoundedReachability objectiveBoundedReachability = (GraphSolverObjectiveExplicitBoundedReachability) objective;
            targets = objectiveBoundedReachability.getTarget();
        }
        if (targets != null) {
            assert this.inputValues == null;
            int numStates = iterGraph.computeNumStates();
            this.inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), numStates);
            for (int origNode = 0; origNode < origGraph.getNumNodes(); origNode++) {
                int iterNode = builder.inputToOutputNode(origNode);
                if (iterNode < 0) {
                    continue;
                }
                this.inputValues.set(targets.get(origNode) ? 1 : 0, iterNode);
            }
        }        
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

    private void dtBoundedReachability() {
        assert iterGraph != null;
        GraphSolverObjectiveExplicitBoundedReachability objectiveBoundedReachability = (GraphSolverObjectiveExplicitBoundedReachability) objective;
        ValueInteger time = ValueInteger.as(objectiveBoundedReachability.getTime());
        assert time.getInt() >= 0 : time.getInt();
        boolean min = objectiveBoundedReachability.isMin();
        if (isSparseMarkovNative(iterGraph)) {
            dtmcBoundedNative(time.getInt(), asSparseMarkov(iterGraph), inputValues);            
        } else if (isSparseMDPNative(iterGraph)) {
            mdpBoundedNative(time.getInt(), asSparseNondet(iterGraph), min, inputValues);                        
        } else {
            assert false : isSparseMarkov(iterGraph) + " " + isSparseNondet(iterGraph);
        }
    }

    private void ctBoundedReachability() {
        assert iterGraph != null : "iterGraph == null";
        assert lambda != null : "lambda == null";
        assert ValueReal.is(lambda) : lambda;
        Options options = Options.get();
        ValueReal precision = UtilValue.newValue(TypeReal.get(), options.getString(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE));
        FoxGlynn foxGlynn = new FoxGlynn(lambda, precision);
        if (isSparseMarkovNative(iterGraph)) {
            ctmcBoundedNative(asSparseMarkov(iterGraph), inputValues, foxGlynn);
        } else {
            assert false;
        }
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

    private static void ctmcBoundedNative(GraphExplicitSparse graph,
            Value values, FoxGlynn foxGlynn) {
        int numStates = graph.computeNumStates();
        double[] fg = ValueContentDoubleArray.getContent(foxGlynn.getArray());
        int left = foxGlynn.getLeft();
        int right = foxGlynn.getRight();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        int code = startWithInfoBounded(right, info -> {
            return IterationNative.double_ctmc_bounded(fg, left, right, numStates, stateBounds, targets, weights, valuesMem, info.createNumIterations());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void dtmcBoundedNative(int bound,
            GraphExplicitSparse graph, Value values) {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        int code = startWithInfoBounded(bound, info -> {
            return IterationNative.double_dtmc_bounded(bound, numStates, stateBounds, targets, weights, valuesMem, info.createNumIterations());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpBoundedNative(int bound,
            GraphExplicitSparseAlternate graph, boolean min,
            Value values) {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        int code = startWithInfoBounded(bound, info -> {
            return IterationNative.double_mdp_bounded(bound, numStates, stateBounds,
                nondetBounds, targets, weights, min ? 1 : 0, valuesMem, info.createNumIterations());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
