package epmc.coalition.explicit;

import java.util.Arrays;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.GraphExplicitProperties;
import epmc.util.BitSet;
import epmc.value.ContextValue;

final class GraphExplicitRestricted implements GraphExplicit {
	private final GraphExplicit original;
	private final BitSet restriction;
	private final int[] substitute;
	private final int maxNumSuccessors;
	private final GraphExplicitProperties properties;
	private int queriedNode = -1;

	GraphExplicitRestricted(GraphExplicit original, BitSet restriction) throws EPMCException {
		assert original != null;
		assert restriction != null;
		this.original = original;
		this.restriction = restriction;
    	int numNodes = original.getNumNodes();
    	int maxNumSuccessors = 0;
    	for (int node = 0; node < numNodes; node++) {
    		original.queryNode(node);
    		maxNumSuccessors = Math.max(maxNumSuccessors, original.getNumSuccessors());
    	}
    	this.maxNumSuccessors = maxNumSuccessors;
    	substitute = new int[numNodes];
    	Arrays.fill(substitute, -1);
    	for (int node = 0; node < numNodes; node++) {
    		int succ = restriction.nextSetBit(maxNumSuccessors * node);
    		substitute[node] = succ % maxNumSuccessors;
    	}
    	properties = new GraphExplicitProperties(this, original.getContextValue());
    	for (Object property : original.getGraphProperties()) {
    		properties.registerGraphProperty(property, original.getGraphProperty(property));
    	}
    	for (Object property : original.getNodeProperties()) {
    		properties.registerNodeProperty(property, new NodePropertyRestricted(this, original.getNodeProperty(property)));
    	}
    	for (Object property : original.getEdgeProperties()) {
    		properties.registerEdgeProperty(property, new EdgePropertyRestricted(this, original.getEdgeProperty(property)));
    	}
	}
	
	@Override
	public ContextValue getContextValue() {
		return original.getContextValue();
	}

	@Override
	public int getNumNodes() {
		return original.getNumNodes();
	}

	@Override
	public BitSet getInitialNodes() {
		return original.getInitialNodes();
	}

	@Override
	public void queryNode(int node) throws EPMCException {
		original.queryNode(node);
		this.queriedNode = node;
	}

	@Override
	public int getQueriedNode() {
		return queriedNode;
	}

	@Override
	public int getNumSuccessors() {
		return original.getNumSuccessors();
	}

	@Override
	public int getSuccessorNode(int successor) {
		boolean valid = this.restriction.get(queriedNode * maxNumSuccessors + successor);
		return original.getSuccessorNode(valid
				? successor
                : substitute[queriedNode]);
	}

	@Override
	public GraphExplicitProperties getProperties() {
		return properties;
	}

	BitSet getRestriction() {
		return restriction;
	}
	
	int[] getSubstitute() {
		return substitute;
	}
	
	int getMaxNumSuccessors() {
		return maxNumSuccessors;
	}

	public int getOrigSuccNumber(int successor) {
		boolean valid = this.restriction.get(queriedNode * maxNumSuccessors + successor);
		return valid ? successor : substitute[queriedNode];
	}
	
	@Override
	public void close() {
	}
}
