package epmc.param.graph;

import java.util.ArrayList;

import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitProperties;
import epmc.graph.explicit.GraphExporterDOT;
import epmc.util.BitSet;
import epmc.util.BitSetUnboundedIntArray;
import epmc.util.BitSetUnboundedLongArray;
import epmc.value.Type;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.array.TIntArrayStack;

public final class MutableGraph implements GraphExplicit {
    private final static int UNDEFINED = -1;
    private BitSet initialNodes = new BitSetUnboundedLongArray();
    private int numNodes;
    private final ArrayList<TIntArrayList> successors = new ArrayList<>();
    private final ArrayList<TIntArrayList> predecessors = new ArrayList<>();
    private final TIntArrayStack unusedNodes = new TIntArrayStack();
    private final BitSet usedNodes = new BitSetUnboundedIntArray();
    private final GraphExplicitProperties properties = new GraphExplicitProperties(this);
    private int totalNumTransitions;
    private boolean removeShift = false;

    public MutableGraph() {
        properties.registerNodeProperty(PropertyNames.USED_NODES, 
                new UsedNodesProperty(this));
    }
    
    public int addNode() {
        int node;
        if (unusedNodes.size() == 0) {
            node = successors.size();
            successors.add(new TIntArrayList());
            predecessors.add(new TIntArrayList());
        } else {
            node = unusedNodes.pop();
        }
        usedNodes.set(node);
        numNodes++;
        return node;
    }
    
    public void clearPredecessorList(int node) {
        TIntArrayList nodePredecessors = predecessors.get(node);
        nodePredecessors.resetQuick();
    }

    public void removeSuccessorNumber(int node, int succNr) {
        TIntArrayList nodeSuccessors = successors.get(node);
        int lastEntry = nodeSuccessors.get(nodeSuccessors.size() - 1);
        if (removeShift) {
            nodeSuccessors.removeAt(succNr);
//            System.out.println("> " + succNr + " " + nodeSuccessors);
//            for (int i = succNr; i < nodeSuccessors.size() - 1; i++) {
  //              nodeSuccessors.setQuick(i, nodeSuccessors.getQuick(i+1));
    //        }
        } else {
            nodeSuccessors.setQuick(succNr, lastEntry);
            nodeSuccessors.removeAt(nodeSuccessors.size() - 1);
//        System.out.println("< " + succNr + " " + nodeSuccessors);
        }
        totalNumTransitions--;
    }

    public void removeNode(int node) {
        assert node >= 0;
        assert usedNodes.get(node);
        assert successors.get(node).size() == 0;
        assert predecessors.get(node).size() == 0 : node + " " + predecessors.get(node);
        usedNodes.clear(node);
        numNodes--;
        unusedNodes.push(node);
    }
    
    @Override
    public int getNumNodes() {
        return successors.size();
    }

    public int getNumNodesUsed() {
        return numNodes;
    }
    
    @Override
    public BitSet getInitialNodes() {
        return initialNodes;
    }

    @Override
    public int getNumSuccessors(int node) {
        return successors.get(node).size();
    }

    public int getNumPredecessors(int node) {
        return predecessors.get(node).size();        
    }
    
    @Override
    public int getSuccessorNode(int node, int successor) {
        int result = successors.get(node).getQuick(successor);
        if (result == UNDEFINED) {
            result = node;
        }
        return result;
    }

    public int getPredecessorNode(int node, int predNr) {
        int result = predecessors.get(node).getQuick(predNr);
        if (result == UNDEFINED) {
            result = node;
        }
        return result;
    }

    @Override
    public void setSuccessorNode(int node, int succNr, int succNode) {
        successors.get(node).setQuick(succNr, succNode);
        predecessors.get(succNode).add(node);
    }
    
    @Override
    public GraphExplicitProperties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return GraphExporterDOT.toString(this);
    }
    
    BitSet getUsedNodes() {
        return usedNodes;
    }

    public void addSuccessor(int node, int successor) {
        TIntArrayList nodeSuccessors = successors.get(node);
        nodeSuccessors.add(successor);
        TIntArrayList nodePredecessors = predecessors.get(successor);
        nodePredecessors.add(node);
        totalNumTransitions++;
    }

    public MutableNodeProperty addMutableNodeProperty(Object name, Type type) {
        MutableNodeProperty reward = new MutableNodeProperty(this, type);
        properties.registerNodeProperty(name, reward);
        return reward;
    }

    public MutableEdgeProperty addMutableEdgeProperty(Object name, Type type) {
        MutableEdgeProperty reward = new MutableEdgeProperty(this, type);
        properties.registerEdgeProperty(name, reward);
        return reward;
    }

    public boolean isUsedNode(int node) {
        assert node >= 0;
        assert node < successors.size();
        return usedNodes.get(node);
    }
    
    public int getTotalNumTransitions() {
        return totalNumTransitions;
    }

    public void clearSuccessors(int node) {
        TIntArrayList nodeSuccessors = successors.get(node);
        for (int succNr = 0; succNr < nodeSuccessors.size(); succNr++) {
            int succ = nodeSuccessors.get(succNr);
            TIntArrayList succPredecessors = predecessors.get(succ);
            for (int succPredNr = 0; succPredNr < succPredecessors.size(); succPredNr++) {
                int succPred = succPredecessors.get(succPredNr);
                if (succPred == node) {
                    succPredecessors.setQuick(succPredNr, succPredecessors.get(succPredecessors.size() - 1));
                    succPredecessors.removeAt(succPredecessors.size() - 1);
                    break;
                }
            }
        }
        totalNumTransitions -= nodeSuccessors.size();
        nodeSuccessors.reset();
    }
    
    boolean isRemoveShift() {
        return removeShift;
    }
    
    @Override
    public void close() {
    }
}
