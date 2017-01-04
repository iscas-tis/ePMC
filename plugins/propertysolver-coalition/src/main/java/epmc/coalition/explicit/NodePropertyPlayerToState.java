package epmc.coalition.explicit;

import epmc.error.EPMCException;
import epmc.graph.Player;
import epmc.graph.explicit.GraphExplicit;
import epmc.graph.explicit.NodeProperty;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

final class NodePropertyPlayerToState implements NodeProperty {
	/** 1L, as I don't know any better. */
	private static final long serialVersionUID = 1L;
	private final GraphExplicit graph;
	private final NodeProperty playerProperty;
	private final ValueBoolean value;

	NodePropertyPlayerToState(GraphExplicit graph, NodeProperty player) {
		assert graph != null;
		assert player != null;
		this.graph = graph;
		this.playerProperty = player;
		this.value = TypeBoolean.get(graph.getContextValue()).newValue();
	}
	
	@Override
	public GraphExplicit getGraph() {
		return graph;
	}

	@Override
	public Value get() throws EPMCException {
		Player player = playerProperty.getEnum();
		value.set(player == Player.ONE || player == Player.TWO);
		return value;
	}

	@Override
	public void set(Value value) throws EPMCException {
		assert value != null;
		assert false;
	}

	@Override
	public Type getType() {
		return value.getType();
	}
}
