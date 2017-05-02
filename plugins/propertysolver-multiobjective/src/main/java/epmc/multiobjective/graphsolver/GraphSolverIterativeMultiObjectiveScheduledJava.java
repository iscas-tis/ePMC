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

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.GraphBuilderExplicit;
import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graphsolver.GraphSolverExplicit;
import epmc.graphsolver.iterative.IterationMethod;
import epmc.graphsolver.iterative.IterationStopCriterion;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicit;
import epmc.options.Options;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeDouble;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArrayAlgebra;

public final class GraphSolverIterativeMultiObjectiveScheduledJava implements GraphSolverExplicit {
    public static String IDENTIFIER = "graph-solver-iterative-multiobjective-scheduled-java";

    private GraphExplicit origGraph;
    private GraphExplicit iterGraph;
    private ValueArrayAlgebra inputValues;
    private ValueArrayAlgebra outputValues;
    private SchedulerSimpleMultiobjectiveJava scheduler;
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
    	if (!(objective instanceof GraphSolverObjectiveExplicitMultiObjectiveScheduled)) {
            return false;
        }
    	Semantics semantics = origGraph.getGraphPropertyObject(CommonProperties.SEMANTICS);
    	if (!SemanticsMDP.isMDP(semantics)) {
    		return false;
    	}
        Options options = origGraph.getOptions();
        Type typeWeight = TypeWeight.get();
        if (options.getBoolean(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)
                && TypeDouble.isDouble(typeWeight)) {
        	return false;
        }
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objMulti = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        if (!(objMulti.getScheduler() instanceof SchedulerSimpleMultiobjectiveJava)) {
        	return false;
        }
        return true;
    }

    @Override
    public void solve() throws EPMCException {
    	prepareIterGraph();
    	multiobjectiveScheduled();
        prepareResultValues();
    }

    private void prepareIterGraph() throws EPMCException {
        assert origGraph != null;
        this.builder = new GraphBuilderExplicit();
        builder.setInputGraph(origGraph);
        builder.addDerivedGraphProperties(origGraph.getGraphProperties());
        builder.addDerivedNodeProperties(origGraph.getNodeProperties());
        builder.addDerivedEdgeProperties(origGraph.getEdgeProperties());
        builder.setForNative(false);
        builder.setReorder();
        builder.build();
        this.iterGraph = builder.getOutputGraph();
        assert iterGraph != null;
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveMultiObjectiveScheduled = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        inputValues = objectiveMultiObjectiveScheduled.getValues();
    }

    private void prepareResultValues() throws EPMCException {
    	this.outputValues = inputValues;
    	objective.setResult(outputValues);
    }

    private void multiobjectiveScheduled() throws EPMCException {
        Options options = iterGraph.getOptions();
        IterationMethod iterMethod = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_METHOD);
        IterationStopCriterion stopCriterion = options.getEnum(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_STOP_CRITERION);
        double tolerance = options.getDouble(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_TOLERANCE);
        GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveMultiObjectiveScheduled = (GraphSolverObjectiveExplicitMultiObjectiveScheduled) objective;
        scheduler = (SchedulerSimpleMultiobjectiveJava) objectiveMultiObjectiveScheduled.getScheduler();
        ValueArrayAlgebra stopStateRewards = objectiveMultiObjectiveScheduled.getStopStateRewards();
        ValueArrayAlgebra cumulativeTransitionRewards = objectiveMultiObjectiveScheduled.getTransitionRewards();
        inputValues = objectiveMultiObjectiveScheduled.getValues();
        if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.JACOBI) {
            mdpMultiobjectivescheduledJacobiJava(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else if (isSparseMDPJava(iterGraph) && iterMethod == IterationMethod.GAUSS_SEIDEL) {
            mdpMultiobjectivescheduledGaussseidelJava(asSparseNondet(iterGraph), stopStateRewards, cumulativeTransitionRewards, stopCriterion, tolerance, inputValues, scheduler);
        } else {
            assert false;
        }
    }

    /* auxiliary methods */
    
    private static void compDiff(double[] distance, ValueAlgebra previous,
            Value current, IterationStopCriterion stopCriterion) throws EPMCException {
        if (stopCriterion == null) {
            return;
        }
        double thisDistance = previous.distance(current);
        if (stopCriterion == IterationStopCriterion.RELATIVE) {
            double presNorm = previous.norm();
            if (presNorm != 0.0) {
                thisDistance /= presNorm;
            }
        }
        distance[0] = Math.max(distance[0], thisDistance);
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
        GraphExplicitSparseAlternate sparseNondet = asSparseNondet(graph);
        return !sparseNondet.isNative();
    }
    
    private static GraphExplicitSparseAlternate asSparseNondet(GraphExplicit graph) {
        return (GraphExplicitSparseAlternate) graph;
    }
    
    /* implementation of iteration algorithms */    
    
    private void mdpMultiobjectivescheduledJacobiJava(
            GraphExplicitSparseAlternate graph, ValueArrayAlgebra stopRewards,
            ValueArrayAlgebra transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            ValueArrayAlgebra values, SchedulerSimpleMultiobjectiveJava scheduler) throws EPMCException {
        ContextValue contextValue = ContextValue.get();
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = scheduler.getDecisions();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra stopReward = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        Value zero = values.getType().getEntryType().getZero();
        Value optInitValue = typeWeight.getNegInf();
        int valuesTotalSize = values.size();
        for (int index = 0; index < valuesTotalSize; index++) {
            values.set(zero, index);
        }
        ValueArrayAlgebra presValues = values;
        ValueArrayAlgebra nextValues = UtilValue.newArray(values.getType(), numStates);
        ValueAlgebra transReward = newValueWeight();
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                stopRewards.get(stopReward, state);
                presValues.get(presStateProb, state);
                nextStateProb.set(optInitValue);
                int nondetNr = schedulerJava[state];
                if (nondetNr == -1) {
                    nextStateProb.set(stopReward);
                } else {
                    transRewards.get(transReward, nondetNr);
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    nextStateProb.set(transReward);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        presValues.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        nextStateProb.add(nextStateProb, weighted);
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                nextValues.set(nextStateProb, state);
            }
            ValueArrayAlgebra swap = nextValues;
            nextValues = presValues;
            presValues = swap;
        } while (distance[0] > tolerance / 2);
        values.set(presValues);
    }
    
    private void mdpMultiobjectivescheduledGaussseidelJava(
            GraphExplicitSparseAlternate graph, ValueArrayAlgebra stopRewards,
            ValueArrayAlgebra transRewards,
            IterationStopCriterion stopCriterion, double tolerance,
            ValueArrayAlgebra values, SchedulerSimpleMultiobjectiveJava scheduler) throws EPMCException {
        ContextValue contextValue = ContextValue.get();
        TypeWeight typeWeight = TypeWeight.get();
        int numStates = graph.computeNumStates();
        int[] nondetBounds = graph.getNondetBoundsJava();
        int[] targets = graph.getTargetsJava();
        int[] schedulerJava = scheduler.getDecisions();
        ValueArrayAlgebra weights = ValueArrayAlgebra.asArrayAlgebra(graph.getEdgeProperty(CommonProperties.WEIGHT).asSparseNondetOnlyNondet().getContent());
        ValueAlgebra stopReward = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        ValueAlgebra weighted = newValueWeight();
        ValueAlgebra succStateProb = newValueWeight();
        ValueAlgebra nextStateProb = newValueWeight();
        ValueAlgebra presStateProb = newValueWeight();
        double[] distance = new double[1];
        Value zero = values.getType().getEntryType().getZero();
        Value optInitValue = typeWeight.getNegInf();
        int valuesTotalSize = values.size();
        for (int index = 0; index < valuesTotalSize; index++) {
            values.set(zero, index);
        }
        Value transReward = newValueWeight();
        do {
            distance[0] = 0.0;
            for (int state = 0; state < numStates; state++) {
                stopRewards.get(stopReward, state);
                values.get(presStateProb, state);
                nextStateProb.set(optInitValue);
                int nondetNr = schedulerJava[state];
                if (nondetNr == -1) {
                    nextStateProb.set(stopReward);
                } else {
                    transRewards.get(transReward, nondetNr);
                    int nondetFrom = nondetBounds[nondetNr];
                    int nondetTo = nondetBounds[nondetNr + 1];
                    nextStateProb.set(transReward);
                    for (int stateSucc = nondetFrom; stateSucc < nondetTo; stateSucc++) {
                        weights.get(weight, stateSucc);
                        int succState = targets[stateSucc];
                        values.get(succStateProb, succState);
                        weighted.multiply(weight, succStateProb);
                        nextStateProb.add(nextStateProb, weighted);
                    }
                }
                compDiff(distance, presStateProb, nextStateProb, stopCriterion);
                values.set(nextStateProb, state);
            }
        } while (distance[0] > tolerance / 2);
    }
    
    private ValueAlgebra newValueWeight() {
    	return TypeWeight.get().newValue();
    }
}
