package epmc.param.graphsolver.eliminationorder;

import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.param.graph.MutableGraph;
import epmc.util.BitSet;
import epmc.util.BitSetBoundedLongArray;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public final class EliminationOrderSameStructure implements EliminationOrder {
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
            return new EliminationOrderSameStructure(this);
        }
        
    }
    
    public final static String IDENTIFIER = "same-structure";
    private final MutableGraph graph;
    private final int numNodes;
    private int nextNode;
    private final IntArrayList stateList = new IntArrayList();
    private int stateEntry;
    private int[] todo;
    
    private EliminationOrderSameStructure(Builder builder) {
        assert builder != null;
        graph = builder.graph;
        numNodes = builder.graph.getNumNodes();
        todo = new int[numNodes];
        for (int node = 0; node < numNodes; node++) {
            todo[node] = node;
        }
        createOrder();
    }

    @Override
    public boolean hasNodes() {
        return nextNode < numNodes;
    }

    @Override
    public int nextNode() {
        assert nextNode != numNodes;
        if (stateEntry >= stateList.size()) {
            createOrder();
            stateEntry = 0;
        }
        nextNode++;
        int result = stateList.getInt(stateEntry);
        stateEntry++;
        return result;
    }

    private void createOrder() {
        // TODO currently only approximate
        stateList.clear();
        Long2ObjectOpenHashMap<IntArrayList> map = new Long2ObjectOpenHashMap<>();
        EdgeProperty weights = graph.getEdgeProperty(CommonProperties.WEIGHT);
        for (int nodeNr = 0; nodeNr < todo.length; nodeNr++) {
            int node = todo[nodeNr];
            long hash = 0;
            int successors = graph.getNumSuccessors(node);
            int predecessors = graph.getNumPredecessors(node);
            hash = hash(hash, successors);
            hash = hash(hash, predecessors);
            for (int succNr = 0; succNr < successors; succNr++) {
                hash = hash(hash, weights.get(node, succNr).hashCode());
            }
            for (int predNr = 0; predNr < predecessors; predNr++) {
                int predNode = graph.getPredecessorNode(node, predNr);
                int predSuccNr = graph.getSuccessorNumber(predNode, node);
                hash = hash(hash, weights.get(predNode, predSuccNr).hashCode());
            }
            // TODO take reward into account
            IntArrayList array = map.get(hash);
            if (array == null) {
                array = new IntArrayList();
                map.put(hash, array);
            }
            array.add(node);
        }
        IntArrayList[] maxArg = new IntArrayList[1];
        
        int[] max = new int[1];
        max[0] = -1;
        
        map.forEach((a, array) -> {
            if (array.size() >= max[0]) {
                maxArg[0] = array;
                max[0] = array.size();
            } 
        });
//        System.out.println("MM  " + max);
        BitSet chosenOrMarked = new BitSetBoundedLongArray(numNodes);
        BitSet chosen = new BitSetBoundedLongArray(numNodes);
        for (int entryNr = 0; entryNr < maxArg[0].size(); entryNr++) {
            int node = maxArg[0].getInt(entryNr);
            if (chosenOrMarked.get(node)) {
                continue;
            }
            chosenOrMarked.set(node);
            int successors = graph.getNumSuccessors(node);
            for (int succNr = 0; succNr < successors; succNr++) {
                chosenOrMarked.set(graph.getSuccessorNode(node, succNr));
            }
            int predecessors = graph.getNumPredecessors(node);
            for (int predNr = 0; predNr < predecessors; predNr++) {
                chosenOrMarked.set(graph.getPredecessorNode(node, predNr));
            }
            stateList.add(node);
            chosen.set(node);
        }
        int[] newTodo = new int[todo.length - chosen.cardinality()];
        int newTodoEntry = 0;
        for (int nodeNr = 0; nodeNr < todo.length; nodeNr++) {
            int node = todo[nodeNr];
            if (chosen.get(node)) {
                continue;
            }
            newTodo[newTodoEntry] = node;
            newTodoEntry++;
        }
        todo = newTodo;        
//        System.out.println("SL  " + stateList.size());
    }

    private long hash(long old, long next) {
        return next + (old << 6) + (old << 16) - old;
    }    
}
