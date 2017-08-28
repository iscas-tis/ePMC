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
import epmc.coalition.messages.MessagesCoalition;
import epmc.coalition.options.OptionsCoalition;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.Scheduler;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.SchedulerSimple;
import epmc.graph.explicit.SchedulerSimpleArray;
import epmc.graph.explicit.SchedulerSimpleSettable;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;

public final class SolverNonStochasticMcNaughton implements SolverNonStochastic {
    /** Identifier for this solver. */
    public final static String IDENTIFIER = "mcnaughton";

    /** Game to be solved. */
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
    /** Number of recursive calls to the algorithm. */
    private int mcNaughtonCalls;
    /** Nodes which belong to the even or the stochastic player. */
    private BitSet playerEven;
    /** Nodes which belong to the odd or the stochastic player. */
    private BitSet playerOdd;

    @Override
    public void setGame(GraphExplicit game) {
        assert game != null;
        this.game = game;
    }

    @Override
    public void setComputeStrategies(boolean playerEven, boolean playerOdd) {
        computeStrategyP0 = playerEven;
        computeStrategyP1 = playerOdd;
    }

    @Override
    public QualitativeResult solve() {
        int numNodes = game.getNumNodes();
        StopWatch watch = new StopWatch(true);
        getLog().send(MessagesCoalition.COALITION_NONSTOCHASTIC_MCNAUGHTON_START);
        playerEven = UtilBitSet.newBitSetBounded(numNodes);
        playerOdd = UtilBitSet.newBitSetBounded(numNodes);
        NodeProperty propertyPlayer = game.getNodeProperty(CommonProperties.PLAYER);
        for (int node = 0; node < numNodes; node++) {
            Player player = propertyPlayer.getEnum(node);
            if (player == Player.ONE) {
                playerEven.set(node);
            } else if (player == Player.TWO) {
                playerOdd.set(node);				
            } else {
                assert false;
            }
        }
        sameColorShortCut = Options.get().getBoolean(OptionsCoalition.COALITION_SAME_COLOR_SHORTCUT);
        EMPTY_BIT_SET = UtilBitSet.newBitSetBounded(game.getNumNodes());
        SchedulerSimple strategies = null;
        if (computeStrategyP0 || computeStrategyP1) {
            strategies = new SchedulerSimpleArray(game);
        }
        EMPTY_BIT_SET_PAIR = new QualitativeResult(UtilBitSet.newBitSetBounded(game.getNumNodes()), UtilBitSet.newBitSetBounded(game.getNumNodes()), strategies);
        BitSet p = UtilBitSet.newBitSetBounded(game.getNumNodes());
        p.set(0, game.getNumNodes());
        QualitativeResult result = mcNaughton(p);
        getLog().send(MessagesCoalition.COALITION_SCHEWE_MCNAUGHTON_CALLS, getMcNaughtonCalls());
        getLog().send(MessagesCoalition.COALITION_NONSTOCHASTIC_MCNAUGHTON_DONE, watch.getTimeSeconds());
        return result;
    }

    QualitativeResult mcNaughton(BitSet p) {
        assert p != null;
        mcNaughtonCalls++;
        if (p.isEmpty()) {
            return EMPTY_BIT_SET_PAIR;
        }
        NodeProperty labels = game.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        int minPriority = Integer.MAX_VALUE;
        boolean allEven = true;
        boolean allOdd = true;
        for (int node = p.nextSetBit(0); node >= 0; node = p.nextSetBit(node+1)) {
            AutomatonParityLabel label = labels.getObject(node);
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
        return mcNaughtonIterate(p, minPriority);
    }

    private QualitativeResult mcNaughtonIterate(BitSet p, int minPriority) {
        assert p != null;
        assert minPriority >= 0;
        NodeProperty labels = game.getNodeProperty(CommonProperties.AUTOMATON_LABEL);
        BitSet mapsToMinPriority = UtilBitSet.newBitSetBounded(game.getNumNodes());
        for (int node = p.nextSetBit(0); node >= 0; node = p.nextSetBit(node+1)) {
            AutomatonParityLabel label = labels.getObject(node);
            mapsToMinPriority.set(node, label.getPriority() == minPriority);
        }
        assert minPriority >= 0 : minPriority;

        BitSet pPrimed = UtilBitSet.newBitSetBounded(game.getNumNodes());
        BitSet wOther = UtilBitSet.newBitSetBounded(game.getNumNodes());
        SchedulerSimpleSettable strategies = null;
        if (computeStrategyP0 || computeStrategyP1) {
            strategies = new SchedulerSimpleArray(game);
        }
        do {
            pPrimed.clear();
            pPrimed.or(p);
            BitSet satr = satr(mapsToMinPriority, p, minPriority % 2 == 1);
            pPrimed.andNot(satr);
            QualitativeResult wPrimed = mcNaughton(pPrimed);
            BitSet wPrimedThis = minPriority % 2 == 0 ? wPrimed.getSet0() : wPrimed.getSet1();
            BitSet wPrimedOther = minPriority % 2 == 1 ? wPrimed.getSet0() : wPrimed.getSet1();
            if (wPrimedOther.isEmpty()) {
                BitSet wThis = p.clone();
                wThis.andNot(wOther);
                if (computeStrategyP0 || computeStrategyP1) {
                    SchedulerSimple innerStrategies = wPrimed.getStrategies();
                    for (int node = wPrimedThis.nextSetBit(0); node >= 0; node = wPrimedThis.nextSetBit(node + 1)) {
                        if (computeStrategyP0 && playerEven.get(node)
                                || computeStrategyP1 && playerOdd.get(node)) {
                            int decision = innerStrategies.getDecision(node);
                            assert decision != Scheduler.UNSET;
                            strategies.set(node, decision);
                        }
                    }
                    for (int node = mapsToMinPriority.nextSetBit(0); node >= 0; node = mapsToMinPriority.nextSetBit(node + 1)) {
                        if (!p.get(node)) {
                            continue;
                        }
                        if (!(computeStrategyP0 && playerEven.get(node)
                                || computeStrategyP1 && playerOdd.get(node))) {
                            continue;
                        }
                        boolean found = false;
                        for (int succ = 0; succ < game.getNumSuccessors(node); succ++) {
                            if (p.get(game.getSuccessorNode(node, succ))) {
                                strategies.set(node, succ);
                                found = true;
                                break;
                            }
                        }
                        assert found;
                    }
                    BitSet player = minPriority % 2 == 0 ? playerEven : playerOdd;
                    computeStrategy(strategies, game, mapsToMinPriority, satr, player);
                    for (int node = wThis.nextSetBit(0); node >= 0; node = wThis.nextSetBit(node + 1)) {
                        assert !(computeStrategyP0 && playerEven.get(node)) || strategies.getDecision(node) != Scheduler.UNSET;
                        assert !(computeStrategyP1 && playerOdd.get(node)) || strategies.getDecision(node) != Scheduler.UNSET;
                    }
                    for (int node = wOther.nextSetBit(0); node >= 0; node = wOther.nextSetBit(node + 1)) {
                        assert !(computeStrategyP0 && playerEven.get(node)) || strategies.getDecision(node) != Scheduler.UNSET;
                        assert !(computeStrategyP1 && playerOdd.get(node)) || strategies.getDecision(node) != Scheduler.UNSET;
                    }
                }
                return new QualitativeResult(minPriority % 2 == 0 ? wThis : wOther,
                        minPriority % 2 == 0 ? wOther : wThis, strategies);
            }
            BitSet atrOther = null;
            if (minPriority % 2 == 0) {
                atrOther = satr1(wPrimedOther, p);
            } else {
                atrOther = satr0(wPrimedOther, p);
            }
            wOther.or(atrOther);
            p.andNot(atrOther);
            if (computeStrategyP0 || computeStrategyP1) {
                SchedulerSimple innerStrategies = wPrimed.getStrategies();
                for (int node = wPrimedOther.nextSetBit(0); node >= 0; node = wPrimedOther.nextSetBit(node + 1)) {
                    if (computeStrategyP0 && playerEven.get(node)
                            || computeStrategyP1 && playerOdd.get(node)) {
                        int decision = innerStrategies.getDecision(node);
                        assert decision != Scheduler.UNSET : node;
                        strategies.set(node, decision);
                    }
                }
                BitSet player = minPriority % 2 == 1 ? playerEven : playerOdd;
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
     */
    private SchedulerSimple computeArbitraryStrategies(BitSet p) {
        assert p != null;
        if (!computeStrategyP0 && !computeStrategyP1) {
            return null;
        }
        SchedulerSimpleSettable result = new SchedulerSimpleArray(game);
        for (int node = p.nextSetBit(0); node >= 0; node = p.nextSetBit(node + 1)) {
            if (computeStrategyP0 && playerEven.get(node)
                    || computeStrategyP1 && playerOdd.get(node)) {
                boolean found = false;
                for (int succ = 0; succ < game.getNumSuccessors(node); succ++) {
                    if (p.get(game.getSuccessorNode(node, succ))) {
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
     */
    private BitSet satr(BitSet target, BitSet nodes, boolean odd)
    {
        return odd ? satr1(target, nodes) : satr0(target, nodes);
    }

    private BitSet satr0(BitSet target, BitSet nodes) {
        return attract(game, target, nodes, playerEven);
    }

    private BitSet satr1(BitSet target, BitSet nodes) {
        return attract(game, target, nodes, playerOdd);
    }

    int getMcNaughtonCalls() {
        return mcNaughtonCalls;
    }

    /**
     * Compute a strategy for a weak or strong attractor.
     * 
     * 
     * @param strategy
     * @param graph
     * @param target
     * @param nodes
     * @param exists
     */
    private void computeStrategy(SchedulerSimpleSettable strategy, GraphExplicit graph,
            BitSet target, BitSet nodes, BitSet exists)
    {
        assert strategy != null;
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
                for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                    int succState = graph.getSuccessorNode(node, succNr);
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
                for (int predNr = 0; predNr < graph.getProperties().getNumPredecessors(node); predNr++) {
                    int pred = graph.getProperties().getPredecessorNode(node, predNr);
                    /* note that we don't have to check whether predecessor in
                     * nodes set, because in this case remaining[pred] will be
                     * 0 such that it will not be included in contained. */
                    if (remaining[pred] != 0) {
                        remaining[pred]--;
                        if (remaining[pred] == 0) {
                            if (computeStrategyP0 && playerEven.get(pred)
                                    || computeStrategyP1 && playerOdd.get(pred)) {
                                strategy.set(pred, graph.getSuccessorNumber(pred, node));
                            }
                            newNodes.set(pred);
                        }
                    }
                }
            }
        } while (!newNodes.isEmpty());
        /* make sure that we indeed computed the strategy correctly */
        for (int node = nodes.nextSetBit(0); node >= 0; node = nodes.nextSetBit(node+1)) {
            assert !(computeStrategyP0 && playerEven.get(node)) || (strategy.getDecision(node) != Scheduler.UNSET || target.get(node)) : node;
            assert !(computeStrategyP1 && playerOdd.get(node)) || (strategy.getDecision(node) != Scheduler.UNSET || target.get(node)) : node;
        }
    }

    private BitSet attract(GraphExplicit graph,
            BitSet target, BitSet nodes, BitSet exists)
    {
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
                for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                    int succState = graph.getSuccessorNode(node, succNr);
                    if (nodes.get(succState)) {
                        remaining[node]++;
                    }
                }
            }
        }
        BitSet contained = UtilBitSet.newBitSetBounded(game.getNumNodes());
        for (int node = target.nextSetBit(0); node >= 0; node = target.nextSetBit(node+1)) {
            /* note that target may be a superset of the nodes taken into account */
            if (nodes.get(node)) {
                contained.set(node);
            }
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
                for (int predNr = 0; predNr < graph.getProperties().getNumPredecessors(node); predNr++) {
                    int pred = graph.getProperties().getPredecessorNode(node, predNr);
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
        return Options.get().get(OptionsMessages.LOG);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
