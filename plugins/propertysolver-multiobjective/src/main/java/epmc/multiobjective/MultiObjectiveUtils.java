package epmc.multiobjective;

import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.ExpressionMultiObjective;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.EdgePropertyApply;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.NodePropertyApply;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.iterative.OptionsGraphSolverIterative;
import epmc.modelchecker.ModelChecker;
import epmc.multiobjective.graphsolver.GraphSolverObjectiveExplicitMultiObjectiveScheduled;
import epmc.multiobjective.graphsolver.GraphSolverObjectiveExplicitMultiObjectiveWeighted;
import epmc.propertysolver.PropertySolverExplicitReward;
import epmc.value.ContextValue;
import epmc.value.OperatorAddInverse;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeHasNativeArray;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;

final class MultiObjectiveUtils {
    static int compareProductDistance(ValueArray weights, ValueArray q,
            ValueArray bounds) throws EPMCException {
        assert weights != null;
        assert q != null;
        assert bounds != null;
        ContextValue contextValue = weights.getType().getContext();
        int size = weights.size();
        Type typeWeight = TypeWeight.get(contextValue);
        assert weights.getType().getContext() == contextValue;
        assert q.getType().getContext() == contextValue;
        assert bounds.getType().getContext() == contextValue;
        assert weights.size() == size;
        assert q.size() == size;
        assert bounds.size() == size;
        assert weights.getType().getEntryType() == typeWeight;
        assert q.getType().getEntryType() == typeWeight;
        assert bounds.getType().getEntryType() == typeWeight;
        ValueAlgebra weightsXqSum = newValueWeight(contextValue);
        ValueAlgebra weightsXboundsSum = newValueWeight(contextValue);
        ValueAlgebra weightsXqEntry = newValueWeight(contextValue);
        ValueAlgebra weightsXboundsEntry = newValueWeight(contextValue);
        ValueAlgebra weightsEntry = newValueWeight(contextValue);
        ValueAlgebra qEntry = newValueWeight(contextValue);
        ValueAlgebra boundsEntry = newValueWeight(contextValue);
        for (int dim = 0; dim < weights.size(); dim++) {
            weights.get(weightsEntry, dim);
            q.get(qEntry, dim);
            bounds.get(boundsEntry, dim);
            weightsXqEntry.multiply(weightsEntry, qEntry);
            weightsXqSum.add(weightsXqSum, weightsXqEntry);
            weightsXboundsEntry.multiply(weightsEntry, boundsEntry);
            weightsXboundsSum.add(weightsXboundsSum, weightsXboundsEntry);
        }
        return weightsXqSum.compareTo(weightsXboundsSum);
    }

    static ValueArrayAlgebra computeQuantifierBoundsArray(ModelChecker modelChecker,
            ExpressionMultiObjective property, boolean invert)
                    throws EPMCException {
        assert property != null;
        ContextValue contextValue = modelChecker.getModel().getContextValue();
        Value numMinValue = null;
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
                numMinValue = modelChecker.check(minQ, mcGraph.newInitialStateSet()).getSomeValue();
            } else if (modelChecker instanceof ModelChecker) {
                ModelChecker modelCheckerExplicit = modelChecker;
                // TODO should be replaced by GraphSolver
                PropertySolverExplicitReward solver =
                        (PropertySolverExplicitReward) modelCheckerExplicit.getSolverFor(property.getOperand1(), modelChecker.getLowLevel().newInitialStateSet());
                GraphExplicit mcGraph = modelCheckerExplicit.getLowLevel();
                NodeProperty nodeProp = mcGraph.getNodeProperty(((ExpressionReward) quantified).getReward());
                EdgeProperty edgeProp = mcGraph.getEdgeProperty(((ExpressionReward) quantified).getReward());
                if (invert) {
                    nodeProp = new NodePropertyApply(mcGraph, contextValue.getOperator(OperatorAddInverse.IDENTIFIER), nodeProp);
                    edgeProp = new EdgePropertyApply(mcGraph, contextValue.getOperator(OperatorAddInverse.IDENTIFIER), edgeProp);
                }
                numMinValue = newValueWeight(contextValue);
                solver.solve(quantified, (StateSetExplicit) modelChecker.getLowLevel().newInitialStateSet(), true, nodeProp, edgeProp).getExplicitIthValue(numMinValue, 0);
            } else {
                assert false;
            }
        }
        ValueArrayAlgebra bounds = newValueArrayWeight(contextValue, property.getOperands().size());
        int objNr = 0;
        for (Expression objective : property.getOperands()) {
        	ExpressionQuantifier objectiveQuantifier = (ExpressionQuantifier) objective;
            if (!isIs(objective)) {
            	ExpressionLiteral compare = (ExpressionLiteral) objectiveQuantifier.getCompare();
                bounds.set(compare.getValue(), objNr);
            }
            objNr++;
        }
        if (numMinValue != null) {
            bounds.set(numMinValue, 0);
        }
        return bounds;
    }

    static void iterate(ValueArray q,
    		ValueArrayAlgebra weights,
            GraphExplicit graph,
            MultiObjectiveIterationRewards rewards)
                    throws EPMCException {
        assert q != null;
        assert weights != null;
        assert graph != null;
        assert rewards != null;
        ContextValue contextValue = q.getType().getContext();
        int numAutomata = rewards.getNumObjectives();
        int iterInit = graph.getInitialNodes().nextSetBit(0);
        int[] choice = new int[graph.computeNumStates()];
        ValueArrayAlgebra weightedCombinations = combinationsToWeighted(rewards, choice, weights);
        ValueArrayAlgebra weightedRewards = rewardsToWeighted(rewards, weights);
        Type typeWeight = TypeWeight.get(contextValue);
        boolean useNative = contextValue.getOptions().getBoolean(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)
                && TypeHasNativeArray.getTypeNativeArray(typeWeight) != null;
        ValueArrayAlgebra iterResult = useNative
                ? UtilValue.newArray(TypeHasNativeArray.getTypeNativeArray(TypeWeight.get(contextValue)), graph.computeNumStates())
                : UtilValue.newArray(TypeWeight.get(contextValue).getTypeArray(), graph.computeNumStates());

        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit(graph.getOptions());
        GraphSolverObjectiveExplicitMultiObjectiveWeighted objective = new GraphSolverObjectiveExplicitMultiObjectiveWeighted();
        objective.setValues(iterResult);
        objective.setGraph(graph);
        objective.setStopStateReward(weightedCombinations);
        objective.setTransitionRewards(weightedRewards);
        configuration.setObjective(objective);
        configuration.solve();
        Value scheduler = objective.getScheduler();
        iterResult = objective.getResult();
        Value initValue = newValueWeight(contextValue);
        ValueArrayAlgebra propWeights = newValueArrayWeight(contextValue, numAutomata);
        for (int prop = 0; prop < numAutomata; prop++) {
            int propWeightsTotalSize = propWeights.getTotalSize();
            for (int index = 0; index < propWeightsTotalSize; index++) {
            	propWeights.set(0, index);
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
            scheduler = objective.getScheduler();
            iterResult = objective.getResult();
            iterResult.get(initValue, iterInit);
            q.set(initValue, prop);
        }
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

	private static ValueArrayAlgebra rewardsToWeighted(
            MultiObjectiveIterationRewards rewards, ValueArrayAlgebra weights) throws EPMCException {
        assert rewards != null;
        assert weights != null;
        assert weights.size() == rewards.getNumObjectives();
        ContextValue contextValue = weights.getType().getContext();
        int numNondet = rewards.getNumNondet();
        int numObjectives = rewards.getNumObjectives();
        boolean useNative = contextValue.getOptions().getBoolean(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)
                && TypeHasNativeArray.getTypeNativeArray(TypeWeight.get(contextValue)) != null;
        ValueArrayAlgebra result = useNative
                ? UtilValue.newArray(TypeHasNativeArray.getTypeNativeArray(TypeWeight.get(contextValue)), numNondet)
                : UtilValue.newArray(TypeWeight.get(contextValue).getTypeArray(), numNondet);
        ValueAlgebra entry = newValueWeight(contextValue);
        ValueAlgebra objWeight = newValueWeight(contextValue);
        ValueAlgebra objRew = newValueWeight(contextValue);
        ValueAlgebra prod = newValueWeight(contextValue);
        for (int obj = 0; obj < numObjectives; obj++) {
            weights.get(objWeight, obj);
            for (int nondet = 0; nondet < numNondet; nondet++) {
                rewards.getRewards(obj).get(objRew, nondet);
                prod.multiply(objWeight, objRew);
                result.get(entry, nondet);
                entry.add(entry, prod);
                result.set(entry, nondet);
            }
        }
        
        return result;
    }

    private static ValueArrayAlgebra combinationsToWeighted(MultiObjectiveIterationRewards combinations,
            int[] choice, ValueArrayAlgebra weights) throws EPMCException {
        assert combinations != null;
        assert weights != null;
        assert weights.size() == combinations.getNumObjectives();
        ContextValue contextValue = weights.getType().getContext();
        int numStates = combinations.getNumStates();
        int numObjectives = combinations.getNumObjectives();
        
        
        boolean useNative = contextValue.getOptions().getBoolean(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)
                && TypeHasNativeArray.getTypeNativeArray(TypeWeight.get(contextValue)) != null;
        ValueArrayAlgebra result = useNative
                ? UtilValue.newArray(TypeHasNativeArray.getTypeNativeArray(TypeWeight.get(contextValue)), numStates)
                : UtilValue.newArray(TypeWeight.get(contextValue).getTypeArray(), numStates);
        ValueAlgebra max = newValueWeight(contextValue);
        ValueAlgebra entryValue = newValueWeight(contextValue);
        Value weight = newValueWeight(contextValue);
        for (int state = 0; state < numStates; state++) {
            max.set(0);
            int numEntries = combinations.getNumEntries(state);
            for (int entry = 0; entry < numEntries; entry++) {
                entryValue.set(0);
                for (int objective = 0; objective < numObjectives; objective++) {
                    if (combinations.get(state, entry, objective)) {
                        weights.get(weight, objective);
                        entryValue.add(entryValue, weight);
                    }
                }
                if (entryValue.isGt(max)) {
                    max.set(entryValue);
                    choice[state] = entry;
                }
            }
            result.set(max, state);
        }
        return result;
    }

    private static ValueArrayAlgebra chosenCombinationsToWeighted(MultiObjectiveIterationRewards combinations,
            int[] choice, ValueArrayAlgebra weights) throws EPMCException {
        assert combinations != null;
        assert weights != null;
        assert weights.size() == combinations.getNumObjectives();
        ContextValue contextValue = weights.getType().getContext();
        int numStates = combinations.getNumStates();
        int numObjectives = combinations.getNumObjectives();
        boolean useNative = contextValue.getOptions().getBoolean(OptionsGraphSolverIterative.GRAPHSOLVER_ITERATIVE_NATIVE)
                && TypeHasNativeArray.getTypeNativeArray(TypeWeight.get(contextValue)) != null;
        ValueArrayAlgebra result = useNative
                ? UtilValue.newArray(TypeHasNativeArray.getTypeNativeArray(TypeWeight.get(contextValue)), numStates)
                : UtilValue.newArray(TypeWeight.get(contextValue).getTypeArray(), numStates);
        ValueAlgebra entryValue = newValueWeight(contextValue);
        Value weight = newValueWeight(contextValue);
        for (int state = 0; state < numStates; state++) {
            int entry = choice[state];
            entryValue.set(0);
            for (int objective = 0; objective < numObjectives; objective++) {
                if (combinations.get(state, entry, objective)) {
                    weights.get(weight, objective);
                    entryValue.add(entryValue, weight);
                }
            }   
            result.set(entryValue, state);
        }
        return result;
    }

    private static ValueArrayAlgebra newValueArrayWeight(ContextValue contextValue, int size) {
        TypeArray typeArray = TypeWeight.get(contextValue).getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }
    
    private static ValueAlgebra newValueWeight(ContextValue contextValue) {
    	return TypeWeight.get(contextValue).newValue();
    }

    private static boolean isIs(Expression expression) {
        assert expression != null;
        if (!(expression instanceof ExpressionQuantifier)) {
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
