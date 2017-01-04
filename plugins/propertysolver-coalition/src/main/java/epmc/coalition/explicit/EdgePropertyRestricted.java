package epmc.coalition.explicit;

import epmc.error.EPMCException;
import epmc.graph.explicit.EdgeProperty;
import epmc.graph.explicit.GraphExplicit;
import epmc.util.BitSet;
import epmc.value.Type;
import epmc.value.Value;

final class EdgePropertyRestricted implements EdgeProperty {
	/** 1L, as I don't know any better. */
	private static final long serialVersionUID = 1L;
	private final GraphExplicitRestricted graph;
	private final EdgeProperty original;
	private final BitSet restriction;
	private final int[] substitute;
	private final int maxNumSuccessors;

	EdgePropertyRestricted(GraphExplicitRestricted graph, EdgeProperty original) {
		assert graph != null;
		assert original != null;
		this.graph = graph;
		this.original = original;
		this.restriction = graph.getRestriction();
		this.substitute = graph.getSubstitute();
		this.maxNumSuccessors = graph.getMaxNumSuccessors();
	}
	
	@Override
	public GraphExplicit getGraph() {
		return graph;
	}

	@Override
	public Value get(int successor) throws EPMCException {
		int queriedNode = graph.getQueriedNode();
		boolean valid = restriction.get(queriedNode * maxNumSuccessors + successor);
		return original.get(valid ? successor : substitute[queriedNode]);
	}

	@Override
	public void set(Value value, int successor) {
		assert false;
	}

	@Override
	public Type getType() {
		return original.getType();
	}

}
