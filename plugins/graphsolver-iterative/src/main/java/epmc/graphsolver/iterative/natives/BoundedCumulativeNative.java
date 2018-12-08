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

import epmc.error.UtilError;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsCTMC;
import epmc.graph.SemanticsContinuousTime;
import epmc.graph.SemanticsDTMC;
import epmc.graph.SemanticsMDP;
import epmc.graph.SemanticsNonDet;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitModifier;
import epmc.graph.explicit.GraphExplicitSparse;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedCumulative;
import epmc.operator.OperatorMultiply;
import epmc.options.Options;
import epmc.util.ProblemsUtil;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
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

import java.util.Arrays;

import epmc.algorithms.FoxGlynn;

// TODO reward-based stuff should be moved to rewards plugin

/**
 * Commonly used routines to solve graph-based problems using value iteration.
 * The routines provided here only work in the case that reals are implemented
 * using IEEE doubles. They are implemented in native code, and should run
 * relatively fast.
 * 
 * @author Ernst Moritz Hahn
 */
public final class BoundedCumulativeNative implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-bounded-cumulative-native";
    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
    private GraphSolverObjectiveExplicit objective;
    private GraphBuilderExplicit builder;
    private ValueArrayAlgebra cumulativeStateRewards;
    private ValueReal lambda;
    private Value unifRate;

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
        if (!(objective instanceof GraphSolverObjectiveExplicitBoundedCumulative)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        prepareIterGraph();
        if (objective instanceof GraphSolverObjectiveExplicitBoundedCumulative) {
            boundedCumulative();
        }
        prepareResultValues();
    }

    private void prepareIterGraph() {
        assert origGraph != null;
        Semantics semanticsType = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        boolean uniformise = SemanticsContinuousTime.isContinuousTime(semanticsType) && (objective instanceof GraphSolverObjectiveExplicitBoundedCumulative);
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedNodeProperties(origGraph.getNodeProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        builder.setUniformise(uniformise);
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        unifRate = newValueWeight();
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeReal.get(), TypeReal.get());
        if (uniformise) {
            GraphExplicitModifier.uniformise(iterGraph, unifRate);
        }
        if (SemanticsCTMC.isCTMC(semanticsType)) {
            lambda = TypeReal.get().newValue();
            GraphSolverObjectiveExplicitBoundedCumulative objectiveBounded = (GraphSolverObjectiveExplicitBoundedCumulative) objective;
            Value time = objectiveBounded.getTime();
            multiply.apply(lambda, time, unifRate);
        }

        cumulativeStateRewards = null;
        GraphSolverObjectiveExplicitBoundedCumulative objectiveBoundedCumulative = (GraphSolverObjectiveExplicitBoundedCumulative) objective;
        cumulativeStateRewards = objectiveBoundedCumulative.getStateRewards();
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

    private void boundedCumulative() {
        assert iterGraph != null;
        GraphSolverObjectiveExplicitBoundedCumulative objectiveBoundedCumulative = (GraphSolverObjectiveExplicitBoundedCumulative) objective;
        ValueInteger time = ValueInteger.as(objectiveBoundedCumulative.getTime());
        assert time.getInt() >= 0;
        boolean min = objectiveBoundedCumulative.isMin();
        inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), iterGraph.computeNumStates());
        Semantics semantics = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (SemanticsDTMC.isDTMC(semantics)) {
            dtmcBoundedCumulativeNative(time.getInt(), asSparseMarkov(iterGraph), inputValues, cumulativeStateRewards);
        } else if (SemanticsCTMC.isCTMC(semantics)) {
            ValueReal precision = UtilValue.newValue(TypeReal.get(), Options.get().getString(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE));
            FoxGlynn foxGlynn = new FoxGlynn(lambda, precision);
            double doubleUnif = ValueReal.as(unifRate).getDouble();
            ctmcBoundedCumulativeNative(asSparseMarkov(iterGraph), inputValues, cumulativeStateRewards, foxGlynn, doubleUnif);
        } else if (isSparseMDPNative(iterGraph)) {
            mdpBoundedCumulativeNative(time.getInt(), asSparseNondet(iterGraph), min, inputValues, cumulativeStateRewards);
        } else {
            assert false : iterGraph.getClass();
        }
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

    private static GraphExplicitSparse asSparseMarkov(GraphExplicit graph) {
        return (GraphExplicitSparse) graph;
    }

    /* implementation/native call of/to iteration algorithms */    

    private static void dtmcBoundedCumulativeNative(int bound,
            GraphExplicitSparse graph, Value values, Value cumul) {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);

        int code = startWithInfoBounded(bound, info -> {
            return IterationNative.double_dtmc_bounded_cumulative(bound, numStates, stateBounds, targets, weights, valuesMem, cumulMem, info.createNumIterations());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void ctmcBoundedCumulativeNative(GraphExplicitSparse graph,
            Value values, ValueArrayAlgebra rewards, FoxGlynn foxGlynn, double doubleUnif) {
        int numStates = graph.computeNumStates();
        double[] fg = ValueContentDoubleArray.getContent(foxGlynn.getArray());
        int left = foxGlynn.getLeft();
        int right = foxGlynn.getRight();
        double[] cumulFg = new double[right];
        for (int i = left > 0 ? left : 1; i < right; i++) {
            cumulFg[i] = cumulFg[i - 1] + fg[i - left];
        }
        for (int i = 0; i < cumulFg.length; i++) {
            cumulFg[i] = (1.0 - cumulFg[i]) / doubleUnif;
        }
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] rewardsMem = ValueContentDoubleArray.getContent(rewards);
        for (int i = 0; i < rewardsMem.length; i++) {
            valuesMem[i] = rewardsMem[i];
        }
        int code = startWithInfoBounded(right, info -> {
            return IterationNative.double_ctmc_bounded(cumulFg, 0, right, numStates, stateBounds, targets, weights, valuesMem, info.createNumIterations());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpBoundedCumulativeNative(int bound,
            GraphExplicitSparseAlternate graph, boolean min,
            Value values, Value cumul) {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);

        int code = startWithInfoBounded(bound, info -> {
            return IterationNative.double_mdp_bounded_cumulative(bound, numStates, stateBounds,
                nondetBounds, targets, weights, min ? 1 : 0, valuesMem, cumulMem,
                        info.createNumIterations());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
