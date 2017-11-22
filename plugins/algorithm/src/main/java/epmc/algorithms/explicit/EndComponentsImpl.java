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

public class EndComponentsImpl implements EndComponents {
    private final GraphExplicit graph;
    private final BitSet existing;
    private boolean hasNext;
    private final int[] scc;
    private int sccSize;
    private int[] todo;
    private int todoSize;
    private int[] nextTodo;
    private int nextTodoSize;

    private int tjNode;
    private int tjMaxdfs;
    private final int[] tjStack;
    private BitSet tjInStack;
    private BitSet tjVisited;
    private final int[] tjDfs;
    private final int[] tjLowlink;
    private final int[] tjCallNodeStack;
    private int[] tjCallSuccStack;
    private int tjCallStackIndex = -1;
    private int tjStackIndex;
    private int tjSuccIter;

    private final int[] ckRemaining;
    private final BitSet ckInScc;
    private final int[] ckLeaving;
    private final NodeProperty playerProp;
    private final boolean mecsOnly;

    public EndComponentsImpl(GraphExplicit graph, BitSet existing, boolean mecsOnly) {
        this.graph = graph;
        this.playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        this.existing = existing;
        this.scc = new int[graph.getNumNodes()];
        this.todo = new int[graph.getNumNodes()];
        this.nextTodo = new int[graph.getNumNodes()];
        for (int node = existing.nextSetBit(0); node >= 0;
                node = existing.nextSetBit(node+1)) {
            todo[todoSize] = node;
            todoSize++;
        }

        this.tjStack = new int[graph.getNumNodes()];
        this.tjInStack = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
        this.tjVisited = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
        this.tjDfs= new int[graph.getNumNodes()];
        this.tjLowlink = new int[graph.getNumNodes()];
        this.tjCallNodeStack = new int[graph.getNumNodes()];
        this.tjCallSuccStack = new int[graph.getNumNodes()];

        this.ckRemaining = new int[graph.getNumNodes()];
        this.ckInScc = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
        this.ckLeaving = new int[graph.getNumNodes()];
        this.mecsOnly = mecsOnly;
    }

    private void computeNextComponent() {
        hasNext = false;
        while (!hasNext && todoSize != 0) {
            if (tjCallStackIndex == -1) {
                todoSize--;
                int node = todo[todoSize];
                while (todoSize != 0
                        && (!existing.get(node) || tjVisited.get(node))) {
                    todoSize--;
                    node = todo[todoSize];
                }
                if (existing.get(node) && !tjVisited.get(node)) {
                    tjNode = node;
                    tarjanInit();
                }
            }
            tarjan();
            if (hasNext) {
                if (mecsOnly) {
                    checkMEC();
                } else {
                    for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
                        int node = scc[nodeNr];
                        this.existing.set(node, false);
                    }
                }
            }
            if (todoSize == 0) {
                int[] swap = todo;
                todo = nextTodo;
                nextTodo = swap;
                todoSize = nextTodoSize;
                nextTodoSize = 0;
                tjMaxdfs = 0;
                tjVisited.clear();
            }
        }
        if (!hasNext && todoSize == 0 && !existing.isEmpty()) {
            assert false :
                existing.cardinality() + " " + existing.nextSetBit(0);
        }
    }

    private void tarjanInit() {
        tjCallStackIndex = 0;
        tjStackIndex = 0;
        tjSuccIter = 0;

        tjDfs[tjNode] = tjMaxdfs;
        tjLowlink[tjNode] = tjMaxdfs;
        tjMaxdfs++;
        tjStack[tjStackIndex] = tjNode;
        tjInStack.set(tjNode, true);
        tjStackIndex++;
        tjVisited.set(tjNode, true);
    }

    @Override
    public BitSet next() {
        computeNextComponent();
        if (!hasNext) {
            return null;
        }
        // TODO if this copying turns out to be a bottleneck, could avoid
        // but have to be careful then as in some cases we do have to copy
        BitSet result = UtilBitSet.newBitSetUnbounded(sccSize);
        for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
            result.set(scc[nodeNr]);
        }
        return result;
    }

    private void tarjan() {
        while (!hasNext && tjCallStackIndex >= 0) {
            int numSucc = graph.getNumSuccessors(tjNode);
            if (tjSuccIter < numSucc) {
                int succNode = graph.getSuccessorNode(tjNode, tjSuccIter);
                tjSuccIter++;
                if (existing.get(succNode)) {
                    if (!tjVisited.get(succNode)) {
                        tjCallNodeStack[tjCallStackIndex] = tjNode;
                        tjCallSuccStack[tjCallStackIndex] = tjSuccIter;
                        tjCallStackIndex++;
                        tjNode = succNode;
                        tjSuccIter = 0;
                        tjDfs[tjNode] = tjMaxdfs;
                        tjLowlink[tjNode] = tjMaxdfs;
                        tjMaxdfs++;
                        tjStack[tjStackIndex] = tjNode;
                        tjInStack.set(tjNode, true);
                        tjStackIndex++;
                        tjVisited.set(tjNode, true);
                    } else if (tjInStack.get(succNode)) {
                        tjLowlink[tjNode] = Math.min(tjLowlink[tjNode], tjDfs[succNode]);
                    }
                }
            } else {
                if (tjLowlink[tjNode] == tjDfs[tjNode]) {
                    int succNode;
                    sccSize = 0;
                    do {
                        tjStackIndex--;
                        succNode = tjStack[tjStackIndex];
                        tjInStack.set(succNode, false);
                        scc[sccSize] = succNode;
                        sccSize++;
                    } while (tjNode != succNode);
                    hasNext = true;
                }
                tjCallStackIndex--;
                if (tjCallStackIndex >= 0) {
                    int succNode = tjNode;
                    tjNode = tjCallNodeStack[tjCallStackIndex];
                    tjSuccIter = tjCallSuccStack[tjCallStackIndex];
                    tjLowlink[tjNode] = Math.min(tjLowlink[tjNode], tjLowlink[succNode]);
                }
            }
        }
    }

    private void checkMEC() {
        if (this.sccSize == 1) {
            int node = scc[0];
            this.existing.set(scc[0], false);
            if (graph.getNumSuccessors(node) == 0) {
                this.hasNext = false;
                return;
            } else {
                for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                    int succ = graph.getSuccessorNode(node, succNr);
                    if (succ != node) {
                        this.hasNext = false;
                        return;
                    }
                }
            }
            hasNext = true;
        } else {
            int leavingIndex = 0;
            boolean isEndComponent = true;
            for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
                int node = scc[nodeNr];
                this.ckInScc.set(node, true);
            }
            leavingIndex = 0;
            for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
                int node = scc[nodeNr];
                Player player = playerProp.getEnum(node);
                if (player == Player.STOCHASTIC) {
                    int numSucc = graph.getNumSuccessors(node);
                    for (int succNr = 0; succNr < numSucc; succNr++) {
                        int succ = graph.getSuccessorNode(node, succNr);
                        if (!this.ckInScc.get(succ)) {
                            this.ckLeaving[leavingIndex] = node;
                            leavingIndex++;
                            this.existing.set(node, false);
                            isEndComponent = false;
                            break;
                        }
                    }
                } else if (player == Player.ONE) {
                    boolean foundIn = false;
                    int numSucc = graph.getNumSuccessors(node);
                    for (int succNr = 0; succNr < numSucc; succNr++) {
                        int succ = graph.getSuccessorNode(node, succNr);
                        if (this.ckInScc.get(succ)) {
                            foundIn = true;
                        }
                    }
                    if (!foundIn) {
                        this.ckLeaving[leavingIndex] = node;
                        leavingIndex++;
                        this.existing.set(node, false);
                        isEndComponent = false;
                    }
                }
            }
            if (isEndComponent) {
                this.hasNext = true;
                for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
                    int node = scc[nodeNr];
                    this.existing.set(node, false);
                }
            } else {
                removeLeavingAttr(this.graph, this.existing, this.ckRemaining,
                        this.ckInScc, this.ckLeaving,
                        leavingIndex);
                for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
                    int node = this.scc[nodeNr];
                    if (this.existing.get(node)) {
                        this.nextTodo[nextTodoSize] = node;
                        this.nextTodoSize++;
                    }
                }
            }
            for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
                int node = scc[nodeNr];
                this.ckInScc.set(node, false);
            }
            this.hasNext = isEndComponent;
        }
    }

    private void removeLeavingAttr(GraphExplicit graph,
            BitSet existingStates, int[] remaining,
            BitSet scc, int[] leaving, int leavingIndex) {
        graph.computePredecessors();
        NodeProperty player = graph.getNodeProperty(CommonProperties.PLAYER);
        for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
            int node = this.scc[nodeNr];
            remaining[node] = 0;
            if (player.getEnum(node) == Player.ONE) {
                int numSucc = graph.getNumSuccessors(node);
                for (int succNr = 0; succNr < numSucc; succNr++) {
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

}
