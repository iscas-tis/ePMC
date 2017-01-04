package epmc.propertysolverltlfg.algorithm;

import java.util.LinkedList;
import java.util.List;

import epmc.algorithms.explicit.EndComponents;
import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.propertysolverltlfg.automaton.RabinTransitionUtil;
import epmc.util.BitSet;
import epmc.util.UtilBitSet;
import epmc.value.ContextValue;
import epmc.value.ValueObject;

// specific SCC decomposition for transition-based acceptances
public class ComponentsEX implements EndComponents {
    private final GraphExplicit graph;
    private final BitSet existing;
    private int sccSize;
    private final List<BitSet> sccs = new LinkedList<>();
	private final int[] tjStack;
    private final int[] tjDfs;
    private final int[] tjLowlink;
    private int tjStackIndex = 0;
    private BitSet tjInStack;
    private int tjMaxDfs = 0;

//    private final NodeProperty playerProp;
    private final boolean mecsOnly;
    private final int finiteEdges;
    private EdgeProperty edgeProp ;
    private int sccIndex = 0;
    private ContextValue contextValue ;
    private NodeProperty isState;
    private BitSet actions;

    public ComponentsEX(GraphExplicit graph, BitSet existing, int finEdge, boolean mecsOnly) {
        contextValue = graph.getContextValue();
        this.graph = graph;
        this.isState = graph.getNodeProperty(CommonProperties.STATE);
//        this.playerProp = graph.getNodeProperty(CommonProperties.PLAYER);
        this.edgeProp = graph.getEdgeProperty(CommonProperties.AUTOMATON_LABEL);
        this.existing = existing;
        this.finiteEdges = finEdge;

        this.tjStack = new int[graph.getNumNodes()];
        this.tjInStack = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
        this.actions = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
        this.tjDfs= new int[graph.getNumNodes()];
        this.tjLowlink = new int[graph.getNumNodes()];
        this.mecsOnly = mecsOnly;
    }
    
    private void tarjan() throws EPMCException {
    	for(int node = existing.nextSetBit(0); 
    			node >= 0 ;
    			node = existing.nextSetBit(node + 1)) {
    		if(tjDfs[node] == 0) {
    			tarjan(node);
    		}
    	}
    }
    
    private boolean edgeNotInMecs(int succNr) throws EPMCException {
    	if(finiteEdges == -1) return false;  // if true, do not ignore
    	if(finiteEdges == -2) return true;   // if false, always ignore
    	if(! isState.getBoolean()) return false;
    	RabinTransitionUtil edge = ValueObject.asObject(edgeProp.get(succNr)).getObject();
    	if(edge == null) return false;
    	return edge.getLabeling().get(finiteEdges);  
    }
    // when we compute SCCs, we totally ignore those edges 
    // which are labeled by finite number
    private void tarjan(int node) throws EPMCException {
		tjDfs[node] = tjLowlink[node] = ++ tjMaxDfs;
		tjInStack.set(node);
		tjStack[tjStackIndex ++] = node;
		int succNr = 0;
		while(true) {
			graph.queryNode(node); // every reset node 
			if(! isState.getBoolean()) actions.set(node);
			if(succNr >= graph.getNumSuccessors()) break;
			int succ = graph.getSuccessorNode(succNr);
			++ succNr;
			if(edgeNotInMecs(succNr - 1) || !existing.get(succ)) {
				continue;
			}
			// do not consider states outside 
			if (tjDfs[succ] == 0) {
				tarjan(succ);
				tjLowlink[node] = Math.min(tjLowlink[node], tjLowlink[succ]);
			} else if (tjInStack.get(succ)) {
				tjLowlink[node] = Math.min(tjDfs[succ], tjLowlink[node]);
			}
			
		}
		
		if(tjDfs[node] == tjLowlink[node]) {
			int succ = -1;
			sccSize = 0;
			BitSet scc = UtilBitSet.newBitSetUnbounded(graph.getNumNodes());
			do {
				succ = tjStack[-- tjStackIndex];
				tjInStack.clear(succ);
				scc.set(succ);
				++ sccSize; 
			}while(succ != node);
			if(mecsOnly) {
                if(checkMec(scc)) sccs.add(scc);
			}else {
				if(checkScc(scc)) sccs.add(scc);
			}
		}
    }
    
    private boolean checkScc(BitSet scc) throws EPMCException {
    	// trivial SCCs
    	if(sccSize == 1) {
    		int node = scc.nextSetBit(0);
    		graph.queryNode(node);
    		int numSucc = graph.getNumSuccessors();
            if (numSucc == 0) { // trivial nodes without self-loop
                return false;
            } else { 
            	//need to be more careful, must have at least one self-loop
            	int num = 0;
            	for(int succNr = 0; succNr < numSucc; succNr ++) {
            		int succ = graph.getSuccessorNode(succNr);
            		if(!existing.get(succ) || edgeNotInMecs(succNr)) continue;
            		if(succ == node) {
            			++ num;
            		}// has other succs
            	}
            	if(num >= 1) return true;
            	return false;
            }
    	}
		return true;
    }
    
    private boolean checkMec(BitSet scc) throws EPMCException {
    	// trivial SCCs
    	if(sccSize == 1) {
    		int node = scc.nextSetBit(0);
    		graph.queryNode(node);
    		int numSucc = graph.getNumSuccessors();
            if (numSucc == 0) { // trivial nodes without self-loop
                return false;
            } else { 
            	//need to be more careful, must have at least one self-loop
            	// may have actions ? NO PROBLEM, if can go to itself, action
            	// will be added
            	int num = 0;
            	for(int succNr = 0; succNr < numSucc; succNr ++) {
            		int succ = graph.getSuccessorNode(succNr);
            		if(!existing.get(succ) || edgeNotInMecs(succNr)) continue;
            		if(succ != node) {
            			return false;
            		}// has other succs
            		++ num;
            	}
            	if(num >= 1) return true;
            	return false;
            }
    	}
    	// not trivial SCC
		for(int n = scc.nextSetBit(0); 
				n >= 0; 
				n = scc.nextSetBit(n + 1)) {
			graph.queryNode(n);
			// has other succs, but may need rule out some actions
			// it is ok to have action successor which do not belong to the scc
        	for(int succNr = 0; succNr < graph.getNumSuccessors(); succNr ++) {
        		int succ = graph.getSuccessorNode(succNr);
        		if(!existing.get(succ) || edgeNotInMecs(succNr)) continue;
        		if(!scc.get(succ) && !actions.get(succ)) {
        			return false;
        		}
        	}
		}
		return true;
    }
    
    public void computeSCCs() throws EPMCException {
    	tarjan();
    }

	@Override
	public BitSet next() throws EPMCException {
		if(sccIndex >= sccs.size()) {
			return null;
		}
		++ sccIndex; 
		return sccs.get(sccIndex - 1);
	}
}
