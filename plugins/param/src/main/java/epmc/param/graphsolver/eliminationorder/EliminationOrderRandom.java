package epmc.param.graphsolver.eliminationorder;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import epmc.param.graph.MutableGraph;
import epmc.util.BitSet;

public final class EliminationOrderRandom implements EliminationOrder {
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
            return new EliminationOrderRandom(this);
        }
        
    }
    
    public final static String IDENTIFIER = "random";
    private final int numNodes;
    private final int[] nodes;
    private int nextIndex;
    
    private EliminationOrderRandom(Builder builder) {
        assert builder != null;
        numNodes = builder.graph.getNumNodes();
        nodes = new int[numNodes];
        for (int node = 0; node < numNodes; node++) {
            nodes[node] = node;
        }
        shuffleArray(nodes);
    }

    @Override
    public boolean hasNodes() {
        return nextIndex < numNodes;
    }

    @Override
    public int nextNode() {
        assert nextIndex != numNodes;
        int result = nodes[nextIndex];
        nextIndex++;
        return result;
    }

    /**
     *  Implementing Fisher–Yates shuffle
     *  https://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
     * @param array
     */
    private static void shuffleArray(int[] array) {
        Random random = ThreadLocalRandom.current();
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }
    
}
