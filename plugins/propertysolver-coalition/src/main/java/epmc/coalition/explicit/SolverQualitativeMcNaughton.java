package epmc.coalition.explicit;

import epmc.automaton.AutomatonParityLabel;
import epmc.coalition.messages.MessagesCoalition;
import epmc.coalition.options.OptionsCoalition;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.Scheduler;
import epmc.graph.explicit.SchedulerSimple;
import epmc.graph.explicit.SchedulerSimpleArray;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;

// TODO correct options for strategy computation

public final class SolverQualitativeMcNaughton implements SolverQualitative {
	public final static String IDENTIFIER = "mcnaughton";
	
	/** Stochastic game to be solved. */
	private GraphExplicit game;
	/** Use shortcut for subgames where colors either all even or all odd. */
	private boolean sameColorShortCut;
	/** Empty bitset. Do not modify this object. */
    private BitSet EMPTY_BIT_SET;
	/** Empty bitset pair. Do not modify this object. */
    private QualitativeResult EMPTY_BIT_SET_PAIR;
	/** Whether to compute a strategy for the even player. */
	private boolean computeStrategyP0;
	/** Whether to compute a strategy for the odd player. */
	private boolean computeStrategyP1;
	/** Whether the even player must win with probability 1. */
	private boolean strictEven;
	/** Whether the odd player must win with probability 1. */
	private boolean strictOdd;
	/** Number of recursive calls to the algorithm. */
	private int zeroMcNaughtonCalls;
	/** Nodes which belong to the even or the stochastic player. */
	private BitSet playerEvenOrStochastic;
	/** Nodes which belong to the odd or the stochastic player. */
	private BitSet playerOddOrStochastic;

	private NodeProperty propertyPlayer;

	@Override
	public void setGame(GraphExplicit game) {
		assert game != null;
		this.game = game;
	}

	@Override
	public void setStrictEven(boolean strictEven) {
		this.strictEven = strictEven;
		this.strictOdd = !strictEven;
	}

	@Override
	public void setComputeStrategies(boolean playerEven, boolean playerOdd) {
		computeStrategyP0 = playerEven;
		computeStrategyP1 = playerOdd;
	}

	@Override
	public QualitativeResult solve() throws EPMCException {
		StopWatch watch = new StopWatch(true);
		getLog().send(MessagesCoalition.COALITION_STOCHASTIC_MCNAUGHTON_START);
		int numNodes = game.getNumNodes();
		playerEvenOrStochastic = UtilBitSet.newBitSetBounded(numNodes);
		playerOddOrStochastic = UtilBitSet.newBitSetBounded(numNodes);
		propertyPlayer = game.getNodeProperty(CommonProperties.PLAYER);
		for (int node = 0; node < numNodes; node++) {
			game.queryNode(node);
			Player player = propertyPlayer.getEnum();
			if (player == Player.ONE) {
				playerEvenOrStochastic.set(node);
			} else if (player == Player.TWO) {
				playerOddOrStochastic.set(node);				
			} else if (player == Player.STOCHASTIC) {
				playerEvenOrStochastic.set(node);
				playerOddOrStochastic.set(node);
			} else {
				assert false;
			}
		}
		sameColorShortCut = game.getOptions().getBoolean(OptionsCoalition.COALITION_SAME_COLOR_SHORTCUT);
		EMPTY_BIT_SET = UtilBitSet.newBitSetBounded(game.getNumNodes());
		SchedulerSimple strategies = null;
		if (computeStrategyP0 || computeStrategyP1) {
			strategies = new SchedulerSimpleArray(game);
		}
    	EMPTY_BIT_SET_PAIR = new QualitativeResult(UtilBitSet.newBitSetBounded(game.getNumNodes()), UtilBitSet.newBitSetBounded(game.getNumNodes()), strategies);
		BitSet p = UtilBitSet.newBitSetBounded(game.getNumNodes());
		p.set(0, game.getNumNodes());
		QualitativeResult result = zeroMcNaughton(p);
		getLog().send(MessagesCoalition.COALITION_SCHEWE_MCNAUGHTON_CALLS, getZeroMcNaughtonCalls());
		getLog().send(MessagesCoalition.COALITION_STOCHASTIC_MCNAUGHTON_DONE, watch.getTimeSeconds());
        return result;
	}
	
	QualitativeResult zeroMcNaughton(BitSet p) throws EPMCException {
        assert p != null;
        zeroMcNaughtonCalls++;
        if (p.isEmpty()) {
            return EMPTY_BIT_SET_PAIR;
        }
        NodeProperty labels = game.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        int minPriority = Integer.MAX_VALUE;
        boolean allEven = true;
        boolean allOdd = true;
        for (int node = p.nextSetBit(0); node >= 0; node = p.nextSetBit(node+1)) {
            game.queryNode(node);
            AutomatonParityLabel label = labels.getObject();
            int priority = label.getPriority();
            assert priority >= 0 : priority;
            minPriority = Math.min(minPriority, priority);
            allEven = allEven && priority % 2 == 0;
            allOdd = allOdd && priority % 2 == 1;
        }
        assert !allEven || !allOdd;
        if (sameColorShortCut && allEven) {
            return new QualitativeResult(p, EMPTY_BIT_SET, computeArbitraryStrategies(p));
        } else if (sameColorShortCut && allOdd) {
            return new QualitativeResult(EMPTY_BIT_SET, p, computeArbitraryStrategies(p));
        }
        return zeroMcNaughtonIterate(p, minPriority);
    }

    /**
     * Performs the main part of the stochastic McNaughton algorithm.
     * This method will be used after the minimal priority of a subgame has
     * been computed and the short cut computation cannot or should not be used.
     * Note that the algorithm subsumes contains only one main loop, which
     * subsumes the two loops of lines 5 (c) and 7 of the algorithm description
     * in our paper.
     * The nodes parameter must not be {@code null}. The minimal must be equal
     * to the minimal priority of the subgame and in particular must be
     * nonnegative.
     * 
     * @param p node space of subgame to consider
     * @param minPriority minimal priority of the subgame
     * @return winning regions of the players and requested strategies
     * @throws EPMCException thrown in case of problems
     */
	private QualitativeResult zeroMcNaughtonIterate(BitSet p, int minPriority) throws EPMCException {
		assert p != null;
		assert minPriority >= 0;
        NodeProperty labels = game.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        BitSet mapsToMinPriority = UtilBitSet.newBitSetBounded(game.getNumNodes());
        for (int node = p.nextSetBit(0); node >= 0; node = p.nextSetBit(node+1)) {
            game.queryNode(node);
            AutomatonParityLabel label = labels.getObject();
            mapsToMinPriority.set(node, label.getPriority() == minPriority);
        }
        assert minPriority >= 0 : minPriority;

        BitSet pPrimed = UtilBitSet.newBitSetBounded(game.getNumNodes());
        BitSet wOther = UtilBitSet.newBitSetBounded(game.getNumNodes());
		SchedulerSimple strategies = null;
		if (computeStrategyP0 || computeStrategyP1) {
			strategies = new SchedulerSimpleArray(game);
		}
        do {
        	pPrimed.clear();
        	pPrimed.or(p);
        	BitSet satr = satr(mapsToMinPriority, p, minPriority % 2 == 1);
        	pPrimed.andNot(satr);
        	QualitativeResult wPrimed = zeroMcNaughton(pPrimed);
        	BitSet wPrimedThis = minPriority % 2 == 0 ? wPrimed.getSet0() : wPrimed.getSet1();
        	BitSet wPrimedOther = minPriority % 2 == 1 ? wPrimed.getSet0() : wPrimed.getSet1();
        	if (wPrimedOther.isEmpty()) {
        		BitSet wThis = p.clone();
        		wThis.andNot(wOther);
        		if (computeStrategyP0 || computeStrategyP1) {
        			SchedulerSimple innerStrategies = wPrimed.getStrategies();
        			for (int node = wPrimedThis.nextSetBit(0); node >= 0; node = wPrimedThis.nextSetBit(node + 1)) {
        				game.queryNode(node);
        				Player player = this.propertyPlayer.getEnum();
        				if (computeStrategyP0 && player == Player.ONE
        						|| computeStrategyP1 && player == Player.TWO) {
        					int decision = innerStrategies.get(node);
        					assert decision != Scheduler.UNSET;
        					assert strategies.get(node) == Scheduler.UNSET;
        					strategies.set(node, decision);
        				}
        			}
        			for (int node = mapsToMinPriority.nextSetBit(0); node >= 0; node = mapsToMinPriority.nextSetBit(node + 1)) {
        				if (!p.get(node)) {
        					continue;
        				}
        				game.queryNode(node);
        				Player player = this.propertyPlayer.getEnum();
        				if (!(computeStrategyP0 && player == Player.ONE
        						|| computeStrategyP1 && player == Player.TWO)) {
        					continue;
        				}
        				boolean found = false;
            			for (int succ = 0; succ < game.getNumSuccessors(); succ++) {
            				if (p.get(game.getSuccessorNode(succ))) {
            					assert strategies.get(node) == Scheduler.UNSET;
                				strategies.set(node, succ);
            					found = true;
            					break;
            				}
            			}
            			assert found;
        			}
        			BitSet player = minPriority % 2 == 0 ? playerEvenOrStochastic : playerOddOrStochastic;
        			computeStrategy(strategies, game, mapsToMinPriority, satr, player);
        			for (int node = wThis.nextSetBit(0); node >= 0; node = wThis.nextSetBit(node + 1)) {
        				game.queryNode(node);
        				Player pl = this.propertyPlayer.getEnum();
        				assert !(computeStrategyP0 && pl == Player.ONE) || strategies.get(node) != Scheduler.UNSET;
        				assert !(computeStrategyP1 && pl == Player.TWO) || strategies.get(node) != Scheduler.UNSET;
        			}
        			for (int node = wOther.nextSetBit(0); node >= 0; node = wOther.nextSetBit(node + 1)) {
        				game.queryNode(node);
        				Player pl = this.propertyPlayer.getEnum();
        				assert !(computeStrategyP0 && pl == Player.ONE) || strategies.get(node) != Scheduler.UNSET;
        				assert !(computeStrategyP1 && pl == Player.TWO) || strategies.get(node) != Scheduler.UNSET;
            		}
        		}
    			return new QualitativeResult(minPriority % 2 == 0 ? wThis : wOther,
    					minPriority % 2 == 0 ? wOther : wThis, strategies);
        	}
        	BitSet atrOther = null;
        	if (minPriority % 2 == 0) {
        		atrOther = strictOdd ? watr1(wPrimedOther, p) : satr1(wPrimedOther, p);
        	} else {
        		atrOther = strictEven ? watr0(wPrimedOther, p) : satr0(wPrimedOther, p);
        	}
        	wOther.or(atrOther);
        	p.andNot(atrOther);
    		if (computeStrategyP0 || computeStrategyP1) {
    			SchedulerSimple innerStrategies = wPrimed.getStrategies();
    			for (int node = wPrimedOther.nextSetBit(0); node >= 0; node = wPrimedOther.nextSetBit(node + 1)) {
    				game.queryNode(node);
    				Player pl = this.propertyPlayer.getEnum();
    				if (computeStrategyP0 && pl == Player.ONE
    						|| computeStrategyP1 && pl == Player.TWO) {
    					int decision = innerStrategies.get(node);
    					assert decision != Scheduler.UNSET : node;
    					assert strategies.get(node) == Scheduler.UNSET;
    					strategies.set(node, decision);
    				}
    			}
    			BitSet player = minPriority % 2 == 1 ? playerEvenOrStochastic : playerOddOrStochastic;
    			computeStrategy(strategies, game, wPrimedOther, atrOther, player);
    		}
        } while (true);
	}

    /**
     * Compute an arbitrary strategy for a given set of nodes.
     * This strategy is meant for handling the cases where we are considering a
     * subgame the nodes of which are either all even or all odd coloured.
     * Strategies will only be computed if strategy computation is at activated
     * for at least one player. Only nodes under the control of players for
     * which strategy computation is enabled will be assigned a decision.
     * 
     * @param p node set of subgame
     * @return arbitrary valid strategy in p
     * @throws EPMCException thrown in case of problems
     */
    private SchedulerSimple computeArbitraryStrategies(BitSet p) throws EPMCException {
    	assert p != null;
    	if (!computeStrategyP0 && !computeStrategyP1) {
    		return null;
    	}
    	SchedulerSimple result = new SchedulerSimpleArray(game);
    	for (int node = p.nextSetBit(0); node >= 0; node = p.nextSetBit(node + 1)) {
    		if (computeStrategyP0 && playerEvenOrStochastic.get(node)
    				|| computeStrategyP1 && playerOddOrStochastic.get(node)) {
    			game.queryNode(node);
				Player player = this.propertyPlayer.getEnum();
				if (!(computeStrategyP0 && player == Player.ONE
						|| computeStrategyP1 && player == Player.TWO)) {
					continue;
				}
    			boolean found = false;
    			for (int succ = 0; succ < game.getNumSuccessors(); succ++) {
    				if (p.get(game.getSuccessorNode(succ))) {
    	    			result.set(node, succ);
    					found = true;
    					break;
    				}
    			}
    			assert found;
    		}
    	}
		return result;
	}

	/**
	 * Compute strong attractor of a given target set.
	 * TODO continue description
	 * 
	 * @param target target set
	 * @param nodes nodes to restrict area to
	 * @param odd whether to compute attractor set for player odd (1)
	 * @return
	 * @throws EPMCException
	 */
	private BitSet satr(BitSet target, BitSet nodes, boolean odd)
            throws EPMCException {
        return odd ? satr1(target, nodes) : satr0(target, nodes);
    }
    
    private BitSet satr0(BitSet target, BitSet nodes) throws EPMCException {
        return attract(game, target, nodes, playerEvenOrStochastic);
    }

    private BitSet satr1(BitSet target, BitSet nodes) throws EPMCException {
        return attract(game, target, nodes, playerOddOrStochastic);
    }

    private BitSet watr(BitSet target, BitSet nodes, boolean odd)
            throws EPMCException {
        BitSet satrSame = satr(target, nodes, odd);
        BitSet nodesMSatrSame = UtilBitSet.newBitSetBounded(game.getNumNodes());
        BitSet nodesMTarget = UtilBitSet.newBitSetBounded(game.getNumNodes());
        BitSet satrOther = UtilBitSet.newBitSetBounded(game.getNumNodes());
        BitSet nodesMSatrOther = UtilBitSet.newBitSetBounded(game.getNumNodes());
        nodes = nodes.clone();
        while (!nodes.equals(satrSame)) {
            nodesMSatrSame.clear();
            nodesMSatrSame.or(nodes);
            nodesMSatrSame.andNot(satrSame);
            nodesMTarget.clear();
            nodesMTarget.or(nodes);
            nodesMSatrSame.andNot(target);
            satrOther = satr(nodesMSatrSame, nodesMTarget, !odd);
            nodesMSatrOther.clear();
            nodesMSatrOther.or(nodes);
            nodesMSatrOther.andNot(satrOther);
            nodes.clear();
            nodes.or(nodesMSatrOther);
            satrSame = satr(target, nodes, odd);            
        }
        return satrSame;
    }
    
    private BitSet watr0(BitSet target, BitSet nodes) throws EPMCException {
        return watr(target, nodes, false);
    }

    private BitSet watr1(BitSet target, BitSet nodes) throws EPMCException {
        return watr(target, nodes, true);
    }
    
    int getZeroMcNaughtonCalls() {
		return zeroMcNaughtonCalls;
	}

    /**
     * Compute a strategy for a weak or strong attractor.
     * 
     * @param strategies strategy to be updated
     * @param graph graph for which to compute the strategy
     * @param target 
     * @param nodes nodes reaching the target
     * @param exists
     * @throws EPMCException
     */
    private void computeStrategy(SchedulerSimple strategies, GraphExplicit graph,
            BitSet target, BitSet nodes, BitSet exists)
                    throws EPMCException {
    	assert strategies != null;
        assert graph != null;
        assert target != null;
        assert nodes != null;
        assert exists != null;
        
        graph.computePredecessors();
        int[] remaining = new int[graph.getNumNodes()];
        for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node+1)) {
            if (exists.get(node)) {
                remaining[node] = 1;
            } else {
                graph.queryNode(node);
                for (int succNr = 0; succNr < graph.getNumSuccessors(); succNr++) {
                    int succState = graph.getSuccessorNode(succNr);
                    if (nodes.get(succState)) {
                        remaining[node]++;
                    }
                }
            }
        }
        for (int node = target.nextSetBit(0); node >= 0; node = target.nextSetBit(node+1)) {
            remaining[node] = 0;
        }
        
        BitSet newNodes = target.clone();
        BitSet previousNodes = UtilBitSet.newBitSetBounded(game.getNumNodes());
        do {
            BitSet swap = previousNodes;
            previousNodes = newNodes;
            newNodes = swap;
            newNodes.clear();
            for (int node = previousNodes.nextSetBit(0); node >= 0;
                    node = previousNodes.nextSetBit(node+1)) {
                graph.queryNode(node);
                for (int predNr = 0; predNr < graph.getNumPredecessors(); predNr++) {
                    int pred = graph.getPredecessorNode(predNr);
                    /* note that we don't have to check whether predecessor in
                     * nodes set, because in this case remaining[pred] will be
                     * 0 such that it will not be included in contained. */
                    if (remaining[pred] != 0) {
                        remaining[pred]--;
                        if (remaining[pred] == 0) {
                            graph.queryNode(pred);
                            Player player = this.propertyPlayer.getEnum();
                            if (computeStrategyP0 && player == Player.ONE
                            		|| computeStrategyP1 && player == Player.TWO) {
            					assert strategies.get(node) == Scheduler.UNSET;
                            	strategies.set(pred, graph.getSuccessorNumber(node));
                            }
                            graph.queryNode(node);
                            newNodes.set(pred);
                        }
                    }
                }
            }
        } while (!newNodes.isEmpty());
        /* make sure that we indeed computed the strategy correctly */
        for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node+1)) {
        	game.queryNode(node);
        	Player player = propertyPlayer.getEnum();
        	assert !(computeStrategyP0 && player == Player.ONE) || strategies.get(node) != Scheduler.UNSET || target.get(node) : node;
        	assert !(computeStrategyP1 && player == Player.TWO) || strategies.get(node) != Scheduler.UNSET || target.get(node) : node;
        }
    }

    private BitSet attract(GraphExplicit graph,
            BitSet target, BitSet nodes, BitSet exists)
                    throws EPMCException {
        assert graph != null;
        assert target != null;
        assert nodes != null;
        assert exists != null;
        
        graph.computePredecessors();
        int[] remaining = new int[graph.getNumNodes()];
        for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node+1)) {
            if (exists.get(node)) {
                remaining[node] = 1;
            } else {
                graph.queryNode(node);
                for (int succNr = 0; succNr < graph.getNumSuccessors(); succNr++) {
                    int succState = graph.getSuccessorNode(succNr);
                    if (nodes.get(succState)) {
                        remaining[node]++;
                    }
                }
            }
        }
        BitSet contained = UtilBitSet.newBitSetBounded(game.getNumNodes());
        for (int node = target.nextSetBit(0); node >= 0; node = target.nextSetBit(node+1)) {
            contained.set(node);
            remaining[node] = 0;
        }
        
        BitSet newNodes = target.clone();
        BitSet previousNodes = UtilBitSet.newBitSetBounded(game.getNumNodes());
        do {
            BitSet swap = previousNodes;
            previousNodes = newNodes;
            newNodes = swap;
            newNodes.clear();
            for (int node = previousNodes.nextSetBit(0); node >= 0;
                    node = previousNodes.nextSetBit(node+1)) {
                graph.queryNode(node);
                for (int predNr = 0; predNr < graph.getNumPredecessors(); predNr++) {
                    int pred = graph.getPredecessorNode(predNr);
                    /* note that we don't have to check whether predecessor in
                     * nodes set, because in this case remaining[pred] will be
                     * 0 such that it will not be included in contained. */
                    if (remaining[pred] != 0) {
                        remaining[pred]--;
                        if (remaining[pred] == 0) {
                            contained.set(pred);
                            newNodes.set(pred);
                        }
                    }
                }
            }
        } while (!newNodes.isEmpty());
        return contained;
    }

    /**
     * Get log to send messages.
     * 
     * @return log to send messages
     */
    private Log getLog() {
    	return game.getOptions().get(OptionsMessages.LOG);
    }

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
}
