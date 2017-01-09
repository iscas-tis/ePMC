package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.graph.explorer.Explorer;
import epmc.value.Type;
import epmc.value.Value;

public final class PropertyNodeTransientValue implements PropertyNode {
	private final ExplorerJANI explorer;
	private final int varNr;
	private final Type type;

	PropertyNodeTransientValue(ExplorerJANI explorer, int varNr) throws EPMCException {
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
	public Value get() throws EPMCException {
		return explorer.getQueriedNode().getValue(varNr);
	}

	@Override
	public Type getType() {
		return type;
	}	
}
