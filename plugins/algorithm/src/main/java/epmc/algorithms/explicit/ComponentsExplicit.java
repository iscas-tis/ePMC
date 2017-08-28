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

package epmc.algorithms.explicit;

import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;

public final class ComponentsExplicit {
    private final static String SPACE = " ";

    public EndComponents endComponents(GraphExplicit graph,
            BitSet existingNodes, boolean mecsOnly) {
        return new EndComponentsImpl(graph, existingNodes, mecsOnly);
    }

    public void removeLeavingAttr(GraphExplicit graph, BitSet set) {
        assert graph != null;
        assert set != null;
        assert set.length() <= graph.getNumNodes();
        graph.computePredecessors();
        int[] remaining = new int[graph.getNumNodes()];
        int[] leaving = new int[graph.getNumNodes()];
        int leavingIndex = 0;
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        for (int node = set.nextSetBit(0); node >= 0; node = set.nextSetBit(node+1)) {
            remaining[node] = 0;
            Player player = playerProp.getEnum(node);
            if (player == Player.ONE) {
                for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                    int succNode = graph.getSuccessorNode(node, succNr);
                    if (set.get(succNode)) {
                        remaining[node]++;
                    }
                }
                if (remaining[node] == 0) {
                    leaving[leavingIndex] = node;
                    leavingIndex++;
                }
            } else if (player == Player.STOCHASTIC) {
                boolean outside = false;
                for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                    int succNode = graph.getSuccessorNode(node, succNr);
                    if (!set.get(succNode)) {
                        outside = false;
                        break;
                    }
                }
                if (outside) {
                    leaving[leavingIndex] = node;
                    leavingIndex++;
                } else {
                    remaining[node] = 1;
                }
            }
        }

        while (leavingIndex != 0) {
            leavingIndex--;
            int node = leaving[leavingIndex];
            for (int predNr = 0; predNr < graph.getProperties().getNumPredecessors(node); predNr++) {
                int pred = graph.getProperties().getPredecessorNode(node, predNr);
                if (set.get(pred)) {
                    remaining[pred]--;
                    if (remaining[pred] == 0) {
                        set.set(pred, false);
                        leaving[leavingIndex] = pred;
                        leavingIndex++;
                    } else if (remaining[pred] < 0) {
                        assert false;
                    }
                }
            }
        }
    }

    public BitSet removeLeavingAttr(GraphExplicit graph,
            int[] leaving, int leavingSize) {
        int[] remaining = new int[graph.getNumNodes()];
        BitSet existingStates = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
        existingStates.flip(0, graph.getNumNodes());
        BitSet ecc = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
        ecc.flip(0, graph.getNumNodes());
        for (int node : leaving) {
            existingStates.set(node, false);
        }
        int[] scc = new int[graph.getNumNodes()];
        for (int state = 0; state < scc.length; state++) {
            scc[state] = state;
        }
        final int sccBegin = 0;
        final int sccEnd = scc.length;
        removeLeavingAttr(graph, existingStates, remaining, existingStates,
                leaving, leavingSize, scc, sccBegin, sccEnd);
        return existingStates;
    }

    private static void removeLeavingAttr(GraphExplicit graph,
            BitSet existingStates, int[] remaining,
            BitSet scc, int[] leaving, int leavingIndex,
            int[] sccStates, int sccBegin, int sccEnd) {
        graph.computePredecessors();
        NodeProperty player = graph.getNodeProperty(CommonProperties.PLAYER);
        for (int nodeNr = sccBegin; nodeNr < sccEnd; nodeNr++) {
            int node = sccStates[nodeNr];
            remaining[node] = 0;
            if (player.getEnum(node) == Player.ONE) {
                for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                    int succ = graph.getSuccessorNode(node, succNr);
                    if (scc.get(succ)) {
                        remaining[node]++;
                    }
                }
            } else if (player.getEnum(node) == Player.STOCHASTIC) {
                remaining[node] = 1;
            }
        }

        while (leavingIndex != 0) {
            leavingIndex--;
            int node = leaving[leavingIndex];
            for (int predNr = 0; predNr < graph.getProperties().getNumPredecessors(node); predNr++) {
                int pred = graph.getProperties().getPredecessorNode(node, predNr);
                if (scc.get(pred) && existingStates.get(pred)) {
                    remaining[pred]--;
                    if (remaining[pred] == 0) {
                        existingStates.set(pred, false);
                        leaving[leavingIndex] = pred;
                        leavingIndex++;
                    } else if (remaining[pred] < 0) {
                        assert false;
                    }
                }
            }
        }
    }

    public BitSet reachPre(GraphExplicit graph,
            BitSet target, BitSet nodes, boolean min, boolean one)
    {
        assert graph != null;
        assert target != null;
        assert nodes != null;
        graph.computePredecessors();
        int[] remaining = new int[graph.getNumNodes()];
        BitSet contained = UtilBitSet.newBitSetUnbounded();
        if (one) {
            BitSet reachSome = reachPre(graph, target, nodes, min, false);
            BitSet reachNone = nodes.clone();
            reachNone.andNot(reachSome);
            BitSet nodesAndNotTarget = nodes.clone();
            nodesAndNotTarget.andNot(target);
            reachNone = reachPre(graph, reachNone, nodesAndNotTarget, !min, false);
            BitSet result = nodes.clone();
            result.andNot(reachNone);
            return result;
        }
        NodeProperty player = graph.getNodeProperty(CommonProperties.PLAYER);
        for (int node = 0; node < graph.getNumNodes(); node++) {
            if (player.getEnum(node) == Player.ONE) {
                remaining[node] = min ? graph.getNumSuccessors(node) : 1;
            } else if (player.getEnum(node) == Player.STOCHASTIC) {
                remaining[node] = 1;
            } else {
                assert false;
            }
        }
        for (int node = target.nextSetBit(0); node >= 0; node = target.nextSetBit(node+1)) {
            contained.set(node);
            remaining[node] = 0;
        }
        BitSet result = target.clone();
        BitSet newNodes = target.clone();
        BitSet previousNodes = UtilBitSet.newBitSetUnbounded();
        do {
            result.or(newNodes);
            previousNodes = newNodes.clone();
            newNodes = UtilBitSet.newBitSetUnbounded();
            for (int node = previousNodes.nextSetBit(0); node >= 0; node = previousNodes.nextSetBit(node+1)) {
                for (int predNr = 0; predNr < graph.getProperties().getNumPredecessors(node); predNr++) {
                    int pred = graph.getProperties().getPredecessorNode(node, predNr);
                    if (!nodes.get(pred)) {
                        continue;
                    }
                    if (!contained.get(pred)) {
                        remaining[pred]--;
                        if (remaining[pred] == 0) {
                            contained.set(pred);
                            newNodes.set(pred);
                        }
                    }
                }
            }
        } while (!newNodes.isEmpty());
        return result;
    }

    public BitSet reachPre(GraphExplicit graph,
            BitSet target, boolean min, boolean one) {
        assert graph != null;
        assert target != null;
        if (!min && one) {
            return reachMaxOne(graph, target);
        } else {
            BitSet nodes = UtilBitSet.newBitSetUnbounded();
            nodes.set(0, graph.getNumNodes(), true);
            return reachPre(graph, target, nodes, min, one);
        }
    }

    /**
     * Compute nodes which may reach given set of target nodes with certainty.
     *
     * Adapted from
     * <a href="http://www.prismmodelchecker.org/lectures/pmc/14-mdp%20model%20checking.pdf">
     * Dave Parker's slides</a>
     * 
     * @param graph graph for which to compute set
     * @param target target nodes
     * @return set of nodes which may reach target set with probability one
     */
    public BitSet reachMaxOne(GraphExplicit graph, BitSet target) {
        assert graph != null;
        assert target != null;
        assert target.length() <= graph.getNumNodes()
                : target.length() + SPACE + graph.getNumNodes();
        assert graph.getNodeProperties().contains(CommonProperties.PLAYER);
        NodeProperty playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        BitSet R = UtilBitSet.newBitSetUnbounded();
        R.set(0, graph.getNumNodes(), true);
        boolean done = false;
        graph.computePredecessors();
        while (!done) {
            BitSet Rprimed = target.clone();
            boolean donePrimed = false;
            while (!donePrimed) {
                BitSet RprimedPrimed = Rprimed.clone();
                for (int node = Rprimed.nextSetBit(0); node >= 0; node = Rprimed.nextSetBit(node+1)) {
                    int numNodePredecessors = graph.getProperties().getNumPredecessors(node);
                    for (int predNr = 0; predNr < numNodePredecessors; predNr++) {
                        int predNode = graph.getProperties().getPredecessorNode(node, predNr);
                        Player player = playerProp.getEnum(predNode);
                        if (player == Player.ONE) {
                            RprimedPrimed.set(predNode);
                        } else if (player == Player.STOCHASTIC) {
                            boolean include = true;
                            int numPredNodeSuccessors = graph.getNumSuccessors(predNode);
                            for (int predSuccNr = 0; predSuccNr < numPredNodeSuccessors; predSuccNr++) {
                                int predSucc = graph.getSuccessorNode(predNode, predSuccNr);
                                if (!R.get(predSucc)) {
                                    include = false;
                                    break;
                                }
                            }
                            if (include) {
                                RprimedPrimed.set(predNode);
                            }
                        } else {
                            assert false : player;
                        }        				
                    }
                }
                if (RprimedPrimed.equals(Rprimed)) {
                    donePrimed = true;
                }
                Rprimed = RprimedPrimed;
            }
            if (Rprimed.equals(R)) {
                done = true;
            }
            R = Rprimed.clone();
        }
        return R;
    }

    public BitSet reachMinOne(GraphExplicit graph, BitSet target) {
        assert graph != null;
        assert target != null;
        assert target.length() <= graph.getNumNodes()
                : target.length() + SPACE + graph.getNumNodes();
        assert graph.getNodeProperties().contains(CommonProperties.PLAYER);
        BitSet nodes = UtilBitSet.newBitSetUnbounded();
        nodes.set(0, graph.getNumNodes(), true);
        return reachPre(graph, target, nodes, true, true);
    }

    public BitSet reachMaxOne(GraphExplicit graph, BitSet target, BitSet nodes) {
        return reachPre(graph, target, nodes, false, true);
    }

    public EndComponents maximalEndComponents(GraphExplicit graph) {
        return endComponents(graph, true);
    }

    public EndComponents maximalEndComponents(GraphExplicit graph, BitSet existingNodes) {
        return endComponents(graph, existingNodes, true);        
    }

    public EndComponents stronglyConnectedComponents(GraphExplicit graph) {
        return endComponents(graph, false);
    }

    public EndComponents stronglyConnectedComponents(GraphExplicit graph, BitSet existingNodes) {
        return endComponents(graph, existingNodes, false);        
    }

    public EndComponents endComponents(GraphExplicit graph, boolean mecsOnly) {
        BitSet existingStates = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
        existingStates.flip(0, graph.getNumNodes());
        return endComponents(graph, existingStates, mecsOnly);
    }
}
