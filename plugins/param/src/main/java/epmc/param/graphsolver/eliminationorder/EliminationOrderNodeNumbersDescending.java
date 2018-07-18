package epmc.param.graphsolver.eliminationorder;

import epmc.param.graph.MutableGraph;
import epmc.util.BitSet;

public final class EliminationOrderNodeNumbersDescending implements EliminationOrder {
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
            return new EliminationOrderNodeNumbersDescending(this);
        }
        
    }
    
    public final static String IDENTIFIER = "node-numbers-descending";
    private final int numNodes;
    private int nextNode;
    
    private EliminationOrderNodeNumbersDescending(Builder builder) {
        assert builder != null;
        numNodes = builder.graph.getNumNodes();
        nextNode = numNodes - 1;
    }

    @Override
    public boolean hasNodes() {
        return nextNode >= 0;
    }

    @Override
    public int nextNode() {
        assert nextNode != numNodes;
        int result = nextNode;
        nextNode--;
        return result;
    }

    
}
