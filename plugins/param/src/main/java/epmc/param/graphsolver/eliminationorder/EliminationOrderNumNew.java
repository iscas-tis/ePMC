package epmc.param.graphsolver.eliminationorder;

import java.util.Map.Entry;
import java.util.TreeMap;

import epmc.param.graph.MutableGraph;
import epmc.util.BitSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public final class EliminationOrderNumNew implements EliminationOrder {
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
            return new EliminationOrderNumNew(this);
        }

    }

    public final static String IDENTIFIER = "num-new";
    private final MutableGraph graph;
    private final TreeMap<Integer,IntOpenHashSet> valueToNode = new TreeMap<>();
    private final int[] nodeToValue;
    private final IntArrayList mustUpdate = new IntArrayList();
    private final boolean[] mustUpdateSeen;
    private final boolean[] removed;
    
    private EliminationOrderNumNew(Builder builder) {
        graph = builder.graph;
        int numNodes = graph.getNumNodes();
        nodeToValue = new int[numNodes];
        for (int node = 0; node < numNodes; node++) {
            int value = value(node);
            nodeToValue[node] = value;
            IntOpenHashSet list = valueToNode.get(value);
            if (list == null) {
                list = new IntOpenHashSet();
                valueToNode.put(value, list);
            }
            list.add(node);
        }
        removed = new boolean[numNodes];
        mustUpdateSeen = new boolean[numNodes];
    }

    @Override
    public boolean hasNodes() {
        return !valueToNode.isEmpty();
    }

    @Override
    public int nextNode() {
        updateNodes();
        Entry<Integer, IntOpenHashSet> entry = valueToNode.firstEntry();
        int value = entry.getKey();
        IntOpenHashSet list = entry.getValue();
        int chosen = list.iterator().nextInt();
        list.remove(chosen);
        if (list.isEmpty()) {
            valueToNode.remove(value);
        }
        removed[chosen] = true;
        collectNeighbors(chosen);
        return chosen;
    }

    private void updateNodes() {
        int mustUpdateSize = mustUpdate.size();
        for (int index = 0; index < mustUpdateSize; index++) {
            int node = mustUpdate.getInt(index);
            assert !removed[node] : node;
            mustUpdateSeen[node] = false;
            int oldValue = nodeToValue[node];
            int newValue = value(node);
            if (oldValue == newValue) {
                continue;
            }
            IntOpenHashSet list = valueToNode.get(oldValue);
            assert list != null : oldValue;
            list.remove(node);
            if (list.isEmpty()) {
                valueToNode.remove(oldValue);
            }
            list = valueToNode.get(newValue);
            if (list == null) {
                list = new IntOpenHashSet();
                valueToNode.put(newValue, list);                
            }
            list.add(node);
            nodeToValue[node] = newValue;
        }
    }

    private void collectNeighbors(int node) {
        int numPred = graph.getNumPredecessors(node);
        int numSucc = graph.getNumSuccessors(node);
        mustUpdate.clear();
        for (int predNr = 0; predNr < numPred; predNr++) {
            int pred = graph.getPredecessorNode(node, predNr);
            if (pred == node) {
                continue;
            }
            if (mustUpdateSeen[pred]) {
                continue;
            }
            if (removed[pred]) {
                continue;
            }
            mustUpdate.add(pred);
            mustUpdateSeen[pred] = true;
        }
        for (int succNr = 0; succNr < numSucc; succNr++) {
            int succ = graph.getSuccessorNode(node, succNr);
            if (succ == node) {
                continue;
            }
            if (mustUpdateSeen[succ]) {
                continue;
            }
            if (removed[succ]) {
                continue;
            }
            mustUpdate.add(succ);
            mustUpdateSeen[succ] = true;
        }
    }

    private int value(int node) {
        int numNew = 0;
        int numPred = graph.getNumPredecessors(node);
        int numSucc = graph.getNumSuccessors(node);
        for (int predNr = 0; predNr < numPred; predNr++) {
            int pred = graph.getPredecessorNode(node, predNr);
            if (pred == node) {
                continue;
            }
            for (int succNr = 0; succNr < numSucc; succNr++) {
                int succ = graph.getSuccessorNode(node, succNr);
                if (succ == node) {
                    continue;
                }
                if (graph.getSuccessorNumber(pred, succ) != -1) {
                    continue;
                } else {
                    numNew++;
                }
            }
        }
        numNew = numNew - (numPred + numSucc);
        if (graph.getSuccessorNumber(node, node) != -1) {
            numNew++;
        }   
        return numNew;
    }
}
