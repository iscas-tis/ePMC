package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.value.Type;
import epmc.value.Value;

public final class PropertyEdgeDecision implements ExplorerEdgeProperty {
	private final ExplorerJANI explorer;
	private final ValueJANIDecision value;

	public PropertyEdgeDecision(ExplorerJANI explorer) {
		assert explorer != null;
		this.explorer = explorer;
		TypeJANIDecision type = new TypeJANIDecision(explorer);
		value = type.newValue();
	}
	
	@Override
	public Value get(int successor) throws EPMCException {
		NodeJANI node = explorer.getSuccessorNode(successor);
		value.set(node);
		return value;
	}

	@Override
	public Type getType() {
		return value.getType();
	}
}
