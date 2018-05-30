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
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorDistance;
import epmc.operator.OperatorDivide;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorIsZero;
import epmc.operator.OperatorMax;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.options.Options;
import epmc.util.StopWatch;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeReal;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.ValueReal;
import epmc.value.ValueSetString;

public final class GraphSolverIterativeMultiObjectiveScheduledJava implements GraphSolverExplicit {
    public static final String IDENTIFIER = "graph-solver-iterative-multiobjective-scheduled-java";

    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
    private SchedulerSimpleMultiobjectiveJava scheduler;
    private GraphSolverObjectiveExplicit objective;
    private GraphBuilderExplicit builder;

    private final OperatorEvaluator distanceEvaluator;
    private final OperatorEvaluator maxEvaluator;
    private final OperatorEvaluator divideEvaluator;
    private final OperatorEvaluator gtEvaluator;
    private final OperatorEvaluator isZeroEvaluator;
    private final ValueReal thisDistance;
    private final ValueReal zeroDistance;
    private final ValueBoolean cmp;

    public GraphSolverIterativeMultiObjectiveScheduledJava() {
        distanceEvaluator = ContextValue.get().getEvaluator(OperatorDistance.DISTANCE, TypeWeight.get(), TypeWeight.get());
        maxEvaluator = ContextValue.get().getEvaluator(OperatorMax.MAX, TypeReal.get(), TypeReal.get());
        divideEvaluator = ContextValue.get().getEvaluator(OperatorDivide.DIVIDE, TypeReal.get(), TypeReal.get());
        gtEvaluator = ContextValue.get().getEvaluator(OperatorGt.GT, TypeReal.get(), TypeReal.get());
        thisDistance = TypeReal.get().newValue();
        zeroDistance = TypeReal.get().newValue();
        isZeroEvaluator = ContextValue.get().getEvaluator(OperatorIsZero.IS_ZERO, TypeReal.get());
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
        if (!(objective instanceof GraphSolverObjectiveExplicitMultiObjectiveScheduled)) {
            return false;
        }
        Semantics semantics = origGraph.getGraphPropertyObject(CommonProperties.SEMANTICS);
        if (!SemanticsMDP.isMDP(semantics)) {
            return false;
        }
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objMulti = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        if (!(objMulti.getScheduler() instanceof SchedulerSimpleMultiobjectiveJava)) {
            return false;
        }
        return true;
    }

    @Override
    public void solve() {
        prepareIterGraph();
        multiobjectiveScheduled();
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
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveMultiObjectiveScheduled = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        inputValues = objectiveMultiObjectiveScheduled.getValues();
    }

    private void prepareResultValues() {
        this.outputValues = inputValues;
        objective.setResult(outputValues);
    }

    private void multiobjectiveScheduled() {
        Options options = Options.get();
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        Log log = options.get(OptionsMessages.LOG);
        StopWatch timer = new StopWatch(true);
        log.send(MessagesGraphSolverIterative.ITERATING);
        double tolerance = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveMultiObjectiveScheduled = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        scheduler = (SchedulerSimpleMultiobjectiveJava) objectiveMultiObjectiveScheduled.getScheduler();
        ValueArrayAlgebra stopStateRewards = objectiveMultiObjectiveScheduled.getStopStateRewards();
        ValueArrayAlgebra cumulativeTransitionRewards = objectiveMultiObjectiveScheduled.getTransitionRewards();
        inputValues = objectiveMultiObjectiveScheduled.getValues();
        int[] numIterations = new int[1];
        if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpMultiobjectivescheduledJacobiJava(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler, numIterations);
        } else if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpMultiobjectivescheduledGaussseidelJava(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler, numIterations);
        } else {
            assert false;
        }
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

    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }

    /* implementation of iteration algorithms */    

    private void mdpMultiobjectivescheduledJacobiJava(
            GraphExplicitSparseAlternate graph, ValueArrayAlgebra stopRewards,
            ValueArrayAlgebra transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            ValueArrayAlgebra values, SchedulerSimpleMultiobjectiveJava scheduler,
            int[] numIterationsResult) {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = scheduler.getDecisions();
        ValueArrayAlgebra weights = ValueArrayAlgebra.as(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra stopReward = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueReal distance = TypeReal.get().newValue();
        Value zero = UtilValue.newValue(values.getType().getEntryType(), 0);
        Value optInitValue = UtilValue.newValue(typeWeight, UtilValue.NEG_INF);
        int valuesTotalSize = values.size();
        for (int index = 0; index < valuesTotalSize; index++) {
            values.set(zero, index);
        }
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        ValueAlgebra transReward = newValueWeight();
        int iterations = 0;
        ValueReal precisionValue = TypeReal.get().newValue();
        ValueSetString.as(precisionValue).set(Double.toString(tolerance / 2));
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setReal = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        OperatorEvaluator setWeight = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setArray = ContextValue.get().getEvaluator(OperatorSet.SET, values.getType(), values.getType());
        ValueAlgebra zeroReal = UtilValue.newValue(TypeReal.get(), 0);
        do {
            setReal.apply(distance, zeroReal);
            for (int state = 0; state < numStates; state++) {
                stopRewards.get(stopReward, state);
                presValues.get(presStateProb, state);
                setWeight.apply(nextStateProb, optInitValue);
                int nondetNr = schedulerJava[state];
                if (nondetNr == -1) {
                    setWeight.apply(nextStateProb, stopReward);
                } else {
                    transRewards.get(transReward, nondetNr);
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    setWeight.apply(nextStateProb, transReward);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        multiply.apply(weighted, weight, succStateProb);
                        add.apply(nextStateProb, nextStateProb, weighted);
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = nextValues;
            nextValues = presValues;
            presValues = swap;
            iterations++;
            gtEvaluator.apply(cmp, distance, precisionValue);
        } while (cmp.getBoolean());
        numIterationsResult[0] = iterations;
        setArray.apply(values, presValues);
    }

    private void mdpMultiobjectivescheduledGaussseidelJava(
            GraphExplicitSparseAlternate graph, ValueArrayAlgebra stopRewards,
            ValueArrayAlgebra transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            ValueArrayAlgebra values, SchedulerSimpleMultiobjectiveJava scheduler,
            int[] numIterationsResult) {
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = scheduler.getDecisions();
        ValueArrayAlgebra weights = ValueArrayAlgebra.as(graph.getEdgePropertySparseNondet(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra stopReward = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        ValueReal distance = TypeReal.get().newValue();
        Value zero = UtilValue.newValue(values.getType().getEntryType(), 0);
        Value optInitValue = UtilValue.newValue(typeWeight, UtilValue.NEG_INF);
        int valuesTotalSize = values.size();
        for (int index = 0; index < valuesTotalSize; index++) {
            values.set(zero, index);
        }
        Value transReward = newValueWeight();
        int iterations = 0;
        ValueReal precisionValue = TypeReal.get().newValue();
        ValueSetString.as(precisionValue).set(Double.toString(tolerance / 2));
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator setReal = ContextValue.get().getEvaluator(OperatorSet.SET, TypeReal.get(), TypeReal.get());
        OperatorEvaluator setWeight = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        ValueAlgebra zeroReal = UtilValue.newValue(TypeReal.get(), 0);
        do {
            setReal.apply(distance, zeroReal);
            for (int state = 0; state < numStates; state++) {
                stopRewards.get(stopReward, state);
                values.get(presStateProb, state);
                setWeight.apply(nextStateProb, optInitValue);
                int nondetNr = schedulerJava[state];
                if (nondetNr == -1) {
                    setWeight.apply(nextStateProb, stopReward);
                } else {
                    transRewards.get(transReward, nondetNr);
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    setWeight.apply(nextStateProb, transReward);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        values.get(succStateProb, succState);
                        multiply.apply(weighted, weight, succStateProb);
                        add.apply(nextStateProb, nextStateProb, weighted);
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                values.set(nextStateProb, state);
            }
            iterations++;
            gtEvaluator.apply(cmp, distance, precisionValue);
        } while (cmp.getBoolean());
        numIterationsResult[0] = iterations;
    }

    private ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
