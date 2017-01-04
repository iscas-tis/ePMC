package epmc.propertysolverltlfg.algorithm;

import epmc.algorithms.explicit.EndComponents;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.propertysolverltlfg.automaton.RabinTransitionUtil;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.ValueObject;

// specific SCC decomposition for transition-based acceptances
public class ComponentsExplicitImpl implements EndComponents {
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
    private final int finiteEdges;
    private EdgeProperty edgeProp ;

    public ComponentsExplicitImpl(GraphExplicit graph, BitSet existing, int finEdge, boolean mecsOnly) {
        this.graph = graph;
        this.playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        this.edgeProp = graph.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        this.existing = existing;
        this.finiteEdges = finEdge;
        this.scc = new int[graph.getNumNodes()];
        this.todo = new int[graph.getNumNodes()];
        this.nextTodo = new int[graph.getNumNodes()];
        for (int node = existing.nextSetBit(0); node >= 0;
                node = existing.nextSetBit(node+1)) {
            todo[todoSize] = node;
            todoSize++;
        }// first get all states possibly in MECs and compute amounts

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
    // first determine whether there is another component
    private void computeNextComponent() throws EPMCException {
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
            }// call stack is empty
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
    public BitSet next() throws EPMCException {
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

    private void tarjan() throws EPMCException {
        while (!hasNext && tjCallStackIndex >= 0) {
            graph.queryNode(tjNode);
            int numSucc = graph.getNumSuccessors();
            if (tjSuccIter < numSucc) {
                int succNode = graph.getSuccessorNode(tjSuccIter);
                tjSuccIter++;
                if(ignoreEdges(tjSuccIter - 1)) continue;   
                if (existing.get(succNode)) { // if succNode is state of interest
                    if (!tjVisited.get(succNode)) {//it is never been visited, start new tarjan
                        tjCallNodeStack[tjCallStackIndex] = tjNode;
                        tjCallSuccStack[tjCallStackIndex] = tjSuccIter;  // record successor stack index
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
            } else {//else we extract nodes from stack
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
	// ignore edges that do not exist, that is, 
    // ignore edges when it is labeled by
    // finite transition label
    private boolean ignoreEdges(int succNr) throws EPMCException {
    	NodeProperty isState = graph.getNodeProperty(CommonProperties.STATE);
    	//we do not ignore edges or those edges starting from actions
    	if(finiteEdges == -1 || !isState.getBoolean()) return false;  
    	RabinTransitionUtil edge = ValueObject.asObject(this.edgeProp.get(succNr)).getObject();
    	if(edge == null) return false;
    	return edge.getLabeling().get(finiteEdges);  
    }

    private void checkMEC() throws EPMCException {
        if (this.sccSize == 1) {
            int node = scc[0];
            this.existing.set(scc[0], false);
            this.graph.queryNode(node);
            if (graph.getNumSuccessors() == 0) { // trivial nodes without self-loop
                this.hasNext = false;
                return;
            } else {
            	int counter = 0;
                for (int succNr = 0; succNr < graph.getNumSuccessors(); succNr++) {
                    int succ = graph.getSuccessorNode(succNr);
                	// ignore edges that do not exist
                	if(ignoreEdges(succNr)) continue;   
                	++ counter; 
                    if (succ != node) {
                        this.hasNext = false;
                        return;
                    }
                }// we do not count finite edges
                if(counter == 0) {
                	this.hasNext = false;
                	return ;
                }
            } // check whether it has only self-loops
            hasNext = true;
        } else {// not trivial MECs
            int leavingIndex = 0;
            boolean isEndComponent = true;
            for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
                int node = scc[nodeNr];
                this.ckInScc.set(node, true);
            }// first set all nodes in SCC
            // check whether there exists outgoing edges 
            leavingIndex = 0;
            for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
                int node = scc[nodeNr];
                this.graph.queryNode(node);
                Player player = playerProp.getEnum();
                if (player == Player.STOCHASTIC) {// resolve distribution, it is action
                    int numSucc = graph.getNumSuccessors();
                    for (int succNr = 0; succNr < numSucc; succNr++) {
                        int succ = graph.getSuccessorNode(succNr);
                        if(ignoreEdges(succNr)) continue; // do not consider finite edges
                        if (!this.ckInScc.get(succ)) { // encounter nodes outside scc
                            this.ckLeaving[leavingIndex] = node;
                            leavingIndex++;
                            this.existing.set(node, false);
                            isEndComponent = false;
                            break;
                        }
                    }
                } else if (player == Player.ONE) {// resolve nondeterministic choices, it is state
                    boolean foundIn = false;
                    int numSucc = graph.getNumSuccessors();
                    for (int succNr = 0; succNr < numSucc; succNr++) {
                        int succ = graph.getSuccessorNode(succNr);
                        if(ignoreEdges(succNr)) continue;
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
                }// remove nodes
            } else {
                removeLeavingAttr(this.graph, this.existing, this.ckRemaining,
                        this.ckInScc, this.ckLeaving,
                        leavingIndex);// what for?
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
    // what is this function for?
    private void removeLeavingAttr(GraphExplicit graph,
            BitSet existingStates, int[] remaining,
            BitSet scc, int[] leaving, int leavingIndex) throws EPMCException {
        graph.computePredecessors();
        NodeProperty player = graph.getNodeProperty(CommonProperties.PLAYER);
        for (int nodeNr = 0; nodeNr < sccSize; nodeNr++) {
            int node = this.scc[nodeNr];
            remaining[node] = 0;
            graph.queryNode(node);
            if (player.getEnum() == Player.ONE) {
                int numSucc = graph.getNumSuccessors();
                for (int succNr = 0; succNr < numSucc; succNr++) {
                    int succ = graph.getSuccessorNode(succNr);
                    if(ignoreEdges(succNr)) continue;
                    if (scc.get(succ)) {
                        remaining[node]++;
                    }// still we keep this node
                }
            } else if (player.getEnum() == Player.STOCHASTIC) {
                remaining[node] = 1;
            }
        }

        while (leavingIndex != 0) {
            leavingIndex--;
            int node = leaving[leavingIndex];
            graph.queryNode(node);
            for (int predNr = 0; predNr < graph.getNumPredecessors(); predNr++) {
                int pred = graph.getPredecessorNode(predNr);
                if (scc.get(pred) && existingStates.get(pred)) { // it is in scc
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
