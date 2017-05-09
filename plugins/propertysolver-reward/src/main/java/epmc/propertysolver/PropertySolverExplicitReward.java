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

package epmc.propertysolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.algorithms.UtilAlgorithms;
import epmc.algorithms.explicit.ComponentsExplicit;
import epmc.error.EPMCException;
import epmc.expression.Expression;
import epmc.expression.evaluatorexplicit.EvaluatorExplicit;
import epmc.expression.standard.CmpType;
import epmc.expression.standard.DirType;
import epmc.expression.standard.ExpressionQuantifier;
import epmc.expression.standard.ExpressionReward;
import epmc.expression.standard.RewardSpecification;
import epmc.expression.standard.RewardType;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.Semantics;
import epmc.graph.SemanticsIMDP;
import epmc.graph.SemanticsMDP;
import epmc.graph.SemanticsMarkovChain;
import epmc.graph.StateMap;
import epmc.graph.StateSet;
import epmc.graph.UtilGraph;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.StateMapExplicit;
import epmc.graph.explicit.StateSetExplicit;
import epmc.graphsolver.GraphSolverConfigurationExplicit;
import epmc.graphsolver.UtilGraphSolver;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBounded;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedCumulative;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitBoundedCumulativeDiscounted;
import epmc.graphsolver.objective.GraphSolverObjectiveExplicitUnboundedCumulative;
import epmc.modelchecker.EngineExplicit;
import epmc.modelchecker.ModelChecker;
import epmc.modelchecker.PropertySolver;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.Operator;
import epmc.value.Type;
import epmc.value.TypeArray;
import epmc.value.TypeWeight;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.ValueReal;

// TODO check whether this works for JANI MDPs - probably not

public final class PropertySolverExplicitReward implements PropertySolver {
    public final static String IDENTIFIER = "reward-explicit";
    private ModelChecker modelChecker;
    private GraphExplicit graph;
	private Expression property;
	private StateSet forStates;

    @Override
    public void setModelChecker(ModelChecker modelChecker) {
        assert modelChecker != null;
        this.modelChecker = modelChecker;
        if (modelChecker.getEngine() instanceof EngineExplicit) {
        	this.graph = modelChecker.getLowLevel();
        }
    }


	@Override
	public void setProperty(Expression property) {
		this.property = property;
	}

	@Override
	public void setForStates(StateSet forStates) {
		this.forStates = forStates;
	}

	@Override
	public Set<Object> getRequiredGraphProperties() throws EPMCException {
		Set<Object> required = new LinkedHashSet<>();
		required.add(CommonProperties.SEMANTICS);
		return Collections.unmodifiableSet(required);
	}


	@Override
	public Set<Object> getRequiredNodeProperties() throws EPMCException {
		Set<Object> required = new LinkedHashSet<>();
		required.add(CommonProperties.STATE);
		required.add(CommonProperties.PLAYER);
		ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        ExpressionReward quantifiedReward = (ExpressionReward) propertyQuantifier.getQuantified();
        required.add(quantifiedReward.getReward());
        RewardType rewardType = quantifiedReward.getRewardType();
        if (rewardType.isReachability()) {
        	required.addAll(modelChecker.getRequiredNodeProperties(quantifiedReward.getRewardReachSet(), forStates));
        }
		return Collections.unmodifiableSet(required);
	}

	@Override
	public Set<Object> getRequiredEdgeProperties() throws EPMCException {
		Set<Object> required = new LinkedHashSet<>();
		required.add(CommonProperties.WEIGHT);
		ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        required.add(((ExpressionReward) (propertyQuantifier.getQuantified())).getReward());
		return Collections.unmodifiableSet(required);
	}

    @Override
    public StateMap solve() throws EPMCException {
		ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        ExpressionReward quantifiedProp = (ExpressionReward) propertyQuantifier.getQuantified();
        if (quantifiedProp.getRewardType().isReachability()) {
        	StateSetExplicit allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        	StateMapExplicit innerResult = (StateMapExplicit) modelChecker.check(quantifiedProp.getRewardReachSet(), allStates);
            UtilGraph.registerResult(graph, quantifiedProp.getRewardReachSet(), innerResult);
            allStates.close();
        }
        DirType dirType = ExpressionQuantifier.computeQuantifierDirType(propertyQuantifier);
        boolean min = dirType == DirType.MIN;
        StateMap result = doSolve(quantifiedProp, (StateSetExplicit) forStates, min);
        if (propertyQuantifier.getCompareType() != CmpType.IS) {
            StateMap compare = modelChecker.check(propertyQuantifier.getCompare(), forStates);
            Operator op = ContextValue.get().getOperator(propertyQuantifier.getCompareType().asExOpType());
            result = result.applyWith(op, compare);
        }
        return result;
    }

    public StateMap doSolve(Expression property, StateSetExplicit states, boolean min)
            throws EPMCException {
        RewardSpecification rewardStructure = ((ExpressionReward) property).getReward();
        NodeProperty stateReward = graph.getNodeProperty(rewardStructure);
        EdgeProperty transReward = graph.getEdgeProperty(rewardStructure);
        return solve(property, states, min, stateReward, transReward);
    }

    public StateMapExplicit solve(Expression property, StateSetExplicit states, boolean min,
            NodeProperty stateReward, EdgeProperty transReward)
                    throws EPMCException {
        assert property != null;
        assert states != null;
        assert stateReward != null;
        assert transReward != null;
        RewardType rewardType = ((ExpressionReward) property).getRewardType();
        switch (rewardType) {
		case CUMULATIVE:
		case DISCOUNTED:
		case REACHABILITY:
			return solveCumulativeOrReachability(property, states, min, stateReward, transReward);
		case INSTANTANEOUS:
			return solveInstantaneous(property, states, min, stateReward, transReward);
		case STEADYSTATE:
			// TODO
			assert false;
			break;
		default:
			assert false;
			break;
        }
        return null;
    }

    public StateMapExplicit solveInstantaneous(Expression property, StateSetExplicit states, boolean min,
            NodeProperty stateReward, EdgeProperty transReward)
                    throws EPMCException {
        assert property != null;
        assert states != null;
        assert stateReward != null;
        assert transReward != null;
        ExpressionReward propertyReward = (ExpressionReward) property;
        ValueAlgebra time = ValueAlgebra.asAlgebra(evaluateValue(propertyReward.getTime()));
        ValueArrayAlgebra values = UtilValue.newArray(TypeWeight.get().getTypeArray(), graph.getNumNodes());
        for (int graphNode = 0; graphNode < graph.getNumNodes(); graphNode++) {
        	Value reward = stateReward.get(graphNode);
        	values.set(reward, graphNode);
        }
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        GraphSolverObjectiveExplicitBounded objective = new GraphSolverObjectiveExplicitBounded();
        objective.setGraph(graph);
        objective.setMin(min);
        objective.setValues(values);
        objective.setTime(time);
        configuration.setObjective(objective);
        configuration.solve();
        values = objective.getResult();
        StateMapExplicit result = valuesToResult(values, states);
        
        return result;
    }

    public StateMapExplicit solveCumulativeOrReachability(Expression property, StateSetExplicit states, boolean min,
            NodeProperty stateReward, EdgeProperty transReward)
                    throws EPMCException {
        assert property != null;
        assert states != null;
        assert stateReward != null;
        assert transReward != null;
        BitSet reachSink = computeReachSink(property);
        BitSet reachNotOneSink = computeReachNotOneSink(property, reachSink, min);
        ExpressionReward propertyReward = (ExpressionReward) property;
        ValueAlgebra time = ValueAlgebra.asAlgebra(evaluateValue(propertyReward.getTime()));
        NodeProperty statesProp = graph.getNodeProperty(CommonProperties.STATE);
        ValueArrayAlgebra values = UtilValue.newArray(TypeWeight.get().getTypeArray(), graph.getNumNodes());

        List<BitSet> sinks = new ArrayList<>();
        if (reachSink.length() > 0) {
            sinks.add(reachSink);
        }
        if (reachNotOneSink.length() > 0) {
            sinks.add(reachNotOneSink);
        }
        ValueArrayAlgebra cumulRewards = buildCumulativeRewards(sinks, reachSink, reachNotOneSink, stateReward, transReward);
        GraphSolverConfigurationExplicit configuration = UtilGraphSolver.newGraphSolverConfigurationExplicit();
        ExpressionReward quantifiedReward = ExpressionReward.asReward(property);
        RewardType rewardType = quantifiedReward.getRewardType();
        if (rewardType.isCumulative() && !time.isPosInf()) {
            GraphSolverObjectiveExplicitBoundedCumulative objective = new GraphSolverObjectiveExplicitBoundedCumulative();
            objective.setGraph(graph);
            objective.setMin(min);
            objective.setTime(time);
            objective.setStateRewards(cumulRewards);
            configuration.setObjective(objective);
            configuration.solve();
            values = objective.getResult();
        } else if (rewardType.isReachability() || (rewardType.isCumulative() && time.isPosInf())) {
            GraphSolverObjectiveExplicitUnboundedCumulative objective = new GraphSolverObjectiveExplicitUnboundedCumulative();
            objective.setStateRewards(cumulRewards);
            objective.setGraph(graph);
            objective.setMin(min);
            objective.setSinks(sinks);
            configuration.setObjective(objective);
            configuration.solve();
            values = objective.getResult();
        } else if (rewardType.isDiscounted()) {
            GraphSolverObjectiveExplicitBoundedCumulativeDiscounted objective = new GraphSolverObjectiveExplicitBoundedCumulativeDiscounted();
            objective.setStateRewards(cumulRewards);
            objective.setGraph(graph);
            objective.setMin(min);
            objective.setDiscount(ValueReal.asReal(evaluateValue(propertyReward.getDiscount())));
            objective.setTime(time);
            configuration.setObjective(objective);
            configuration.solve();
            values = objective.getResult();
        }
        if (rewardType.isReachability()) {
        	for (int graphNode = 0; graphNode < graph.getNumNodes(); graphNode++) {
                if (statesProp.getBoolean(graphNode) && reachNotOneSink.get(graphNode)) {
                    values.set(TypeWeight.get().getPosInf(), graphNode);
                }
            }
        }

        StateMapExplicit result = valuesToResult(values, states);
        return result;
    }

    private ValueArrayAlgebra buildCumulativeRewards(List<BitSet> sinks, BitSet reachSink, BitSet reachNotOneSink, NodeProperty stateReward, EdgeProperty transReward) throws EPMCException {
        Semantics semantics = graph.getGraphPropertyObject(CommonProperties.SEMANTICS);
		if (SemanticsMarkovChain.isMarkovChain(semantics)) {
			return buildCumulativeRewardsMC(sinks, reachSink, reachNotOneSink, stateReward, transReward);
        } else if (SemanticsMDP.isMDP(semantics) || SemanticsIMDP.isIMDP(semantics)) {
        	return buildCumulativeRewardsMDP(sinks, reachSink, reachNotOneSink, stateReward, transReward);
        } else {
        	assert false;
        	return null;
        }    	
    }
    
    private ValueArrayAlgebra buildCumulativeRewardsMC(List<BitSet> sinks, BitSet reachSink, BitSet reachNotOneSink, NodeProperty stateReward, EdgeProperty transReward) throws EPMCException {
    	ValueArrayAlgebra cumulRewards = UtilValue.newArray(TypeWeight.get().getTypeArray(), graph.computeNumStates());
        ValueAlgebra acc = newValueWeight();
        ValueAlgebra acc2 = newValueWeight();
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        int cumulRewIdx = 0;
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        ExpressionReward quantifiedReward = (ExpressionReward) propertyQuantifier.getQuantified();
        RewardType rewardType = quantifiedReward.getRewardType();
        if (rewardType.isReachability()) {
        	cumulRewIdx = sinks.size();
        }
        for (int graphNode = 0; graphNode < graph.getNumNodes(); graphNode++) {
        	if (reachSink.get(graphNode) || reachNotOneSink.get(graphNode)) {
        		continue;
        	}
        	int numSuccessors = graph.getNumSuccessors(graphNode);
        	Value nodeRew = stateReward.get(graphNode);
        	Player player = playerProp.getEnum(graphNode);
        	EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
        	acc.set(nodeRew);                        
        	for (int succNr = 0; succNr < numSuccessors; succNr++) {
        		Value succWeight = weight.get(graphNode, succNr);
        		ValueAlgebra transRew = ValueAlgebra.asAlgebra(transReward.get(graphNode, succNr));
        		if (player == Player.STOCHASTIC) {
        			// TODO hack for imdps
  //                      acc2.multiply(succWeight, transRew);
//	                        acc.add(acc, acc2);
        			if (!transRew.isZero()) {
        			}
        		} else {
        			acc.add(acc, transRew);
        			int succ = graph.getSuccessorNode(graphNode, succNr);
        			ValueAlgebra r = ValueAlgebra.asAlgebra(transReward.get(succ, 0));
        			acc.add(acc2, r);
        		}
        	}
        	cumulRewards.set(acc, graphNode);
        }
		return cumulRewards;
	}

    private ValueArrayAlgebra buildCumulativeRewardsMDP(List<BitSet> sinks, BitSet reachSink, BitSet reachNotOneSink, NodeProperty stateReward, EdgeProperty transReward) throws EPMCException {
        int numNondet = graph.getNumNodes() - graph.computeNumStates();
        ValueArrayAlgebra cumulRewards = UtilValue.newArray(TypeWeight.get().getTypeArray(), numNondet);
        ValueAlgebra acc = newValueWeight();
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        int cumulRewIdx = 0;
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        ExpressionReward quantifiedReward = (ExpressionReward) propertyQuantifier.getQuantified();
        RewardType rewardType = quantifiedReward.getRewardType();
        if (rewardType.isReachability()) {
        	cumulRewIdx = sinks.size();
        }
        for (int graphNode = 0; graphNode < graph.getNumNodes(); graphNode++) {
        	if (reachSink.get(graphNode) || reachNotOneSink.get(graphNode)) {
        		continue;
        	}
        	int numSuccessors = graph.getNumSuccessors(graphNode);
        	Value nodeRew = stateReward.get(graphNode);
        	Player player = playerProp.getEnum(graphNode);
        	EdgeProperty weight = graph.getEdgeProperty(CommonProperties.WEIGHT);
        	for (int succNr = 0; succNr < numSuccessors; succNr++) {
        		acc.set(nodeRew);                        
        		Value succWeight = weight.get(graphNode, succNr);
        		ValueAlgebra transRew = ValueAlgebra.asAlgebra(transReward.get(graphNode, succNr));
        		if (player == Player.STOCHASTIC) {
        		} else {
        			acc.add(acc, transRew);
        			int succ = graph.getSuccessorNode(graphNode, succNr);
        			ValueAlgebra r = ValueAlgebra.asAlgebra(transReward.get(succ, 0));
        			acc.add(acc, r);
        		}
        		if (player == Player.ONE) {
        			cumulRewards.set(acc, cumulRewIdx);
        			cumulRewIdx++;
        		}
        	}
        }
		return cumulRewards;
	}

	private BitSet computeReachNotOneSink(Expression property, BitSet reachSink, boolean min)
            throws EPMCException {
        assert property != null;
        RewardType rewardType = ((ExpressionReward) property).getRewardType();
        BitSet reachNotOneSink = UtilBitSet.newBitSetUnbounded();
        BitSet graphNodes = UtilBitSet.newBitSetUnbounded();
        graphNodes.set(0, graph.getNumNodes());
        if (rewardType.isReachability()) {
            ComponentsExplicit components = UtilAlgorithms.newComponentsExplicit();
            BitSet reachOneSink = components.reachPre(graph, reachSink, !min, true);
            reachNotOneSink = UtilBitSet.newBitSetUnbounded();
            reachNotOneSink.or(graphNodes);
            reachNotOneSink.andNot(reachOneSink);
        }
        return reachNotOneSink;
    }

    private BitSet computeReachSink(Expression property) throws EPMCException {
        assert property != null;
        ExpressionReward propertyReward = (ExpressionReward) property;
        RewardType rewardType = propertyReward.getRewardType();
        BitSet reachSink = UtilBitSet.newBitSetUnbounded();
        if (rewardType.isReachability()) {
        	NodeProperty reachSet = graph.getNodeProperty(propertyReward.getRewardReachSet());
        	for (int graphNode = 0; graphNode < graph.getNumNodes(); graphNode++) {
                if (reachSet.getBoolean(graphNode)) {
                    reachSink.set(graphNode);
                }
            }
        }
        return reachSink;
    }

    private StateMapExplicit valuesToResult(ValueArray values, StateSetExplicit states) {
        assert values != null;
        assert states != null;
        Type typeWeight = TypeWeight.get();
        ValueArray resultValues = newValueArrayWeight(states.size());
        Value entry = typeWeight.newValue();
        for (int i = 0; i < states.size(); i++) {
            values.get(entry, i);
            resultValues.set(entry, i);
        }
        return UtilGraph.newStateMap(states.clone(), resultValues);
    }

    @Override
    public boolean canHandle() throws EPMCException {
        assert property != null;
        if (!(modelChecker.getEngine() instanceof EngineExplicit)) {
            return false;
        }
        if (!(property instanceof ExpressionQuantifier)) {
            return false;
        }
        ExpressionQuantifier propertyQuantifier = (ExpressionQuantifier) property;
        if (!(propertyQuantifier.getQuantified() instanceof ExpressionReward)) {
            return false;
        }
        ExpressionReward quantifiedReward = (ExpressionReward) propertyQuantifier.getQuantified();
        if (((ExpressionReward) (propertyQuantifier.getQuantified())).getRewardType().isReachability()) {
        	StateSet allStates = UtilGraph.computeAllStatesExplicit(modelChecker.getLowLevel());
        	modelChecker.ensureCanHandle(quantifiedReward.getRewardReachSet(), allStates);
        	if (allStates != null) {
        		allStates.close();
        	}
        }
        return true;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
    
    private ValueArray newValueArrayWeight(int size) {
        TypeArray typeArray = TypeWeight.get().getTypeArray();
        return UtilValue.newArray(typeArray, size);
    }
    
    private ValueAlgebra newValueWeight() {
    	return TypeWeight.get().newValue();
    }
    
    private Value evaluateValue(Expression expression) throws EPMCException {
        assert expression != null;
        EvaluatorExplicit evaluator = UtilEvaluatorExplicit.newEvaluator(expression, graph, new Expression[0]);
        return evaluator.evaluate();
    }
}
