package epmc.param.graphsolver.eliminationorder;

import epmc.param.graph.MutableGraph;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

public final class EliminationOrderFromTargets implements EliminationOrder {
    public final static class Builder implements EliminationOrder.Builder {
        private MutableGraph graph;
        private BitSet target;

        @Override
        public Builder setGraph(MutableGraph graph) {
            this.graph = graph;
            return this;
        }

        @Override
        public Builder setTarget(BitSet target) {
            this.target = target;
            return this;
        }

        @Override
        public EliminationOrder build() {
            return new EliminationOrderFromTargets(this);
        }
        
    }
    
    public final static String IDENTIFIER = "from-target";
    private int nextIndex;
    private final BitSet target;
    private final int[] order;
    private int numNodes;
    
    private EliminationOrderFromTargets(Builder builder) {
        assert builder != null;
        int numNodes = builder.graph.getNumNodes();
        order = new int[numNodes];
        target = builder.target;
        BitSet seen = new BitSetUnboundedLongArray();
        int todoFrom = 0;
        int todoTo = 0;
        for (int node = target.nextSetBit(0); node >= 0; node = target.nextSetBit(node + 1)) {
            order[todoTo] = node;
            seen.set(node);
            todoTo++;
        }
        MutableGraph graph = builder.graph;
        while (todoFrom < todoTo) {
            int node = order[todoFrom];
            todoFrom++;
            for (int predNr = 0; predNr < graph.getNumPredecessors(node); predNr++) {
                int pred = graph.getPredecessorNode(node, predNr);
                if (seen.get(pred)) {
                    continue;
                }
                seen.set(pred);
                order[todoTo] = pred;
                todoTo++;
            }
        }
        this.numNodes = todoTo;
    }

    @Override
    public boolean hasNodes() {
        return nextIndex < numNodes;
    }

    @Override
    public int nextNode() {
        assert nextIndex < numNodes;
        int result = order[nextIndex];
        System.out.println(nextIndex);
        nextIndex++;
        return result;
    }
}
