package epmc.jani.explorer;

import java.util.HashSet;
import java.util.Set;

import epmc.error.EPMCException;
import epmc.graph.explorer.Explorer;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.value.TypeBoolean;
import epmc.value.ValueBoolean;

/**
 * Node property stating whether a certain node is an initial node.
 * 
 * @author Ernst Moritz Hahn
 */
final class PropertyNodeInitialNodes implements ExplorerNodeProperty {
	/** Explorer to which this property belongs. */
	private final ExplorerJANI explorer;
	/** Used to return the value of the property. */
	private final ValueBoolean value;
	/** Set of initial nodes of the explorer. */
	private final Set<NodeJANI> initialNodes;

	PropertyNodeInitialNodes(ExplorerJANI explorer) throws EPMCException {
		assert explorer != null;
		this.explorer = explorer;
		this.initialNodes = new HashSet<>(explorer.getInitialNodes());
		TypeBoolean type = TypeBoolean.get(explorer.getContextValue());
		value = type.newValue();
	}
	
	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	@Override
	public ValueBoolean get() throws EPMCException {
		value.set(initialNodes.contains(explorer.getQueriedNode()));
		return value;
	}
	
	@Override
	public boolean getBoolean() throws EPMCException {
		return initialNodes.contains(explorer.getQueriedNode());
	}
	
	@Override
	public TypeBoolean getType() {
		return TypeBoolean.get(explorer.getContextValue());
	}
}
