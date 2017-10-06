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
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBounded;
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
import epmc.value.operator.OperatorMax;
import epmc.value.operator.OperatorMin;

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
        Semantics semantics = ValueObject.asObject(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
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
        Semantics semantics = ValueObject.asObject(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
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
        Semantics semanticsType = ValueObject.asObject(origGraph.getGraphProperty(CommonProperties.SEMANTICS)).getObject();
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
        lambda.multiply(time, unifRate);
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
        ValueInteger time = ValueInteger.asInteger(objectiveBounded.getTime());
        assert time.getInt() >= 0;
        time.getInt();
        boolean min = objectiveBounded.isMin();
        if (isSparseMarkovJava(iterGraph)) {
            dtmcBoundedJava(time.getInt(), asSparseMarkov(iterGraph), inputValues);
        } else if (isSparseMDPJava(iterGraph)) {
            mdpBoundedJava(time.getInt(), asSparseNondet(iterGraph), min, inputValues);            
        } else {
            assert false : isSparseMarkov(iterGraph) + " " + isSparseNondet(iterGraph);
        }
    }

    private void ctBounded() {
        assert iterGraph != null : "iterGraph == null";
        assert inputValues != null : "inputValues == null";
        assert lambda != null : "lambda == null";
        assert ValueReal.isReal(lambda) : lambda;
        Options options = Options.get();

        ValueReal precision = UtilValue.newValue(TypeReal.get(), options.getString(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE));
        FoxGlynn foxGlynn = new FoxGlynn(lambda, precision);
        if (isSparseMarkovJava(iterGraph)) {
            ctmcBoundedJava(asSparseMarkov(iterGraph), inputValues, foxGlynn);
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

    private void ctmcBoundedJava(GraphExplicitSparse graph,
            ValueArray values, FoxGlynn foxGlynn) {
        ValueArrayAlgebra fg = foxGlynn.getArray();
        Value fgWeight = foxGlynn.getTypeReal().newValue();
        int numStates = graph.computeNumStates();
        ValueArrayAlgebra presValues = UtilValue.newArray(values.getType(), numStates);
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra value = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        Value zero = foxGlynn.getTypeReal().getZero();
        for (int i = foxGlynn.getRight() - foxGlynn.getLeft(); i >= 0; i--) {
            fg.get(fgWeight, i);
            for (int state = 0; state < numStates; state++) {
                values.get(value, state);
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                nextStateProb.multiply(fgWeight, value);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(weight, succStateProb);
                    nextStateProb.add(nextStateProb, weighted);
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        }
        for (int i = foxGlynn.getLeft() - 1; i >= 0; i--) {
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                nextStateProb.set(zero);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(succStateProb, weight);
                    nextStateProb.add(nextStateProb, weighted);
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = presValues;
            presValues = nextValues;
            nextValues = swap;            
        }
        values.set(presValues);
    }

    private static void dtmcBoundedJava(int bound,
            GraphExplicitSparse graph, ValueArrayAlgebra values)
    {
        int numStates = graph.computeNumStates();
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra zero = values.getType().getEntryType().getZero();
        for (int step = 0; step < bound; step++) {
            for (int state = 0; state < numStates; state++) {
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                nextStateProb.set(zero);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    presValues.get(succStateProb, succState);
                    weighted.multiply(succStateProb, weight);
                    nextStateProb.add(nextStateProb, weighted);
                }
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = presValues;
            presValues = nextValues;
            nextValues = swap;
        }
        values.set(presValues);
    }

    private void mdpBoundedJava(int bound,
            GraphExplicitSparseAlternate graph, boolean min,
            ValueArrayAlgebra values) {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getStateBoundsJava();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra choiceNextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueAlgebra zero = values.getType().getEntryType().getZero();
        Value optInitValue = min ? typeWeight.getPosInf() : typeWeight.getNegInf();
        ValueArray presValues = values;
        ValueArray nextValues = UtilValue.newArray(values.getType(), numStates);
        OperatorEvaluator minEv = ContextValue.get().getOperatorEvaluator(OperatorMin.MIN, nextStateProb.getType(), choiceNextStateProb.getType());
        OperatorEvaluator maxEv = ContextValue.get().getOperatorEvaluator(OperatorMax.MAX, nextStateProb.getType(), choiceNextStateProb.getType());
        for (int step = 0; step < bound; step++) {
            for (int state = 0; state < numStates; state++) {
                presValues.get(presStateProb, state);
                int stateFrom = stateBounds[state];
                int stateTo = stateBounds[state + 1];
                nextStateProb.set(optInitValue);
                for (int nondetNr = stateFrom; nondetNr < stateTo; nondetNr++) {
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    choiceNextStateProb.set(zero);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        choiceNextStateProb.add(choiceNextStateProb, weighted);
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
        values.set(presValues);
    }

    private static ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
