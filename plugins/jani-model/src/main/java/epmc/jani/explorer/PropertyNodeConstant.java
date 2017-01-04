package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.graph.explorer.Explorer;
import epmc.value.Type;
import epmc.value.Value;

final class PropertyNodeConstant implements PropertyNode {
	private final ExplorerJANI explorer;
	private final Value value;

	PropertyNodeConstant(ExplorerJANI explorer, Value value) {
		assert explorer != null;
		assert value != null;
		this.explorer = explorer;
		this.value = value;
	}
	
	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	@Override
	public Value get() throws EPMCException {
		return value;
	}

	@Override
	public Type getType() {
		return value.getType();
	}

}
