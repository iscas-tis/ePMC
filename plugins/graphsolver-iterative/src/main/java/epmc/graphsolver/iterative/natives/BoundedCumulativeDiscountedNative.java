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
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedCumulativeDiscounted;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedCumulative;
import epmc.util.BitSet;
import epmc.util.ProblemsUtil;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
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
public final class BoundedCumulativeDiscountedNative implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-bounded-cumulative-discounted-native";
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
        if (!(objective instanceof GraphSolverObjectiveExplicitBoundedCumulativeDiscounted)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        prepareIterGraph();
        boundedCumulativeDiscounted();
        prepareResultValues();
    }

    private void prepareIterGraph() {
        assert origGraph != null;
        Semantics semanticsType = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        boolean uniformise = SemanticsContinuousTime.isContinuousTime(semanticsType) && (objective instanceof GraphSolverObjectiveExplicitBoundedCumulativeDiscounted);
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
        builder.setUniformise(uniformise);
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        Value unifRate = newValueWeight();
        if (uniformise) {
            GraphExplicitModifier.uniformise(iterGraph, unifRate);
        }

        cumulativeStateRewards = null;
        if (objective instanceof GraphSolverObjectiveExplicitBoundedCumulativeDiscounted) {
            GraphSolverObjectiveExplicitBoundedCumulativeDiscounted objectiveBoundedCumulativeDiscounted = (GraphSolverObjectiveExplicitBoundedCumulativeDiscounted) objective;
            cumulativeStateRewards = objectiveBoundedCumulativeDiscounted.getStateRewards();
        }
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

    private void boundedCumulativeDiscounted() {
        assert iterGraph != null;
        inputValues = UtilValue.newArray(TypeWeight.get().getTypeArray(), iterGraph.computeNumStates());
        GraphSolverObjectiveExplicitBoundedCumulativeDiscounted objectiveBoundedCumulativeDiscounted = (GraphSolverObjectiveExplicitBoundedCumulativeDiscounted) objective;
        ValueInteger time = ValueInteger.as(objectiveBoundedCumulativeDiscounted.getTime());
        assert time.getInt() >= 0;
        ValueReal discount = objectiveBoundedCumulativeDiscounted.getDiscount();
        assert discount != null;
        assert ValueReal.is(discount) || ValueInteger.is(discount);
        boolean min = objectiveBoundedCumulativeDiscounted.isMin();
        if (isSparseMarkovNative(iterGraph)) {
            dtmcBoundedCumulativeDiscountedNative(time.getInt(), discount, asSparseMarkov(iterGraph), inputValues, cumulativeStateRewards);
        } else if (isSparseMDPNative(iterGraph)) {
            mdpBoundedCumulativeDiscountedNative(time.getInt(), discount, asSparseNondet(iterGraph), min, inputValues, cumulativeStateRewards);
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

    private static void dtmcBoundedCumulativeDiscountedNative(int bound,
            ValueReal discount, GraphExplicitSparse graph, Value values, Value cumul)
    {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);
        double discountDouble = discount.getDouble();
        int code = startWithInfoBounded(bound, info -> {
            return IterationNative.double_dtmc_bounded_cumulative_discounted(bound, discountDouble, numStates, stateBounds, targets, weights, valuesMem, cumulMem, info.createNumIterations());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static void mdpBoundedCumulativeDiscountedNative(int bound,
            ValueReal discount, GraphExplicitSparseAlternate graph, boolean min,
            Value values, Value cumul) {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        double[] weights = ValueContentDoubleArray.getContent(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        double[] valuesMem = ValueContentDoubleArray.getContent(values);
        double[] cumulMem = ValueContentDoubleArray.getContent(cumul);
        double discountDouble = discount.getDouble();

        int code = startWithInfoBounded(bound, info -> {
            return IterationNative.double_mdp_bounded_cumulative_discounted(bound, discountDouble, numStates, stateBounds,
                nondetBounds, targets, weights, min ? 1 : 0, valuesMem, cumulMem, info.createNumIterations());
        });
        UtilError.ensure(code != IterationNative.EPMC_ERROR_OUT_OF_MEMORY, ProblemsUtil.INSUFFICIENT_NATIVE_MEMORY);
        assert code == IterationNative.EPMC_ERROR_SUCCESS;
    }

    private static ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
