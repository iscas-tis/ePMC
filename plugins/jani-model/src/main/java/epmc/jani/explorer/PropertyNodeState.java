package epmc.jani.explorer;

import epmc.error.EPMCException;
import epmc.graph.explorer.Explorer;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class PropertyNodeState implements PropertyNode {
	/** The explorer to which this property belongs. */
	private final ExplorerJANI explorer;
	/** Type of value stored. */
	private final TypeBoolean type;
	/** Value of this property for the node queried last. */
	private final ValueBoolean value;

	/**
	 * Construct new node property.
	 * None of the parameters may be {@code null}.
	 * 
	 * @param explorer explorer to which the property shall belong to
	 * @param type type of the property
	 */
	public PropertyNodeState(ExplorerJANI explorer) {
		assert explorer != null;
		this.explorer = explorer;
		this.type = TypeBoolean.get(explorer.getContextValue());
		this.value = type.newValue();
	}

	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	@Override
	public Value get() throws EPMCException {
		value.set(explorer.isState());
		return value;
	}

	@Override
	public boolean getBoolean() throws EPMCException {
		return explorer.isState();
	}
	
	@Override
	public Type getType() {
		return type;
	}
}
