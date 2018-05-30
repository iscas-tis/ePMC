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

import epmc.algorithms.FoxGlynn;
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
import epmc.graphsolver.iterative.Info;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBounded;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueInteger;
import epmc.value.ValueObject;
import epmc.value.ValueReal;

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
public final class BoundedJava implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-bounded-java";

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
        if (!(objective instanceof GraphSolverObjectiveExplicitBounded)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        prepareIterGraph();
        Semantics semantics = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        if (objective instanceof GraphSolverObjectiveExplicitBounded) {
            if (SemanticsContinuousTime.isContinuousTime(semantics)) {
                ctBounded();
            } else {
                bounded();
            }
        } else {
            assert false;
        }
        prepareResultValues();
    }

    // TODO can directly use original graph under certain circumstances
    private void prepareIterGraph() {
        assert origGraph != null;
        Semantics semanticsType = ValueObject.as(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
        boolean uniformise = SemanticsContinuousTime.isContinuousTime(semanticsType) && (objective instanceof GraphSolverObjectiveExplicitBounded);
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedNodeProperties(origGraph.getNodeProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        builder.setUniformise(uniformise);
        builder.setReorder();
        builder.build();
        iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        Value unifRate = newValueWeight();
        if (uniformise) {
            GraphExplicitModifier.uniformise(iterGraph, unifRate);
        }
        lambda = TypeReal.get().newValue();
        GraphSolverObjectiveExplicitBounded objectiveBounded = (GraphSolverObjectiveExplicitBounded) objective;
        Value time = objectiveBounded.getTime();
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        multiply.apply(lambda, time, unifRate);
        inputValues = objectiveBounded.getValues();
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

    private void bounded() {
        assert iterGraph != null;
        assert inputValues != null;
        GraphSolverObjectiveExplicitBounded objectiveBounded = (GraphSolverObjectiveExplicitBounded) objective;
        ValueInteger time = ValueInteger.as(objectiveBounded.getTime());
        assert time.getInt() >= 0;
        time.getInt();
        boolean min = objectiveBounded.isMin();
        startWithInfoBoundedVoid(time.getInt(), info -> {
            if (isSparseMarkovJava(iterGraph)) {
                dtmcBoundedJava(info, time.getInt(), asSparseMarkov(iterGraph), inputValues);
            } else if (isSparseMDPJava(iterGraph)) {
                mdpBoundedJava(info, time.getInt(), asSparseNondet(iterGraph), min, inputValues);            
            } else {
                assert false : isSparseMarkov(iterGraph) + " " + isSparseNondet(iterGraph);
            }
        });
    }

    private void ctBounded() {
        assert iterGraph != null : "iterGraph == null";
        assert inputValues != null : "inputValues == null";
        assert lambda != null : "lambda == null";
        assert ValueReal.is(lambda) : lambda;
        Options options = Options.get();

        ValueReal precision = UtilValue.newValue(TypeReal.get(), options.getString(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE));
        FoxGlynn foxGlynn = new FoxGlynn(lambda, precision);
        startWithInfoBoundedVoid(foxGlynn.getRight(), info -> {
            if (isSparseMarkovJava(iterGraph)) {
                ctmcBoundedJava(info, asSparseMarkov(iterGraph), inputValues, foxGlynn);
            } else {
                assert false;
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

    /* implementation of iteration algorithms */    

    private void ctmcBoundedJava(Info info, GraphExplicitSparse graph,
            ValueArray values, FoxGlynn foxGlynn) {
        ValueArrayAlgebra fg = foxGlynn.getArray();
        Value fgWeight = TypeReal.get().newValue();
        int numStates = graph.computeNumStates();
        ValueArrayAlgebra presValues = UtilValue.newArray(values.getType(), numStates);
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.as(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra value = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        Value zero = UtilValue.newValue(TypeReal.get(), 0);
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setArray = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get().getTypeArray(), TypeWeight.get().getTypeArray());
        int iter = 0;
        for (int i = foxGlynn.getRight() - foxGlynn.getLeft(); i >= 0; i--) {
            info.setNumIterations(iter);
            iter++;
            fg.get(fgWeight, i);
            for (int state = 0; state < numStates; state++) {
                values.get(value, state);
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                multiply.apply(nextStateProb, fgWeight, value);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    multiply.apply(weighted, weight, succStateProb);
                    add.apply(nextStateProb, nextStateProb, weighted);
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        }
        for (int i = foxGlynn.getLeft() - 1; i >= 0; i--) {
            info.setNumIterations(iter);
            iter++;
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                set.apply(nextStateProb, zero);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    multiply.apply(weighted, succStateProb, weight);
                    add.apply(nextStateProb, nextStateProb, weighted);
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = presValues;
            presValues = nextValues;
            nextValues = swap;            
        }
        setArray.apply(values, presValues);
    }

    private static void dtmcBoundedJava(Info info, int bound,
            GraphExplicitSparse graph, ValueArrayAlgebra values) {
        int numStates = graph.computeNumStates();
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.as(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra zero = UtilValue.newValue(values.getType().getEntryType(), 0);
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setArray = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get().getTypeArray(), TypeWeight.get().getTypeArray());
        for (int step = 0; step < bound; step++) {
            info.setNumIterations(step);
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                set.apply(nextStateProb, zero);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    multiply.apply(weighted, succStateProb, weight);
                    add.apply(nextStateProb, nextStateProb, weighted);
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        }
        setArray.apply(values, presValues);
    }

    private void mdpBoundedJava(Info info, int bound,
            GraphExplicitSparseAlternate graph, boolean min,
            ValueArrayAlgebra values) {
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
        ValueAlgebra zero = UtilValue.newValue(values.getType().getEntryType(), 0);
        Value optInitValue = min ? UtilValue.newValue(typeWeight, UtilValue.POS_INF) : UtilValue.newValue(typeWeight, UtilValue.NEG_INF);
        ValueArray presValues = values;
        ValueArray nextValues = UtilValue.newArray(values.getType(), numStates);
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
                    set.apply(choiceNextStateProb, zero);
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
            ValueArray swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        }
        setArray.apply(values, presValues);
    }

    private static ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
