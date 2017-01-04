package epmc.jani.type.smg;

import epmc.error.EPMCException;
import epmc.graph.explorer.Explorer;
import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.explorer.PropertyNode;
import epmc.value.Type;
import epmc.value.TypeBoolean;
import epmc.value.Value;
import epmc.value.ValueBoolean;

public final class PropertyNodeSMGPlayer implements PropertyNode {
	private final static String PLAYER = "PLAYER";
	
	private final ExplorerJANI explorer;
	private final ExplorerExtensionSMG extension;
	private final int player;
	private final TypeBoolean type;
	private final ValueBoolean value;

	public PropertyNodeSMGPlayer(ExplorerJANI explorer, ExplorerExtensionSMG extension, int player) {
		this.explorer = explorer;
		this.extension = extension;
		this.player = player;
		this.type = TypeBoolean.get(explorer.getContextValue());
		this.value = type.newValue();
	}
	
	@Override
	public Explorer getExplorer() {
		return explorer;
	}

	@Override
	public Value get() throws EPMCException {
		value.set(extension.getNodePlayer() == player);
		return value;
	}

	@Override
	public boolean getBoolean() throws EPMCException {
		return extension.getNodePlayer() == player;
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(PLAYER);
		builder.append(player + 1);
		return builder.toString();
	}
}
