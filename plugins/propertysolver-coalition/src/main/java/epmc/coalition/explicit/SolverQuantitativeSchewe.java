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

package epmc.coalition.explicit;

import epmc.automaton.AutomatonParityLabel;
import epmc.coalition.graphsolver.GraphSolverObjectiveExplicitUnboundedReachabilityGame;
import epmc.coalition.messages.MessagesCoalition;
import epmc.coalition.options.OptionsCoalition;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.Scheduler;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.NodePropertyGeneral;
import epmc.graph.explicit.SchedulerSimple;
import epmc.graph.explicit.SchedulerSimpleArray;
import epmc.graph.explicit.SchedulerSimpleSettable;
import epmc.graph.explicit.induced.GraphExplicitInduced;
import epmc.graph.explicit.subgraph.GraphExplicitSubgraph;
import epmc.graphsolver.UtilGraphSolver;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeObject;
import epmc.value.TypeWeight;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueArray;
import epmc.value.ValueArrayAlgebra;
import epmc.value.TypeObject.StorageType;

// TODO fix issue with neutral transitions: ">=", "<=" to "==".

public final class SolverQuantitativeSchewe implements SolverQuantitative {
	/** Identifier of this solver. */
	public static String IDENTIFIER = "schewe";
	/** String containing a single space. */
	private final static String SPACE = " ";
	private GraphExplicit game;
	/** Whether to compute a strategy for the even player. */
	private boolean computeStrategyP0;
	/** Whether to compute a strategy for the odd player. */
	private boolean computeStrategyP1;
	/** Whether to reduce output printed. */
	private boolean reduceOutput;
	/** Silencing status of log before using this solver. */
	private boolean storedLogStatus;
	/** Number of recursive calls to initialisation function. */
	private int numInitCalls;
	/** Time spent in qualitative game solvers during initialisation. */
	private StopWatch initQualitativeSolver;
	/** Time spent for reachability computations during initialisation. */
	private StopWatch initReach;
	/** Comparison tolerance used. */
	private double compareTolerance;
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setGame(GraphExplicit game) {
		assert game != null;
		this.game = game;
	}

	@Override
	public void setComputeStrategies(boolean playerEven, boolean playerOdd) {
		this.computeStrategyP0 = playerEven;
		this.computeStrategyP1 = playerOdd;
	}

	@Override
	public QuantitativeResult solve() throws EPMCException {
		compareTolerance = getOptions().getDouble(OptionsCoalition.COALITION_QUANTITATIVE_SCHEWE_COMPARE_TOLERANCE);
		StopWatch totalTime = new StopWatch(true);
		getLog().send(MessagesCoalition.COALITION_QUANTITATIVE_SCHEWE_START);
		reduceOutput = getOptions().get(OptionsCoalition.COALITION_QUANTITATIVE_SCHEWE_SILENCE_INTERNAL);
		storedLogStatus = getLog().isSilent();
		
    	if (!game.getNodeProperties().contains(CommonProperties.STATE)) {
    		game.registerNodeProperty(CommonProperties.STATE,
    				new NodePropertyPlayerToState(game,
    						game.getNodeProperty(CommonProperties.PLAYER)));
    	}
    	
		BitSet nodes = UtilBitSet.newBitSetBounded(game.getNumNodes());
		nodes.set(0, game.getNumNodes());
		getLog().send(MessagesCoalition.COALITION_QUANTITATIVE_SCHEWE_INITIALISE_START);
		StopWatch initialiseTime = new StopWatch(true);
		initQualitativeSolver = new StopWatch(false);
		initReach = new StopWatch(false);
		SchedulerSimpleSettable strategies = initialise(nodes);
		getLog().send(MessagesCoalition.COALITION_QUANTITATIVE_SCHEWE_INITIALISE_DONE,
				initialiseTime.getTimeSeconds(),
				numInitCalls,
				initQualitativeSolver.getTimeSeconds(),
				initReach.getTimeSeconds());
		
		QuantitativeResult result = improve(strategies);
		getLog().send(MessagesCoalition.COALITION_QUANTITATIVE_SCHEWE_DONE, totalTime.getTimeSeconds());
		return result;
	}

	private SchedulerSimpleSettable initialise(BitSet p) throws EPMCException {
		assert p != null;
		NodeProperty playerProperty = game.getNodeProperty(CommonProperties.PLAYER);
		numInitCalls++;
        SolverQualitative qualitativeSolver = newQualitativeSolver();
		GraphExplicitSubgraph subGraph = new GraphExplicitSubgraph(game, p);
		assert assertSubGraphSuccessors(subGraph);

		qualitativeSolver.setGame(subGraph);
		qualitativeSolver.setStrictEven(true);
		qualitativeSolver.setComputeStrategies(true, false);
		silenceLog();
		initQualitativeSolver.start();
		QualitativeResult subPair = qualitativeSolver.solve();
		initQualitativeSolver.stop();
		unsilenceLog();
		if (subPair.getSet0().isEmpty()) {
			SchedulerSimpleSettable result = new SchedulerSimpleArray(game);
			for (int node = p.nextSetBit(0); node >= 0; node = p.nextSetBit(node + 1)) {
				Player player = playerProperty.getEnum(node);
				if (player != Player.ONE) {
					continue;
				}
				result.set(node, 0);
			}
			return result;
		}
		SchedulerSimpleSettable strategy = new SchedulerSimpleArray(game);
		NodeProperty playerPropertySubgame = subGraph.getNodeProperty(CommonProperties.PLAYER);
		int numSubGraphNodes = subGraph.getNumNodes();
		for (int subNode = 0; subNode < numSubGraphNodes; subNode++) {
			Player player = playerPropertySubgame.getEnum(subNode);
			if (player != Player.ONE) {
				continue;
			}
			int origNode = subGraph.subToOrig(subNode);
			int subDecision = subPair.getStrategies().getDecision(subNode);
			int origDecision = subGraph.getOrigSuccNumber(subNode, subDecision);
			assert origDecision != Scheduler.UNSET;
			strategy.set(origNode, origDecision);
		}

		initReach.start();
		QuantitativeResult quantiRes = reach(subGraph, subPair.getSet0(), true);
		initReach.stop();
		assert quantiRes.getProbabilities() != null;
		assert quantiRes.getStrategies() != null;
		for (int subNode = 0; subNode < numSubGraphNodes; subNode++) {
			Player player = playerPropertySubgame.getEnum(subNode);
			if (player == Player.ONE && !subPair.getSet0().get(subNode)) {
				int subDecision = quantiRes.getStrategies().getDecision(subNode);
				assert subDecision != Scheduler.UNSET : subNode;
				int origDecision = subGraph.getOrigSuccNumber(subNode, subDecision);
				int origNode = subGraph.subToOrig(subNode);
				strategy.set(origNode, origDecision);
			}
		}
		
		BitSet zeroNodes = UtilBitSet.newBitSetUnbounded();
		Value zero = TypeWeight.get(game.getContextValue()).getZero();
		Value entry = newValueWeight();
		for (int subNode = 0; subNode < numSubGraphNodes; subNode++) {
			quantiRes.getProbabilities().get(entry, subNode);
			if (entry.distance(zero) < 1E-24) {
				int origNode = subGraph.subToOrig(subNode);
				zeroNodes.set(origNode);
			}
		}
		SchedulerSimple g = initialise(zeroNodes);
		for (int origNode = zeroNodes.nextSetBit(0); origNode >= 0; origNode = zeroNodes.nextSetBit(origNode + 1)) {
			Player player = playerProperty.getEnum(origNode);
			if (player == Player.ONE) {
				assert g.getDecision(origNode) != Scheduler.UNSET;
				strategy.set(origNode, g.getDecision(origNode));
			}
		}
		assert assertStrategy(p, strategy);
		return strategy;
	}

    private boolean assertStrategy(BitSet p, SchedulerSimple strategy) throws EPMCException {
    	assert p != null;
    	assert strategy != null;
    	NodeProperty playerProperty = game.getNodeProperty(CommonProperties.PLAYER);
		for (int origNode = p.nextSetBit(0); origNode >= 0; origNode = p.nextSetBit(origNode + 1)) {
			Player player = playerProperty.getEnum(origNode);
			if (player == Player.ONE) {
				assert strategy.getDecision(origNode) != Scheduler.UNSET;
			} else {
				assert strategy.getDecision(origNode) == Scheduler.UNSET;				
			}
		}
		return true;
	}

	private boolean assertSubGraphSuccessors(GraphExplicitSubgraph subGraph) throws EPMCException {
    	assert subGraph != null;
    	int numSubGraphNodes = subGraph.getNumNodes();
		for (int node = 0; node < numSubGraphNodes; node++) {
			assert subGraph.getNumSuccessors(node) > 0 : node;
		}
		return true;
	}

	QuantitativeResult reach(GraphExplicit graph, BitSet target, boolean computeStrategy) throws EPMCException {
        GraphSolverObjectiveExplicitUnboundedReachabilityGame objective = new GraphSolverObjectiveExplicitUnboundedReachabilityGame();
        objective.setGraph(graph);
        objective.setComputeScheduler(computeStrategy);
        objective.setTarget(target);
        silenceLog();
        UtilGraphSolver.solve(objective);
        unsilenceLog();
		assert reachSanityCheck(graph, target, objective);
        return new QuantitativeResult(objective.getResult(), objective.getScheduler());
	}

	private boolean reachSanityCheck(GraphExplicit graph, BitSet target, GraphSolverObjectiveExplicitUnboundedReachabilityGame objective) throws EPMCException {
		int numStates = graph.getNumNodes();
		NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
		EdgeProperty weightProp = graph.getEdgeProperty(CommonProperties.WEIGHT);
		ValueArray values = objective.getResult();
		ValueAlgebra value = objective.getResult().getType().getEntryType().newValue();
		ValueAlgebra newValue = objective.getResult().getType().getEntryType().newValue();
		Value succValue = values.getType().getEntryType().newValue();
		ValueAlgebra weighted = ValueAlgebra.asAlgebra(values.getType().getEntryType().newValue());
		for (int node = 0; node < numStates; node++) {
			values.get(value, node);
			assert !target.get(node) || value.isOne();
			Player player = playerProp.getEnum(node);
			if (player == Player.ONE) {
				newValue.set(TypeWeight.asWeight(newValue.getType()).getNegInf());
			} else if (player == Player.TWO) {
				newValue.set(TypeWeight.asWeight(newValue.getType()).getPosInf());				
			} else if (player == Player.STOCHASTIC) {
				newValue.set(0);				
			}
			int numSuccessors = graph.getNumSuccessors(node);
			for (int succ = 0; succ < numSuccessors; succ++) {
				int succNode = graph.getSuccessorNode(node, succ);
				values.get(succValue, succNode);
				if (player == Player.ONE) {
					newValue.max(newValue, succValue);
				} else if (player == Player.TWO) {
					newValue.min(newValue, succValue);
				} else if (player == Player.STOCHASTIC) {
					weighted.multiply(succValue, weightProp.get(node, succ));
					newValue.add(newValue, weighted);
				}
			}
			assert value.distance(newValue) < 1E-8 : node + SPACE + player
				+ SPACE + value + SPACE + newValue;
		}
		return true;
	}

	private QuantitativeResult improve(SchedulerSimpleSettable strategies) throws EPMCException {
		StopWatch improveTime = new StopWatch(true);
		StopWatch mdpEvaluateTime = new StopWatch(false);
		StopWatch qualitativeEvaluationTime = new StopWatch(false);
		getLog().send(MessagesCoalition.COALITION_QUANTITATIVE_SCHEWE_IMPROVE_START);
		assert strategies != null;
		int numNodes = game.getNumNodes();
		Value value = TypeWeight.get(getContextValue()).newValue();
		ValueAlgebra succValue = TypeWeight.get(getContextValue()).newValue();
		NodeProperty playerProperty = game.getNodeProperty(CommonProperties.PLAYER);
		boolean changed = true;
		ValueArrayAlgebra values = null;
		QuantitativeResult evaluatedResult = null;
		int numDirectImprovements = 0;
		int numIndirectImprovements = 0;
		while (changed) {
			mdpEvaluateTime.start();
			evaluatedResult = evaluateMDP(strategies);
			mdpEvaluateTime.stop();
			values = evaluatedResult.getProbabilities();
			changed = false;
			for (int node = 0; node < numNodes; node++) {
				Player player = playerProperty.getEnum(node);
				if (player != Player.ONE) {
					continue;
				}
				int numSuccessors = game.getNumSuccessors(node);
				values.get(value, node);
				boolean doChange = false;
				for (int succ = 0; succ < numSuccessors; succ++) {
					int succNode = game.getSuccessorNode(node, succ);
					values.get(succValue, succNode);
					if (succValue.isGt(value) && !(succValue.distance(value) < compareTolerance)) {
						doChange = true;
					}
				}
				if (!doChange) {
					continue;
				}
				for (int succ = 0; succ < numSuccessors; succ++) {
					int succNode = game.getSuccessorNode(node, succ);
					values.get(succValue, succNode);
					if (succValue.isGt(value)) {
						strategies.set(node, succ);
						value.set(succValue);
						changed = true;
					}
				}
			}
			numDirectImprovements += changed ? 1 : 0;
			if (!changed) {
				BitSet restriction = computeRestriction(values);
				GraphExplicitRestricted restricted = new GraphExplicitRestricted(game, restriction);
		        SolverQualitative qualitativeSolver = newQualitativeSolver();
		        qualitativeSolver.setComputeStrategies(true, false);
		        qualitativeSolver.setGame(restricted);
		        qualitativeSolver.setStrictEven(true);
		        silenceLog();
		        qualitativeEvaluationTime.start();
		        QualitativeResult restrictedResult = qualitativeSolver.solve();
		        qualitativeEvaluationTime.stop();
		        unsilenceLog();
		        changed = false;
		        BitSet restrictedWon = restrictedResult.getSet0();
		        for (int node = restrictedWon.nextSetBit(0); node >= 0;
		        		node = restrictedWon.nextSetBit(node + 1)) {
					Player player = playerProperty.getEnum(node);
					if (player != Player.ONE) {
						continue;
					}
		        	int restrictedDecision = restrictedResult.getStrategies().getDecision(node);
		        	int origDecision = restricted.getOrigSuccNumber(node, restrictedDecision);
		        	if (strategies.getDecision(node) != origDecision) {
		        		strategies.set(node, origDecision);
		        		changed = true;
		        	}
		        }
		        numIndirectImprovements += changed ? 1 : 0;
			}
		}
		if (!computeStrategyP0) {
			for (int node = 0; node < numNodes; node++) {
				Player player = playerProperty.getEnum(node);
				if (player == Player.ONE) {
					strategies.set(node, Scheduler.UNSET);
				}
			}
		}
		if (computeStrategyP1) {
			for (int node = 0; node < numNodes; node++) {
				Player player = playerProperty.getEnum(node);
				if (player == Player.TWO) {
					strategies.set(node, evaluatedResult.getStrategies().getDecision(node));
				}
			}
		}
		getLog().send(MessagesCoalition.COALITION_QUANTITATIVE_SCHEWE_IMPROVE_DONE,
				improveTime.getTimeSeconds(),
				numDirectImprovements,
				numIndirectImprovements,
				mdpEvaluateTime.getTimeSeconds(),
				qualitativeEvaluationTime.getTimeSeconds());
		return new QuantitativeResult(values, strategies);
	}
	
    private BitSet computeRestriction(ValueArray values) throws EPMCException {
    	assert values != null;
    	int maxNumSuccessors = 0;
    	int numNodes = game.getNumNodes();
    	for (int node = 0; node < numNodes; node++) {
    		maxNumSuccessors = Math.max(maxNumSuccessors, game.getNumSuccessors(node));
    	}
    	BitSet result = UtilBitSet.newBitSetBounded(numNodes * maxNumSuccessors);
    	Value value = newValueWeight();
    	ValueAlgebra succValue = newValueWeight();
		NodeProperty playerProperty = game.getNodeProperty(CommonProperties.PLAYER);
    	for (int node = 0; node < numNodes; node++) {
    		values.get(value, node);
    		Player player = playerProperty.getEnum(node);
    		int numSuccessors = game.getNumSuccessors(node);
    		for (int succ = 0; succ < numSuccessors; succ++) {
    			int succState = game.getSuccessorNode(node, succ);
    			values.get(succValue, succState);
    			if (player == Player.STOCHASTIC
    					|| player == Player.ONE && succValue.isGe(value)
    					|| player == Player.ONE && succValue.distance(value) < compareTolerance
    					|| player == Player.TWO && succValue.isLe(value)
    				    || player == Player.TWO && succValue.distance(value)< compareTolerance) {
    				result.set(node * maxNumSuccessors + succ);
    			}
    		}
    	}    	
		return result;
	}

	private QuantitativeResult evaluateMDP(SchedulerSimple strategies) throws EPMCException {
    	assert strategies != null;
    	GraphExplicitInduced induced = new GraphExplicitInduced(game, strategies);

    	NodeProperty oldLabelProp = induced.getNodeProperty(CommonProperties.AUTOMATON_LABEL);    	
    	Type typeLabel = new TypeObject.Builder()
                .setContext(getContextValue())
                .setClazz(SettableParityLabel.class)
                .setStorageClass(StorageType.NUMERATED_IDENTITY)
                .build();
    	NodeProperty newLabelProp = new NodePropertyGeneral(induced, typeLabel, false);
    	NodeProperty oldPlayerProp = induced.getNodeProperty(CommonProperties.PLAYER);
    	NodePropertyGeneral newPlayerProp = new NodePropertyGeneral(induced, oldPlayerProp.getType(), false);
    	int numNodes = induced.getNumNodes();

    	int numPrios = 0;
    	boolean hasInf = false;
    	for (int node = 0; node < numNodes; node++) {
    		AutomatonParityLabel oldLabel = oldLabelProp.getObject(node);
    		if (oldLabel.getPriority() == Integer.MAX_VALUE) {
    			hasInf = true;
    		} else {
    			numPrios = Math.max(numPrios, oldLabel.getPriority());
    		}
    	}
    	numPrios += 2;
    	SettableParityLabel labels[] = new SettableParityLabel[numPrios + (hasInf ? 1 : 0)];
    	for (int prio = 0; prio < numPrios; prio++) {
    		labels[prio] = new SettableParityLabel(prio);
    	}
    	if (hasInf) {
    		labels[numPrios] = new SettableParityLabel(Integer.MAX_VALUE);
    	}
    	for (int node = 0; node < numNodes; node++) {
    		AutomatonParityLabel oldLabel = oldLabelProp.getObject(node);
    		int prio = oldLabel.getPriority();
    		if (prio == Integer.MAX_VALUE) {
    			newLabelProp.set(node, labels[numPrios]);
    		} else {
    			newLabelProp.set(node, labels[prio + 1]);
    		}
    		Player oldPlayer = oldPlayerProp.getEnum(node);
    		Player newPlayer = null;
    		if (oldPlayer == Player.ONE) {
    			newPlayer = Player.TWO;
    		} else if (oldPlayer == Player.TWO) {
    			newPlayer = Player.ONE;
    		} else if (oldPlayer == Player.STOCHASTIC) {
    			newPlayer = Player.STOCHASTIC;
    		}
    		newPlayerProp.set(node, newPlayer);
    	}
    	induced.removeNodeProperty(CommonProperties.AUTOMATON_LABEL);
    	induced.removeNodeProperty(CommonProperties.PLAYER);
    	induced.registerNodeProperty(CommonProperties.AUTOMATON_LABEL, newLabelProp);
    	induced.registerNodeProperty(CommonProperties.PLAYER, newPlayerProp);
    	
        SolverQualitative qualitativeSolver = newQualitativeSolver();
		qualitativeSolver.setGame(induced);
		qualitativeSolver.setStrictEven(true);
		qualitativeSolver.setComputeStrategies(false, computeStrategyP1);
		silenceLog();
		BitSet reach = qualitativeSolver.solve().getSet0();
		unsilenceLog();
		QuantitativeResult result = reach(induced, reach, computeStrategyP1);
		ValueArrayAlgebra probabilities = result.getProbabilities();
		ValueAlgebra value = probabilities.getType().getEntryType().newValue();
		Value one = value.getType().getOne();
		for (int i = 0; i < probabilities.size(); i++) {
			probabilities.get(value, i);
			value.subtract(one, value);
			probabilities.set(value, i);			
		}
		return result;
	}

    private SolverQualitative newQualitativeSolver() throws EPMCException {
        return UtilOptions.getInstance(getOptions(),
                OptionsCoalition.COALITION_SOLVER);
    }

    /**
     * Get log used by this solver.
     * 
     * @return log used by this solver
     */
    private Log getLog() {
    	return getOptions().get(OptionsMessages.LOG);
    }
    
    /**
     * Obtain options used.
     * 
     * @return options used
     */
	private Options getOptions() {
    	return game.getOptions();
    }

	/**
	 * Obtain value context used.
	 * 
	 * @return value context used.
	 */
    private ContextValue getContextValue() {
    	return game.getContextValue();
    }
    
    /**
     * Disable log output.
     * This method will only have an effect, if 
     * {@link OptionsCoalition#COALITION_QUANTITATIVE_SCHEWE_SILENCE_INTERNAL}
     * is set to {@code true} to prevent excessive output.
     */
    private void silenceLog() {
    	if (reduceOutput) {
    		getLog().setSilent(true);
    	}
    }
    
    /**
     * Set the log silencing state to state before using this solver.
     * This method will only have an effect, if 
     * {@link OptionsCoalition#COALITION_QUANTITATIVE_SCHEWE_SILENCE_INTERNAL}
     * is set to {@code true} to prevent excessive output.
     */
    private void unsilenceLog() {
    	if (reduceOutput) {
    		getLog().setSilent(storedLogStatus);
    	}
    }
    
    private ValueAlgebra newValueWeight() {
    	return TypeWeight.get(getContextValue()).newValue();
    }
}
