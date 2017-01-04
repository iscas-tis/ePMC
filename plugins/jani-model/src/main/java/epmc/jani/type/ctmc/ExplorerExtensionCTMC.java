package epmc.jani.type.ctmc;

import epmc.error.EPMCException;
import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.jani.explorer.ExplorerComponent;
import epmc.jani.explorer.ExplorerComponentAutomaton;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.explorer.NodeJANI;
import epmc.jani.explorer.PropertyEdge;
import epmc.jani.explorer.PropertyNodeGeneral;
import epmc.jani.explorer.UtilExplorer;
import epmc.value.TypeEnum;

public final class ExplorerExtensionCTMC implements ExplorerExtension {
	public final static String IDENTIFIER = "ctmc";
	private ExplorerJANI explorer;
	private ExplorerComponent system;
	private PropertyNodeGeneral player;
	private NodeJANI[] noNondetHelperNode;
	private PropertyEdge systemWeight;
	private boolean allowMulti;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setExplorer(ExplorerJANI explorer) throws EPMCException {
		this.explorer = explorer;
		system = explorer.getExplorerSystem();
		player = new PropertyNodeGeneral(explorer, TypeEnum.get(explorer.getContextValue(), Player.class));
		player.set(Player.STOCHASTIC);
		noNondetHelperNode = new NodeJANI[1];
		noNondetHelperNode[0] = system.newNode();
		systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
		systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
		allowMulti = explorer.getOptions().getBoolean(OptionsJANICTMC.JANI_CTMC_ALLOW_MULTI_TRANSITION);
	}
	
	@Override
	public ExplorerEdgeProperty getEdgeProperty(Object property) throws EPMCException {
		if (property == CommonProperties.WEIGHT) {
			return systemWeight;
		}
		return null;
	}
	
	@Override
	public ExplorerNodeProperty getNodeProperty(Object property) throws EPMCException {
		if (property == CommonProperties.PLAYER) {
			return player;
		} else {
			return null;
		}
	}
	
	@Override
	public void afterQueryAutomaton(ExplorerComponentAutomaton automaton) throws EPMCException {
		assert automaton != null;
		UtilExplorer.checkAutomatonProbabilitySum(automaton);
	}
}
