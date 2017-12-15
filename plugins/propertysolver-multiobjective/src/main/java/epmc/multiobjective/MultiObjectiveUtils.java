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

import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionMultiObjective;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.ExpressionSteadyState;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.Scheduler;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.EdgePropertyApply;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.NodePropertyApply;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.modelchecker.ModelChecker;
import epmc.multiobjective.graphsolver.GraphSolverObjectiveExplicitMultiObjectiveScheduled;
import epmc.multiobjective.graphsolver.GraphSolverObjectiveExplicitMultiObjectiveWeighted;
import epmc.operator.OperatorAdd;
import epmc.operator.OperatorAddInverse;
import epmc.operator.OperatorEq;
import epmc.operator.OperatorGt;
import epmc.operator.OperatorLt;
import epmc.operator.OperatorMultiply;
import epmc.operator.OperatorSet;
import epmc.propertysolver.PropertySolverExplicitReward;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeBoolean;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueBoolean;

final class MultiObjectiveUtils {
    static int compareProductDistance(ValueArray weights, ValueArray q,
            ValueArray bounds) {
        assert weights != null;
        assert q != null;
        assert bounds != null;
        int size = weights.size();
        Type typeWeight = TypeWeight.get();
        assert weights.size() == size;
        assert q.size() == size;
        assert bounds.size() == size;
        assert weights.getType().getEntryType() == typeWeight;
        assert q.getType().getEntryType() == typeWeight;
        assert bounds.getType().getEntryType() == typeWeight;
        ValueAlgebra weightsXqSum = newValueWeight();
        ValueAlgebra weightsXboundsSum = newValueWeight();
        ValueAlgebra weightsXqEntry = newValueWeight();
        ValueAlgebra weightsXboundsEntry = newValueWeight();
        ValueAlgebra weightsEntry = newValueWeight();
        ValueAlgebra qEntry = newValueWeight();
        ValueAlgebra boundsEntry = newValueWeight();
        OperatorEvaluator eq = ContextValue.get().getEvaluator(OperatorEq.EQ, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator lt = ContextValue.get().getEvaluator(OperatorLt.LT, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        for (int dim = 0; dim < weights.size(); dim++) {
            weights.get(weightsEntry, dim);
            q.get(qEntry, dim);
            bounds.get(boundsEntry, dim);
            multiply.apply(weightsXqEntry, weightsEntry, qEntry);
            add.apply(weightsXqSum, weightsXqSum, weightsXqEntry);
            multiply.apply(weightsXboundsEntry, weightsEntry, boundsEntry);
            add.apply(weightsXboundsSum, weightsXboundsSum, weightsXboundsEntry);
        }
        eq.apply(cmp, weightsXqSum, weightsXboundsSum);
        if (cmp.getBoolean()) {
            return 0;
        }
        lt.apply(cmp, weightsXqSum, weightsXboundsSum);
        if (cmp.getBoolean()) {
            return -1;
        }
        gt.apply(cmp, weightsXqSum, weightsXboundsSum);
        if (cmp.getBoolean()) {
            return 1;
        }
        assert false;
        return Integer.MIN_VALUE;
    }

    static ValueArrayAlgebra computeQuantifierBoundsArray(ModelChecker modelChecker,
            ExpressionMultiObjective property, boolean invert) {
        assert property != null;
        ValueAlgebra numMinValue = null;
        if (isNumericalQuery(property)) {
            ExpressionQuantifier propOp1 = (ExpressionQuantifier) property.getOperand1();
            Expression quantified = propOp1.getQuantified();
            if (!(quantified instanceof ExpressionReward)) {
                Expression minQ = new ExpressionQuantifier.Builder()
                        .setDirType(DirType.MIN)
                        .setCmpType(CmpType.IS)
                        .setQuantified(quantified)
                        .build();
                GraphExplicit mcGraph = modelChecker.getLowLevel();
                numMinValue = (ValueAlgebra) modelChecker.check(minQ, mcGraph.newInitialStateSet()).getSomeValue();
            } else if (modelChecker instanceof ModelChecker) {
                ModelChecker modelCheckerExplicit = modelChecker;
                // TODO should be replaced by GraphSolver
                PropertySolverExplicitReward solver =
                        (PropertySolverExplicitReward) modelCheckerExplicit.getSolverFor(property.getOperand1(), modelChecker.getLowLevel().newInitialStateSet());
                GraphExplicit mcGraph = modelCheckerExplicit.getLowLevel();
                NodeProperty nodeProp = mcGraph.getNodeProperty(((ExpressionReward) quantified).getReward());
                EdgeProperty edgeProp = mcGraph.getEdgeProperty(((ExpressionReward) quantified).getReward());
                if (invert) {
                    nodeProp = new NodePropertyApply(mcGraph, OperatorAddInverse.ADD_INVERSE, nodeProp);
                    edgeProp = new EdgePropertyApply(mcGraph, OperatorAddInverse.ADD_INVERSE, edgeProp);
                }
                numMinValue = newValueWeight();
                //                solver.solve(quantified, (StateSetExplicit) modelChecker.getLowLevel().newInitialStateSet(), true, nodeProp, edgeProp).getExplicitIthValue(numMinValue, 0);
                //                System.out.println(numMinValue);
                // TODO hack
                //                numMinValue.set(TypeAlgebra.asAlgebra(numMinValue.getType()).getZero());
                numMinValue.set(-1000);
            } else {
                assert false;
            }
        }
        ValueArrayAlgebra bounds = newValueArrayWeight(property.getOperands().size());
        int objNr = 0;
        for (Expression objective : property.getOperands()) {
            ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            if (!isIs(objective)) {
                bounds.set(UtilEvaluatorExplicit.evaluate(objectiveQuantifier.getCompare()), objNr);
            }
            objNr++;
        }
        if (numMinValue != null) {
            bounds.set(numMinValue, 0);
        }
        return bounds;
    }

    static IterationResult iterate(ValueArrayAlgebra weights,
            GraphExplicit graph,
            IterationRewards rewards) {
        assert weights != null;
        assert graph != null;
        assert rewards != null;
        int numAutomata = rewards.getNumObjectives();
        ValueArrayAlgebra q = newValueArrayWeight(numAutomata);
        int iterInit = graph.getInitialNodes().nextSetBit(0);
        int[] choice = new int[graph.computeNumStates()];
        ValueArrayAlgebra weightedCombinations = combinationsToWeighted(rewards, choice, weights);
        ValueArrayAlgebra weightedRewards = rewardsToWeighted(rewards, weights);
        ValueArrayAlgebra iterResult = UtilValue.newArray(TypeWeight.get().getTypeArray(), graph.computeNumStates());

        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        GraphSolverObjectiveExplicitMultiObjectiveWeighted objective = new GraphSolverObjectiveExplicitMultiObjectiveWeighted();
        objective.setValues(iterResult);
        objective.setGraph(graph);
        objective.setStopStateReward(weightedCombinations);
        objective.setTransitionRewards(weightedRewards);
        configuration.setObjective(objective);
        configuration.solve();
        Scheduler scheduler = objective.getScheduler();
        iterResult = objective.getResult();
        Value initValue = newValueWeight();
        ValueArrayAlgebra propWeights = newValueArrayWeight(numAutomata);
        for (int prop = 0; prop < numAutomata; prop++) {
            int propWeightsTotalSize = propWeights.size();
            for (int index = 0; index < propWeightsTotalSize; index++) {
                propWeights.set(0, index);
            }
            for (int i = 0; i < iterResult.size(); i++) {
                iterResult.set(0, i);
            }
            propWeights.set(1, prop);
            weightedCombinations = chosenCombinationsToWeighted(rewards, choice, propWeights);
            weightedRewards = rewardsToWeighted(rewards, propWeights);
            GraphSolverObjectiveExplicitMultiObjectiveScheduled objectiveSched = new GraphSolverObjectiveExplicitMultiObjectiveScheduled();
            objectiveSched.setGraph(graph);
            objectiveSched.setValues(iterResult);
            objectiveSched.setScheduler(scheduler);
            objectiveSched.setStopStateRewards(weightedCombinations);
            objectiveSched.setTransitionRewards(weightedRewards);
            configuration.setObjective(objectiveSched);
            configuration.solve();
            iterResult.get(initValue, iterInit);
            q.set(initValue, prop);
        }
        return new IterationResult(q, scheduler);
    }

    static boolean isNumericalQuery(ExpressionMultiObjective property) {
        assert property != null;
        int numQuantitative = 0;
        for (Expression operand : property.getOperands()) {
            if (isIs(operand)) {
                numQuantitative++;
            }
        }
        return numQuantitative == 1;
    }
    

    public static boolean isSteadyState(ExpressionMultiObjective property) {
        boolean foundSteadyState = false;
        boolean allSteadyState = true;
        for (Expression operand : property.getOperands()) {
            assert ExpressionQuantifier.is(operand);
            ExpressionQuantifier quantifier = ExpressionQuantifier.as(operand);
            if (ExpressionSteadyState.is(quantifier.getQuantified())) {
                foundSteadyState = true;
            } else {
                allSteadyState = false;
            }
        }
        return foundSteadyState && allSteadyState;
    }

    private static ValueArrayAlgebra rewardsToWeighted(
            IterationRewards rewards, ValueArrayAlgebra weights) {
        assert rewards != null;
        assert weights != null;
        assert weights.size() == rewards.getNumObjectives();
        int numNondet = rewards.getNumNondet();
        int numObjectives = rewards.getNumObjectives();
        ValueArrayAlgebra result = UtilValue.newArray(TypeWeight.get().getTypeArray(), numNondet);
        ValueAlgebra entry = newValueWeight();
        ValueAlgebra objWeight = newValueWeight();
        ValueAlgebra objRew = newValueWeight();
        ValueAlgebra prod = newValueWeight();
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator multiply = ContextValue.get().getEvaluator(OperatorMultiply.MULTIPLY, TypeWeight.get(), TypeWeight.get());
        for (int obj = 0; obj < numObjectives; obj++) {
            weights.get(objWeight, obj);
            for (int nondet = 0; nondet < numNondet; nondet++) {
                rewards.getRewards(obj).get(objRew, nondet);
                multiply.apply(prod, objWeight, objRew);
                result.get(entry, nondet);
                add.apply(entry, entry, prod);
                result.set(entry, nondet);
            }
        }

        return result;
    }

    private static ValueArrayAlgebra combinationsToWeighted(IterationRewards combinations,
            int[] choice, ValueArrayAlgebra weights) {
        assert combinations != null;
        assert weights != null;
        assert weights.size() == combinations.getNumObjectives();
        int numStates = combinations.getNumStates();
        int numObjectives = combinations.getNumObjectives();

        ValueArrayAlgebra result = UtilValue.newArray(TypeWeight.get().getTypeArray(), numStates);
        ValueAlgebra max = newValueWeight();
        ValueAlgebra entryValue = newValueWeight();
        ValueAlgebra weight = newValueWeight();
        OperatorEvaluator gt = ContextValue.get().getEvaluator(OperatorGt.GT, TypeWeight.get(), TypeWeight.get());
        ValueBoolean cmp = TypeBoolean.get().newValue();
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        OperatorEvaluator set = ContextValue.get().getEvaluator(OperatorSet.SET, TypeWeight.get(), TypeWeight.get());
        for (int state = 0; state < numStates; state++) {
            max.set(-10000);
            int numEntries = combinations.getNumEntries(state);
            for (int entry = 0; entry < numEntries; entry++) {
                entryValue.set(-10000);
                boolean alreadySet = false;
                for (int objective = 0; objective < numObjectives; objective++) {
                    if (combinations.get(state, entry, objective)) {
                        weights.get(weight, objective);
                        if (alreadySet) {
                            add.apply(entryValue, entryValue, weight);
                        } else {
                            set.apply(entryValue, weight);
                            alreadySet = true;
                        }
                    }
                }
                gt.apply(cmp, entryValue, max);
                if (cmp.getBoolean()) {
                    set.apply(max, entryValue);
                    choice[state] = entry;
                }
            }
            result.set(max, state);
        }
        return result;
    }

    private static ValueArrayAlgebra chosenCombinationsToWeighted(IterationRewards combinations,
            int[] choice, ValueArrayAlgebra weights) {
        assert combinations != null;
        assert weights != null;
        assert weights.size() == combinations.getNumObjectives();
        int numStates = combinations.getNumStates();
        int numObjectives = combinations.getNumObjectives();
        ValueArrayAlgebra result = UtilValue.newArray(TypeWeight.get().getTypeArray(), numStates);
        ValueAlgebra entryValue = newValueWeight();
        Value weight = newValueWeight();
        OperatorEvaluator add = ContextValue.get().getEvaluator(OperatorAdd.ADD, TypeWeight.get(), TypeWeight.get());
        for (int state = 0; state < numStates; state++) {
            int entry = choice[state];
            entryValue.set(0);
            for (int objective = 0; objective < numObjectives; objective++) {
                if (combinations.get(state, entry, objective)) {
                    weights.get(weight, objective);
                    add.apply(entryValue, entryValue, weight);
                }
            }   
            result.set(entryValue, state);
        }
        return result;
    }

    private static ValueArrayAlgebra newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }

    private static ValueAlgebra newValueWeight() {
        return TypeWeight.get().newValue();
    }

    private static boolean isIs(Expression expression) {
        assert expression != null;
        if (!ExpressionQuantifier.is(expression)) {
            return false;
        }
        ExpressionQuantifier expressionQuantifier = (ExpressionQuantifier) expression;
        return expressionQuantifier.getCompareType() == CmpType.IS;
    }

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private MultiObjectiveUtils() {
    }
}
