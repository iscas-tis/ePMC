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

package epmc.graphsolver.iterative.java;

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
import epmc.graphsolver.iterative.Info;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedCumulative;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;

import static epmc.graphsolver.iterative.UtilGraphSolverIterative.startWithInfoBoundedVoid;

// TODO reward-based stuff should be moved to rewards plugin

/**
 * Commonly used routines to solve graph-based problems using value iteration.
 * The implementations provided here should work for any representation of
 * reals, not just doubles. Due to their generality, they are not that
 * efficient. Their purpose is mainly to be used with representations of
 * reals the computations of which are not directly implemented in hardware
 * (e.g. mpfr numbers). Here, the additional overhead does not matter that
 * much: the majority of time is spent in rather slow computations involving
 * real values, such that the additional time added from generality does not
 * contribute too much to the overall runtime.
 * 
 * @author Ernst Moritz Hahn
 */
public final class BoundedCumulativeJava implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-bounded-cumulative-java";

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
        if (!(objective instanceof GraphSolverObjectiveExplicitBoundedCumulative)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        prepareIterGraph();
        boundedCumulative();
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
        Value unifRate = newValueWeight();
        if (uniformise) {
            GraphExplicitModifier.uniformise(iterGraph, unifRate);
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
        startWithInfoBoundedVoid(time.getInt(), info -> {
            if (isSparseMarkovJava(iterGraph)) {
                dtmcBoundedCumulativeJava(info, time.getInt(), asSparseMarkov(iterGraph), inputValues, cumulativeStateRewards);
            } else if (isSparseMDPJava(iterGraph)) {
                mdpBoundedCumulativeJava(info, time.getInt(), asSparseNondet(iterGraph), min, inputValues, cumulativeStateRewards);
            } else {
                assert false : iterGraph.getClass();
            }
        });
    }

    /* auxiliary methods */

    private static boolean isSparseNondet(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparseAlternate;
    }

    private static boolean isSparseMarkov(GraphExplicit graph) {
        return graph instanceof GraphExplicitSparse;
    }

    private static boolean isSparseMDPJava(GraphExplicit graph) {
        if (!isSparseNondet(graph)) {
            return false;
        }
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        return true;
    }

    private static boolean isSparseMarkovJava(GraphExplicit graph) {
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

    private void dtmcBoundedCumulativeJava(Info info, int bound,
            GraphExplicitSparse graph, ValueArray values, ValueArray cumul) {
        int numStates = graph.computeNumStates();
        ValueArray presValues = values;
        ValueArray nextValues = UtilValue.newArray(values.getType(), numStates);
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.as(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        Value presStateProb = newValueWeight();
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setArray = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get().getTypeArray(), TypeWeight.get().getTypeArray());
        for (int step = 0; step < bound; step++) {
            info.setNumIterations(step);
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                cumul.get(nextStateProb, state);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    multiply.apply(weighted, succStateProb, weight);
                    add.apply(nextStateProb, nextStateProb, weighted);
                }
                presValues.get(presStateProb, state);
                nextValues.set(nextStateProb, state);
            }
            ValueArray swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        }
        setArray.apply(values, presValues);
    }

    private void mdpBoundedCumulativeJava(Info info, int bound,
            GraphExplicitSparseAlternate graph, boolean min,
            ValueArrayAlgebra values, ValueArray cumul) {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.as(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueAlgebra optInitValue = min ? UtilValue.newValue(typeWeight, UtilValue.POS_INF) : UtilValue.newValue(typeWeight, UtilValue.NEG_INF);
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        OperatorEvaluator minEv = ContextValue.get().getEvaluator(OperatorMin.MIN, nextStateProb.getType(), choiceNextStateProb.getType());
        OperatorEvaluator maxEv = ContextValue.get().getEvaluator(OperatorMax.MAX, nextStateProb.getType(), choiceNextStateProb.getType());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setArray = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get().getTypeArray(), TypeWeight.get().getTypeArray());
        for (int step = 0; step < bound; step++) {
            info.setNumIterations(step);
            for (int state = 0; state < numStates; state++) {
                presValues.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                set.apply(nextStateProb, optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    cumul.get(choiceNextStateProb, nondetNr);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        multiply.apply(weighted, weight, succStateProb);
                        add.apply(choiceNextStateProb, choiceNextStateProb, weighted);
                    }
                    if (min) {
                        minEv.apply(nextStateProb, nextStateProb, choiceNextStateProb);
                    } else {
                        maxEv.apply(nextStateProb, nextStateProb, choiceNextStateProb);
                    }
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        }
        setArray.apply(values, presValues);
    }

    private static ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
