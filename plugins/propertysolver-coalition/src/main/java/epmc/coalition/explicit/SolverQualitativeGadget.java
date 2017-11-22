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

import java.util.Arrays;

import epmc.automaton.AutomatonParityLabel;
import epmc.coalition.messages.MessagesCoalition;
import epmc.coalition.options.OptionsCoalition;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitSparse;
import epmc.graph.explicit.NodeProperty;
import epmc.graph.explicit.SchedulerSimple;
import epmc.graph.explicit.SchedulerSimpleArray;
import epmc.graph.explicit.SchedulerSimpleSettable;
import epmc.messages.OptionsMessages;
import epmc.modelchecker.Log;
import epmc.options.Options;
import epmc.options.UtilOptions;
import epmc.util.BitSet;
import epmc.util.StopWatch;
import epmc.util.UtilBitSet;

/**
 * Solve stochastic game by transforming it to a nonstochastic game.
 * 
 * @author Ernst Moritz Hahn
 */
public final class SolverQualitativeGadget implements SolverQualitative {
    public final static String IDENTIFIER = "gadget";

    /** Stochastic game to analyse. */
    private GraphExplicit stochasticGame;
    /** Array mapping nodes from stochastic to qualitative game. */
    private int[] stochasticToQualitative;
    /** Maximal priority in game. */
    private int maxPriority;	

    /** Qualitative game from gadget construction on {@link #stochasticGame}. */
    private GraphExplicit qualitativeGame;
    /** Whether even player must win with probability 1. */
    private boolean strictEven;
    /** Whether the option whether even player must win with probability 1 was set. */
    private boolean strictEvenSet;
    /** Whether player even strategy needs to be computed. */
    private boolean computeStrategyPlayerEven;
    /** Whether player odd strategy needs to be computed. */
    private boolean computeStrategyPlayerOdd;

    @Override
    public void setGame(GraphExplicit game) {
        assert game != null;
        this.stochasticGame = game;
    }

    @Override
    public void setStrictEven(boolean strictEven) {
        this.strictEven = strictEven;
        this.strictEvenSet = true;
    }

    @Override
    public void setComputeStrategies(boolean playerEven, boolean playerOdd) {
        this.computeStrategyPlayerEven = playerEven;
        this.computeStrategyPlayerOdd = playerOdd;
    }

    @Override
    public QualitativeResult solve() {
        assert stochasticGame != null;
        assert strictEvenSet;
        getLog().send(MessagesCoalition.COALITION_GADGET_START);
        StopWatch watch = new StopWatch(true);
        transform();
        SolverNonStochastic nonStochasticSolver = UtilOptions.getInstance(OptionsCoalition.COALITION_SOLVER_NON_STOCHASTIC);
        nonStochasticSolver.setGame(qualitativeGame);
        nonStochasticSolver.setComputeStrategies(computeStrategyPlayerEven, computeStrategyPlayerOdd);
        QualitativeResult qualitativeResult = nonStochasticSolver.solve();
        BitSet evenWins = UtilBitSet.newBitSetBounded(stochasticGame.getNumNodes());
        BitSet oddWins = UtilBitSet.newBitSetBounded(stochasticGame.getNumNodes());
        SchedulerSimpleSettable strategies = qualitativeResult.getStrategies() == null ? null
                : new SchedulerSimpleArray(stochasticGame);
        NodeProperty propertyPlayer = stochasticGame.getNodeProperty(CommonProperties.PLAYER);
        for (int node = 0; node < stochasticToQualitative.length; node++) {
            int qualiNode = stochasticToQualitative[node];
            if (qualitativeResult.getSet0().get(qualiNode)) {
                evenWins.set(node);
            } else if (qualitativeResult.getSet1().get(qualiNode)) {
                oddWins.set(node);
            }
            SchedulerSimple qualitativeStrategies = qualitativeResult.getStrategies();
            if (qualitativeStrategies != null) {
                Player player = propertyPlayer.getEnum(node);
                if (computeStrategyPlayerEven && player == Player.ONE
                        || computeStrategyPlayerOdd && player == Player.TWO) {
                    strategies.set(node, qualitativeStrategies.getDecision(qualiNode));
                }
            }
        }
        getLog().send(MessagesCoalition.COALITION_GADGET_DONE, watch.getTimeSeconds());
        return new QualitativeResult(evenWins, oddWins, strategies);
    }

    /**
     * Transform stochastic game to qualitative game.
     * This method uses the &quot;gadget&quot; construction from the paper
     * &quot;Simple Stochastic Parity Games&quot;,
     * &quot;Quantitative Stochastic Parity Games&quot;.
     * 
     */
    private void transform() {
        StopWatch watch = new StopWatch(true);
        getLog().send(MessagesCoalition.COALITION_GADGET_TRANSFORM_START);
        NodeProperty stochasticLabels = stochasticGame.getNodeProperty(CommonProperties.AUTOMATON_LABEL);

        /* construct mapping from old to new nodes, compute number of states and
         * total number of leaving transitions of new game, count number of
         * priorities */
        stochasticToQualitative = new int[stochasticGame.getNumNodes()];
        Arrays.fill(stochasticToQualitative, -1);
        int nextMappedNodeQualitative = 0;
        int numTotalOut = 0;
        maxPriority = -1;
        boolean hasInfPrio = false;
        int numStochasticGameNodes = stochasticGame.getNumNodes();
        for (int node = 0; node < numStochasticGameNodes; node++) {
            AutomatonParityLabel label = stochasticLabels.getObject(node);
            int priority = label.getPriority();
            if (priority == Integer.MAX_VALUE) {
                hasInfPrio = true;
            } else {
                maxPriority = Math.max(maxPriority, priority);
            }
        }
        if (hasInfPrio) {
            maxPriority++;
        }
        NodeProperty propertyPlayer = stochasticGame.getNodeProperty(CommonProperties.PLAYER);
        for (int node = 0; node < numStochasticGameNodes; node++) {
            Player player = propertyPlayer.getEnum(node);
            AutomatonParityLabel label = stochasticLabels.getObject(node);
            int priority = label.getPriority();
            if (priority == Integer.MAX_VALUE) {
                priority = maxPriority;
            }
            stochasticToQualitative[node] = nextMappedNodeQualitative;
            if (player == Player.ONE || player == Player.TWO) {
                /* nonstochastic nodes will just be "copied", thus number of
                 * states required is 1, and number of transitions required is
                 * as in the stochastic game */
                nextMappedNodeQualitative++;
                numTotalOut += stochasticGame.getNumSuccessors(node);
            } else if (player == Player.STOCHASTIC) {
                /* gadget construction - compute number of total nodes for
                 * replacement and number of new transitions */
                /* one for new top node */
                int numFirstLayer = 1;
                nextMappedNodeQualitative += numFirstLayer;
                /* for second layer nodes */
                int numSecondLayer = (priority + 1) / 2 + 1;
                nextMappedNodeQualitative += numSecondLayer;
                numTotalOut += numSecondLayer;
                /* for third layer nodes */
                int numThirdLayer = priority + 1;
                nextMappedNodeQualitative += numThirdLayer;
                numTotalOut += numThirdLayer;
                /* for transitions leaving the gadget */
                numTotalOut += stochasticGame.getNumSuccessors(node) * numThirdLayer;
            } else {
                assert false;
            }
        }
        /* prebuild objects for priority labels - 0 to highest priority */
        SettableParityLabel[] priorities = new SettableParityLabel[maxPriority + 1];
        for (int priority = 0; priority <= maxPriority; priority++) {
            priorities[priority] = new SettableParityLabel(priority);
        }
        /* construct new game */
        qualitativeGame = new GraphExplicitSparse(nextMappedNodeQualitative, numTotalOut);
        NodeProperty qualitativeLabels = qualitativeGame.addSettableNodeProperty(CommonProperties.AUTOMATON_LABEL, stochasticLabels.getType());
        NodeProperty qualitativePlayer = qualitativeGame.addSettableNodeProperty(CommonProperties.PLAYER, propertyPlayer.getType());
        for (int stochasticNode = 0; stochasticNode < numStochasticGameNodes; stochasticNode++) {
            Player player = propertyPlayer.getEnum(stochasticNode);
            AutomatonParityLabel stochasticLabel = stochasticLabels.getObject(stochasticNode);
            int nodePriority = stochasticLabel.getPriority();
            if (nodePriority == Integer.MAX_VALUE) {
                nodePriority = maxPriority;
            }
            int qualitativeNode = stochasticToQualitative[stochasticNode];
            int numSuccessors = stochasticGame.getNumSuccessors(stochasticNode);
            qualitativeLabels.set(qualitativeNode, priorities[nodePriority]);
            if (player == Player.ONE || player == Player.TWO) {
                /* simple translation for nonstochatic nodes */
                qualitativeGame.prepareNode(qualitativeNode, numSuccessors);
                qualitativePlayer.set(qualitativeNode, player);
                for (int succNr = 0; succNr < numSuccessors; succNr++) {
                    int stochasticSuccState = stochasticGame.getSuccessorNode(stochasticNode, succNr);
                    int qualitativeSuccState = stochasticToQualitative[stochasticSuccState];
                    qualitativeGame.setSuccessorNode(qualitativeNode, succNr, qualitativeSuccState);
                }
            } else if (player == Player.STOCHASTIC) {
                /* gadget construction for stochastic nodes */
                if (strictEven) {
                    qualitativePlayer.set(qualitativeNode, Player.TWO);
                } else {
                    qualitativePlayer.set(qualitativeNode, Player.ONE);
                }
                /* transitions from top node to second layer */
                int numFirstLayer = 1;
                int secondLayerNode = qualitativeNode + numFirstLayer;
                int numSecondLayer = (nodePriority + 1) / 2 + 1;
                qualitativeGame.prepareNode(qualitativeNode, numSecondLayer);
                for (int succNr = 0; succNr < numSecondLayer; succNr++) {
                    qualitativeGame.setSuccessorNode(qualitativeNode, succNr, secondLayerNode);
                    secondLayerNode++;
                }
                /* transitions from second layer to third layer */
                secondLayerNode = qualitativeNode + numFirstLayer;
                int thirdLayerNode = qualitativeNode + numFirstLayer + numSecondLayer;
                for (int priority = 0; priority <= nodePriority + 1; priority += 2) {
                    if (strictEven) {
                        qualitativePlayer.set(secondLayerNode, Player.ONE);
                    } else {
                        qualitativePlayer.set(secondLayerNode, Player.TWO);
                    }
                    qualitativeLabels.set(secondLayerNode, priorities[nodePriority]);
                    if (priority > 0 && priority <= nodePriority) {
                        qualitativeGame.prepareNode(secondLayerNode, 2);
                    } else {
                        qualitativeGame.prepareNode(secondLayerNode, 1);
                    }
                    int succNr = 0;
                    if (priority > 0) {
                        qualitativeGame.setSuccessorNode(secondLayerNode, succNr, thirdLayerNode);
                        succNr++;
                        thirdLayerNode++;
                    }
                    if (priority <= nodePriority) {
                        qualitativeGame.setSuccessorNode(secondLayerNode, succNr, thirdLayerNode);
                        succNr++;
                        thirdLayerNode++;
                    }
                    secondLayerNode++;
                }
                /* transitions from third layer to other parts */
                thirdLayerNode = qualitativeNode + numFirstLayer + numSecondLayer;
                for (int priority = 0; priority <= nodePriority; priority++) {
                    qualitativeLabels.set(thirdLayerNode, priorities[priority]);
                    qualitativeGame.prepareNode(thirdLayerNode, numSuccessors);
                    if (strictEven ^ priority % 2 == 0) {
                        qualitativePlayer.set(thirdLayerNode, Player.ONE);
                    } else {
                        qualitativePlayer.set(thirdLayerNode, Player.TWO);
                    }
                    for (int succNr = 0; succNr < numSuccessors; succNr++) {
                        int quantativeSuccState = stochasticGame.getSuccessorNode(stochasticNode, succNr);
                        int qualitativeSuccState = stochasticToQualitative[quantativeSuccState];
                        qualitativeGame.setSuccessorNode(thirdLayerNode, succNr, qualitativeSuccState);
                    }
                    thirdLayerNode++;
                }
            } else {
                assert false;
            }
        }
        getLog().send(MessagesCoalition.COALITION_GADGET_TRANSFORM_DONE,
                qualitativeGame.getNumNodes(), watch.getTimeSeconds());
    }

    private Log getLog() {
        return Options.get().get(OptionsMessages.LOG);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

}
