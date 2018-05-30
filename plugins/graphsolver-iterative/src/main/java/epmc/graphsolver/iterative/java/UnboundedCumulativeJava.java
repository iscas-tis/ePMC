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

import java.util.List;

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
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.MessagesGraphSolverIterative;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedCumulative;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDistance;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMin;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.StopWatch;
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
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueObject;
import epmc.value.ValueReal;
import epmc.value.ValueSetString;

import static epmc.graphsolver.iterative.UtilGraphSolverIterative.startWithInfoUnboundedVoid;

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
public final class UnboundedCumulativeJava implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-unbounded-cumulative-java";

    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
    private GraphSolverObjectiveExplicit objective;
    private GraphBuilderExplicit builder;
    private ValueArrayAlgebra cumulativeStateRewards;

    private final OperatorEvaluator distanceEvaluator;
    private final OperatorEvaluator maxEvaluator;
    private final OperatorEvaluator divideEvaluator;
    private final OperatorEvaluator gtEvaluator;
    private final OperatorEvaluator isZeroEvaluator;
    private final ValueReal thisDistance;
    private final ValueReal zeroDistance;
    private final ValueBoolean cmp;

    public UnboundedCumulativeJava() {
        distanceEvaluator = ContextValue.get().getEvaluator(OperatorDistance.DISTANCE, TypeWeight.get(), TypeWeight.get());
        maxEvaluator = ContextValue.get().getEvaluator(OperatorMax.MAX, TypeReal.get(), TypeReal.get());
        divideEvaluator = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeReal.get(), TypeReal.get());
        isZeroEvaluator = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, TypeReal.get());
        gtEvaluator = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
        thisDistance = TypeReal.get().newValue();
        zeroDistance = TypeReal.get().newValue();
        cmp = TypeBoolean.get().newValue();
    }

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
        if (embed) {
            GraphExplicitModifier.embed(iterGraph);
        }

        cumulativeStateRewards = null;
        if (objective instanceof GraphSolverObjectiveExplicitUnboundedCumulative) {
            GraphSolverObjectiveExplicitUnboundedCumulative objectiveUnboundedCumulative = (GraphSolverObjectiveExplicitUnboundedCumulative) objective;
            cumulativeStateRewards = objectiveUnboundedCumulative.getStateRewards();
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
        startWithInfoUnboundedVoid(info -> {
            if (isSparseMarkovJava(iterGraph) && iterMethod == IterationMethod.JACOBI) {
                dtmcUnboundedCumulativeJacobiJava(info, asSparseMarkov(iterGraph), inputValues, stopCriterion, precision, cumulativeStateRewards, numIterations);
            } else if (isSparseMarkovJava(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
                dtmcUnboundedCumulativeGaussseidelJava(info, asSparseMarkov(iterGraph), inputValues, stopCriterion, precision, cumulativeStateRewards, numIterations);
            } else if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.JACOBI) {
                mdpUnboundedCumulativeJacobiJava(info, asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision, cumulativeStateRewards, numIterations);
            } else if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
                mdpUnboundedCumulativeGaussseidelJava(info, asSparseNondet(iterGraph), min, inputValues, stopCriterion, precision, cumulativeStateRewards, numIterations);
            } else {
                assert false;
            }
        });
        log.send(MessagesGraphSolverIterative.ITERATING_DONE, numIterations[0],
                timer.getTimeSeconds());
    }

    /* auxiliary methods */

    private void compDiff(ValueReal distance, ValueAlgebra previous,
            Value current, IterationStopCriterion stopCriterion) {
        if (stopCriterion == null) {
            return;
        }
        distanceEvaluator.apply(thisDistance, previous, current);
        ValueAlgebra zero = UtilValue.newValue(previous.getType(), 0);
        if (stopCriterion == IterationStopCriterion.RELATIVE) {
            distanceEvaluator.apply(zeroDistance, previous, zero);
            isZeroEvaluator.apply(cmp, zeroDistance);
            if (!cmp.getBoolean()) {
                divideEvaluator.apply(thisDistance, thisDistance, zeroDistance);
            }
        }
        maxEvaluator.apply(distance, distance, thisDistance);
    }

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

    private void dtmcUnboundedCumulativeJacobiJava(Info info, GraphExplicitSparse graph,
            ValueArray values,
            IterationStopCriterion stopCriterion, double tolerance, ValueArrayAlgebra cumul,
            int[] numIterationsResult) {
        int numStates = graph.computeNumStates();
        ValueArray presValues = values;
        ValueArray nextValues = UtilValue.newArray(values.getType(), numStates);
        ValueArray swap;
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.as(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueReal distance = TypeReal.get().newValue();
        int iterations = 0;
        ValueReal precisionValue = TypeReal.get().newValue();
        ValueSetString.as(precisionValue).set(Double.toString(tolerance / 2));
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setReal = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        OperatorEvaluator setArray = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get().getTypeArray(), TypeWeight.get().getTypeArray());
        ValueAlgebra realZero = UtilValue.newValue(TypeReal.get(), 0);
        do {
            setReal.apply(distance, realZero);
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
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                nextValues.set(nextStateProb, state);
            }
            swap = presValues;
            presValues = nextValues;
            nextValues = swap;
            info.setNumIterations(iterations);
            info.setDifference(distance);
            iterations++;
            gtEvaluator.apply(cmp, distance, precisionValue);
        } while (cmp.getBoolean());
        numIterationsResult[0] = iterations;
        setArray.apply(values, presValues);
    }

    private void dtmcUnboundedCumulativeGaussseidelJava(Info info, GraphExplicitSparse graph,
            ValueArrayAlgebra values,
            IterationStopCriterion stopCriterion, double tolerance, ValueArrayAlgebra cumul,
            int[] numIterations) {
        int numStates = graph.computeNumStates();
        int[] stateBounds = graph.getBoundsJava();
        int[] targets = graph.getTargetsJava();
        ValueArrayAlgebra weights = ValueArrayAlgebra.as(graph.getEdgeProperty(CommonProperties.WEIGHT).getContent());
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueReal distance = TypeReal.get().newValue();
        int iterations = 0;
        ValueReal precisionValue = TypeReal.get().newValue();
        ValueSetString.as(precisionValue).set(Double.toString(tolerance / 2));
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setReal = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        ValueAlgebra realZero = UtilValue.newValue(TypeReal.get(), 0);
        do {
            setReal.apply(distance, realZero);
            for (int state = 0; state < numStates; state++) {
                values.get(presStateProb, state);
                int from = stateBounds[state];
                int to = stateBounds[state + 1];
                cumul.get(nextStateProb, state);
                for (int succ = from; succ < to; succ++) {
                    weights.get(weight, succ);
                    int succState = targets[succ];
                    values.get(succStateProb, succState);
                    multiply.apply(weighted, weight, succStateProb);
                    add.apply(nextStateProb, nextStateProb, weighted);
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                values.set(nextStateProb, state);
            }
            info.setNumIterations(iterations);
            info.setDifference(distance);
            iterations++;
            gtEvaluator.apply(cmp, distance, precisionValue);
        } while (cmp.getBoolean());
        numIterations[0] = iterations;
    }

    private void mdpUnboundedCumulativeJacobiJava(
            Info info, GraphExplicitSparseAlternate graph, boolean min,
            ValueArrayAlgebra values, IterationStopCriterion stopCriterion,
            double tolerance, ValueArrayAlgebra cumul,
            int[] numIterationsResult) {
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
        ValueReal distance = TypeReal.get().newValue();
        Value optInitValue = min ? UtilValue.newValue(typeWeight, UtilValue.POS_INF) : UtilValue.newValue(typeWeight, UtilValue.NEG_INF);
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        OperatorEvaluator minEv = ContextValue.get().getEvaluator(OperatorMin.MIN, nextStateProb.getType(), choiceNextStateProb.getType());
        OperatorEvaluator maxEv = ContextValue.get().getEvaluator(OperatorMax.MAX, nextStateProb.getType(), choiceNextStateProb.getType());
        ValueReal precisionValue = TypeReal.get().newValue();
        ValueSetString.as(precisionValue).set(Double.toString(tolerance / 2));
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        int iterations = 0;
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setReal = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        OperatorEvaluator setArray = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get().getTypeArray(), TypeWeight.get().getTypeArray());
        ValueReal realZero = UtilValue.newValue(TypeReal.get(), 0);
        do {
            setReal.apply(distance, realZero);
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
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = nextValues;
            nextValues = presValues;
            presValues = swap;
            info.setNumIterations(iterations);
            info.setDifference(distance);
            iterations++;
            gtEvaluator.apply(cmp, distance, precisionValue);
        } while (cmp.getBoolean());
        numIterationsResult[0] = iterations;
        setArray.apply(values, presValues);
    }

    private void mdpUnboundedCumulativeGaussseidelJava(
            Info info, GraphExplicitSparseAlternate graph, boolean min, ValueArrayAlgebra values,
            IterationStopCriterion stopCriterion, double tolerance, ValueArrayAlgebra cumul,
            int[] numIterationsResult) {
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
        ValueReal distance = TypeReal.get().newValue();
        Value optInitValue = min ? UtilValue.newValue(typeWeight, UtilValue.POS_INF) : UtilValue.newValue(typeWeight, UtilValue.NEG_INF);
        OperatorEvaluator minEv = ContextValue.get().getEvaluator(OperatorMin.MIN, nextStateProb.getType(), choiceNextStateProb.getType());
        OperatorEvaluator maxEv = ContextValue.get().getEvaluator(OperatorMax.MAX, nextStateProb.getType(), choiceNextStateProb.getType());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        int iterations = 0;
        ValueReal precisionValue = TypeReal.get().newValue();
        ValueSetString.as(precisionValue).set(Double.toString(tolerance / 2));
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setReal = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        ValueAlgebra realZero = UtilValue.newValue(TypeReal.get(), 0);
        do {
            setReal.apply(distance, realZero);
            for (int state = 0; state < numStates; state++) {
                values.get(presStateProb, state);
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
                        values.get(succStateProb, succState);
                        multiply.apply(weighted, weight, succStateProb);
                        add.apply(choiceNextStateProb, choiceNextStateProb, weighted);
                    }
                    if (min) {
                        minEv.apply(nextStateProb, nextStateProb, choiceNextStateProb);
                    } else {
                        maxEv.apply(nextStateProb, nextStateProb, choiceNextStateProb);
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                values.set(nextStateProb, state);
            }
            info.setNumIterations(iterations);
            info.setDifference(distance);
            iterations++;
            gtEvaluator.apply(cmp, distance, precisionValue);
        } while (cmp.getBoolean());
        numIterationsResult[0] = iterations;
    }

    private static ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
