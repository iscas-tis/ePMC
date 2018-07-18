package epmc.param.graphsolver.eliminationorder;

import epmc.param.graph.MutableGraph;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedLongArray;

public final class EliminationOrderMinProdPredSucc implements EliminationOrder {
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
            return new EliminationOrderMinProdPredSucc(this);
        }

    }

    public final static String IDENTIFIER = "min-prod-pred-times";
    private final MutableGraph graph;
    private final BitSet todo = new BitSetUnboundedLongArray();

    private EliminationOrderMinProdPredSucc(Builder builder) {
        graph = builder.graph;
        todo.flip(0, graph.getNumNodes());
    }

    @Override
    public boolean hasNodes() {
        return !todo.isEmpty();
    }

    @Override
    public int nextNode() {
        int min = Integer.MAX_VALUE;
        int chosen = -1;
        for (int node = todo.nextSetBit(0); node >= 0; node = todo.nextSetBit(node + 1)) {
            int numPred = graph.getNumPredecessors(node);
            int numSucc = graph.getNumSuccessors(node);
            if (graph.getSuccessorNumber(node, node) != -1) {
                numPred--;
                numSucc--;
            }
            int prod = numSucc * numPred;
            if (prod < min) {
                min = prod;
                chosen = node;
            }
            if (min == 1) {
                break;
            }
        }
        todo.clear(chosen);
        return chosen;
    }
}
