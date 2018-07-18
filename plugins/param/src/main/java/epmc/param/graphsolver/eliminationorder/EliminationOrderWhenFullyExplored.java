package epmc.param.graphsolver.eliminationorder;

import java.util.Arrays;

import epmc.param.graph.MutableGraph;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

public final class EliminationOrderWhenFullyExplored implements EliminationOrder {
    public final static class Builder implements EliminationOrder.Builder {
        private MutableGraph graph;

        @Override
        public Builder setGraph(MutableGraph graph) {
            this.graph = graph;
            return this;
        }

        @Override
        public Builder setTarget(BitSet target) {
            return this;
        }

        @Override
        public EliminationOrder build() {
            return new EliminationOrderWhenFullyExplored(this);
        }
        
    }
    
    public final static String IDENTIFIER = "when-fully-explored";
    private int numNodes;
    private int nextNode;
    private int[] order;
    
    private EliminationOrderWhenFullyExplored(Builder builder) {
        assert builder != null;
        numNodes = builder.graph.getNumNodes();
        int[] numIncoming = new int[numNodes];
        MutableGraph graph = builder.graph;
        order = new int[numNodes];
        Arrays.fill(order, -1);
        for (int node = 0; node < numNodes; node++) {
            int numSuccessors = graph.getNumSuccessors(node);
            for (int succ = 0; succ < numSuccessors; succ++) {
                int succState = graph.getSuccessorNode(node, succ);
                numIncoming[succState]++;
            }
        }
        
        BitSet seen = new BitSetUnboundedLongArray();
        int todoFrom = 0;
        int todoTo = 0;
        BitSet initialNodes = graph.getInitialNodes();
        int[] forwardOrder = new int[numNodes];
        Arrays.fill(forwardOrder, -1);
        for (int node = initialNodes.nextSetBit(0); node >= 0; node = initialNodes.nextSetBit(node + 1)) {
            forwardOrder[todoTo] = node;
            seen.set(node);
            todoTo++;
        }
        int addStateNr = 0;
        BitSet added = new BitSetUnboundedLongArray();
        while (todoFrom < todoTo) {
            int node = forwardOrder[todoFrom];
            todoFrom++;
            if (numIncoming[node] == 0 && !added.get(node)) {
                order[addStateNr] = node;
                added.set(node);
                addStateNr++;
            }
            for (int succNr = 0; succNr < graph.getNumSuccessors(node); succNr++) {
                int succ = graph.getSuccessorNode(node, succNr);
                numIncoming[succ]--;
                if (numIncoming[succ] == 0 && !added.get(succ)) {
                    order[addStateNr] = succ;
                    added.set(succ);
                    addStateNr++;
                } else if (numIncoming[succ] < 0) {
                    assert false;
                }
                if (!seen.get(succ)) {
                    seen.set(succ);
                    forwardOrder[todoTo] = succ;
                    todoTo++;
                }
            }
        }
        /*
        for (int node = 0; node < numNodes; node++) {
            if (!added.get(node)) {
                order[addStateNr] = node;
                addStateNr++;
            }
        }
        */
        numNodes = addStateNr;
    }

    @Override
    public boolean hasNodes() {
        return nextNode < numNodes;
    }

    @Override
    public int nextNode() {
        assert nextNode != numNodes;
        int result = order[nextNode];
        nextNode++;
        return result;
    }

    
}
