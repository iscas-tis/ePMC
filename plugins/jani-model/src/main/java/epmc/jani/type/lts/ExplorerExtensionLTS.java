package epmc.jani.type.lts;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.explorer.NodeJANI;
import epmc.jani.explorer.PropertyNodeGeneral;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;

public final class ExplorerExtensionLTS implements ExplorerExtension {
	public final static String IDENTIFIER = "lts";
	private ExplorerJANI explorer;
	private PropertyNodeGeneral state;
	private PropertyNodeGeneral player;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setExplorer(ExplorerJANI explorer) throws EPMCException {
		assert this.explorer == null;
		assert explorer != null;
		this.explorer = explorer;
		state = new PropertyNodeGeneral(explorer, TypeBoolean.get(explorer.getContextValue()));
		state.set(true);
		player = new PropertyNodeGeneral(explorer, TypeEnum.get(explorer.getContextValue(), Player.class));
		player.set(Player.ONE);
	}
	
	@Override
	public void handleNoSuccessors(NodeJANI nodeJANI) {
		/*
		NodeJANI[] successors = explorer.getSuccessors();
		successors[0].set(nodeJANI);
		*/
	}
	
	@Override
	public ExplorerNodeProperty getNodeProperty(Object property) throws EPMCException {
		if (property == CommonProperties.STATE) {
			return state;
		} else if (property == CommonProperties.PLAYER) {
			return player;
		} else {
			return null;
		}
	}
}
