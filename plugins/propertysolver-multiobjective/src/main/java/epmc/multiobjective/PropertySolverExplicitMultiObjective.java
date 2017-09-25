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

package epmc.multiobjective;

import static epmc.error.UtilError.ensure;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionMultiObjective;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.graph.CommonProperties;
import epmc.graph.Scheduler;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparseAlternate;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.SchedulerSimple;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.modelchecker.options.OptionsModelChecker;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;
import epmc.value.operator.OperatorSubtract;

public final class PropertySolverExplicitMultiObjective implements PropertySolver {
    public final static String IDENTIFIER = "multiobjective-explicit";

    private ModelChecker modelChecker;
    private Expression property;
    private ExpressionMultiObjective propertyMultiObjective;
    private StateSet forStates;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
    }

    @Override
    public void setProperty(Expression property) {
        this.property = property;
        if (property instanceof ExpressionMultiObjective) {
            this.propertyMultiObjective = (ExpressionMultiObjective) property;
        }
    }

    @Override
    public void setForStates(StateSet forStates) {
        this.forStates = forStates;
    }

    @Override
    public boolean canHandle() {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        if (!(property instanceof ExpressionMultiObjective)) {
            return false;
        }
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression objective : propertyMultiObjective.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Set<Expression> inners = UtilLTL.collectLTLInner(objectiveQuantifier.getQuantified());
            for (Expression inner : inners) {
                // TODO
                //	            modelChecker.ensureCanHandle(inner, allStates);
            }
        }
        if (allStates != null) {
            allStates.close();
        }
        return true;
    }

    @Override
    public Set<Object> getRequiredGraphProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.SEMANTICS);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredNodeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.STATE);
        required.add(CommonProperties.PLAYER);
        for (Expression objective : propertyMultiObjective.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            if (quantified instanceof ExpressionReward) {
                required.add(((ExpressionReward) quantified).getReward());
            } else {
                for (Expression inner : UtilLTL.collectLTLInner(quantified)) {
                    required.addAll(modelChecker.getRequiredNodeProperties(inner, forStates));
                }
            }
        }
        //	    required.add(new ExpressionIdentifierStandard.Builder()
        //    		.setName("x").build());
        //   required.add(new ExpressionIdentifierStandard.Builder()
        //  		.setName("y").build());
        return Collections.unmodifiableSet(required);
    }

    @Override
    public Set<Object> getRequiredEdgeProperties() {
        Set<Object> required = new LinkedHashSet<>();
        required.add(CommonProperties.WEIGHT);
        for (Expression objective : propertyMultiObjective.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            if (quantified instanceof ExpressionReward) {
                required.add(((ExpressionReward) quantified).getReward());
            }
        }
        //	    required.add(CommonProperties.TRANSITION_LABEL);
        return Collections.unmodifiableSet(required);
    }

    @Override
    public StateMap solve() {
        assert property != null;
        assert forStates != null;
        StateSetExplicit initialStates = (StateSetExplicit) modelChecker.getLowLevel().newInitialStateSet();
        ensure(initialStates.size() == 1, ProblemsMultiObjective.MULTI_OBJECTIVE_INITIAL_NOT_SINGLETON);
        PropertyNormaliser normaliser = new PropertyNormaliser()
                .setOriginalProperty(propertyMultiObjective)
                .setExpressionToType(modelChecker.getLowLevel())
                .build();
        property = propertyMultiObjective = normaliser.getNormalisedProperty();
        Value subtractNumericalFrom = normaliser.getSubtractNumericalFrom();
        BitSet invertedRewards = normaliser.getInvertedRewards();
        prepareRequiredPropositionals();
        GraphExplicit graph = modelChecker.getLowLevel();
        Product product = new ProductBuilder()
                .setProperty(propertyMultiObjective)
                .setModelChecker(modelChecker)
                .setGraph(graph)
                .setInvertedRewards(invertedRewards)
                .build();
        return mainLoop(product, subtractNumericalFrom);
    }

    /**
     * Compute values of required propositional properties and register their values.
     * 
     */
    private void prepareRequiredPropositionals() {
        GraphExplicit graph = modelChecker.getLowLevel();
        StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        for (Expression objective : propertyMultiObjective.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            Expression quantified = objectiveQuantifier.getQuantified();
            if (quantified instanceof ExpressionReward) {
                continue;
            }
            Set<Expression> inners = UtilLTL.collectLTLInner(quantified);
            for (Expression inner : inners) {
                StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(inner, allStates);
                UtilGraph.registerResult(graph, inner, innerResult);
            }
        }
        allStates.close();
    }

    private StateMap mainLoop(Product product, Value subtractNumericalFrom) {
        assert product != null;
        GraphExplicit iterGraph = product.getGraph();
        IterationRewards combinations = product.getRewards();
        ValueArrayAlgebra bounds = MultiObjectiveUtils.computeQuantifierBoundsArray(modelChecker, propertyMultiObjective, !ValueAlgebra.asAlgebra(subtractNumericalFrom).isPosInf());
        int numAutomata = product.getNumAutomata();
        DownClosure down = new DownClosure(numAutomata);
        ValueArrayAlgebra weights;
        boolean feasible = false;
        boolean numerical = MultiObjectiveUtils.isNumericalQuery(propertyMultiObjective);
        do {
            weights = down.findSeparating(bounds, numerical);
            if (weights == null) {
                feasible = true;
                break;
            }
            IterationResult iterResult = MultiObjectiveUtils.iterate(weights, iterGraph, combinations);
            if (iterResult.getQ().isEq(bounds)) {
                feasible = true;
                break;
            }
            if (MultiObjectiveUtils.compareProductDistance(weights, iterResult.getQ(), bounds) < 0) {
                feasible = false;
                break;
            }
            down.add(iterResult);
            if (numerical) {
                down.improveNumerical(bounds);
                if (MultiObjectiveUtils.compareProductDistance(weights, iterResult.getQ(), bounds) <= 0) {
                    feasible = true;
                    break;
                }
            }
        } while (true);
        SchedulerInitialRandomisedImpl sched = null;
        if ((feasible || numerical) && Options.get().getBoolean(OptionsModelChecker.COMPUTE_SCHEDULER)) {
            sched = computeRandomizedScheduler(product, down, bounds);
            //      printInitiallyRandomisedScheduler(sched);
        }
        return prepareResult(numerical, feasible, bounds, subtractNumericalFrom, sched);
    }

    private SchedulerInitialRandomisedImpl computeRandomizedScheduler(Product product, DownClosure down, ValueArrayAlgebra bounds) {
        assert product != null;
        assert down != null;
        assert bounds != null;
        GraphExplicit modelGraph = modelChecker.getLowLevel();
        int numStates = modelGraph.computeNumStates();
        GraphExplicitSparseAlternate computationGraph = (GraphExplicitSparseAlternate) product.getGraph();
        NodeProperty nodeProp = computationGraph.getNodeProperty(CommonProperties.NODE_MODEL);
        assert nodeProp != null;
        NodeProperty stateProp = computationGraph.getNodeProperty(CommonProperties.STATE);
        assert stateProp != null;
        int numNodes = computationGraph.getNumNodes();

        ValueArrayAlgebra schedProbs = down.findFeasibleRandomisedScheduler(bounds);
        assert schedProbs != null;
        int numSchedulers = 0;
        for (int schedNr = 0; schedNr < down.size(); schedNr++) {
            ValueAlgebra schedProb = schedProbs.getType().getEntryType().newValue();
            schedProbs.get(schedProb, schedNr);
            if (schedProb.isZero()) {
                continue;
            }
            numSchedulers++;
        }
        ValueAlgebra[] probabilities = new ValueAlgebra[numSchedulers];
        Scheduler[] schedulers = new Scheduler[numSchedulers];

        // TODO check memorylessness
        int usedSchedNr = 0;
        for (int schedNr = 0; schedNr < down.size(); schedNr++) {
            ValueAlgebra schedProb = schedProbs.getType().getEntryType().newValue();
            schedProbs.get(schedProb, schedNr);
            if (schedProb.isZero()) {
                continue;
            }
            SchedulerSimple sched = (SchedulerSimple) down.get(schedNr).getScheduler();
            int[] decisions = new int[numStates];
            Arrays.fill(decisions, -2);
            for (int node = 0; node < numNodes; node++) {
                if (!stateProp.getBoolean(node)) {
                    continue;
                }
                int state = nodeProp.getInt(node);
                int decision = sched.getDecision(node);
                if (decision == -2) {
                    decision = 0;
                }
                assert decisions[state] == -2 || decisions[state] == decision
                        : state + " " + decisions[state] + " " + decision;
                decisions[state] = decision;
            }
            for (int state = 0; state < numStates; state++) {
                if (decisions[state] < 0) {
                    decisions[state] = 0;
                }
            }
            SchedulerSimpleArray convertedSched = new SchedulerSimpleArray(decisions);
            probabilities[usedSchedNr] = schedProb;
            schedulers[usedSchedNr] = convertedSched;
            usedSchedNr++;
        }
        return new SchedulerInitialRandomisedImpl(probabilities, schedulers);
    }

    private StateMap prepareResult(boolean numerical, boolean feasible, ValueArray bounds, Value subtractNumericalFrom, Scheduler scheduler)
    {
        ValueArray resultValues;
        if (numerical) {
            //            ensure(feasible, ProblemsMultiObjective.MULTI_OBJECTIVE_UNEXPECTED_INFEASIBLE);
            resultValues = newValueArrayWeight(forStates.size());
            ValueAlgebra entry = newValueWeight();
            bounds.get(entry, 0);
            OperatorEvaluator subtract = ContextValue.get().getOperatorEvaluator(OperatorSubtract.SUBTRACT, TypeWeight.get(),  TypeWeight.get());
            if (!ValueAlgebra.asAlgebra(subtractNumericalFrom).isPosInf()) {
                subtract.apply(entry, subtractNumericalFrom, entry);
            }
            for (int i = 0; i < forStates.size(); i++) {
                resultValues.set(entry, i);
            }
        } else {
            resultValues = UtilValue.newArray(TypeBoolean.get().getTypeArray(),
                    forStates.size());
            ValueBoolean valueFeasible = TypeBoolean.get().newValue();
            valueFeasible.set(feasible);
            for (int i = 0; i < forStates.size(); i++) {
                resultValues.set(valueFeasible, i);
            }
        }
        return new StateMapExplicit((StateSetExplicit) forStates.clone(), resultValues, scheduler);
    }

    private ValueArrayAlgebra newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }

    private ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }
}
