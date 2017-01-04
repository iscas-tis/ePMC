package epmc.coalition.explicit;

import epmc.error.EPMCException;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.value.Type;
import epmc.value.Value;

final class NodePropertyRestricted implements NodeProperty {
	/** 1L, as I don't know any better. */
	private static final long serialVersionUID = 1L;
	private final GraphExplicitRestricted graph;
	private final NodeProperty original;

	NodePropertyRestricted(GraphExplicitRestricted graph, NodeProperty original) {
		assert graph != null;
		assert original != null;
		this.graph = graph;
		this.original = original;
	}
	
	@Override
	public GraphExplicit getGraph() {
		return graph;
	}

	@Override
	public Value get() throws EPMCException {
		return original.get();
	}

	@Override
	public void set(Value value) throws EPMCException {
		assert false;
	}

	@Override
	public Type getType() {
		return original.getType();
	}
}
