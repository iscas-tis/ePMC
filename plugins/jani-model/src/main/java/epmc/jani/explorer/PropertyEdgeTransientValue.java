package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.graph.explorer.Explorer;
import epmc.value.Type;
import epmc.value.Value;

public final class PropertyEdgeTransientValue implements PropertyEdge {
	private final ExplorerJANI explorer;
	private final int varNr;
	private final Type type;

	PropertyEdgeTransientValue(ExplorerJANI explorer, int varNr) throws EPMCException {
		assert explorer != null;
		assert varNr >= 0;
		this.explorer = explorer;
		this.varNr = varNr;
		this.type = explorer.getStateVariables().getType(explorer.getStateVariables().getVariables().get(varNr));
	}
	
	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	@Override
	public Value get(int successor) throws EPMCException {
		return explorer.getSuccessorNode(successor).getValue(varNr);
	}

	@Override
	public Type getType() {
		return type;
	}	
}
