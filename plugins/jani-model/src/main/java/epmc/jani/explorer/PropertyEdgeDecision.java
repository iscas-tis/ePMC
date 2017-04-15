package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.value.Type;
import epmc.value.Value;

public final class PropertyEdgeDecision implements ExplorerEdgeProperty {
//	private final ExplorerJANI explorer;

	PropertyEdgeDecision(ExplorerJANI explorer) {
		assert explorer != null;
	//	this.explorer = explorer;
	}
	
	@Override
	public Value get(int successor) throws EPMCException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

}
