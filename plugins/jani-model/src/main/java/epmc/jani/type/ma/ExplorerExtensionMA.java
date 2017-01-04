package epmc.jani.type.ma;

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
import epmc.jani.explorer.PropertyEdgeGeneral;
import epmc.jani.explorer.PropertyNodeGeneral;
import epmc.jani.explorer.UtilExplorer;
import epmc.value.TypeEnum;
import epmc.value.TypeWeight;

public final class ExplorerExtensionMA implements ExplorerExtension {
	public final static String IDENTIFIER = "ma";
	private ExplorerJANI explorer;
	private ExplorerComponent system;
	private PropertyNodeGeneral player;
	private PropertyEdgeGeneral weight;
	private PropertyEdge systemWeight;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setExplorer(ExplorerJANI explorer) throws EPMCException {
		this.explorer = explorer;
		this.system = explorer.getExplorerSystem();
		player = new PropertyNodeGeneral(explorer, TypeEnum.get(explorer.getContextValue(), Player.class));
		weight = new PropertyEdgeGeneral(explorer, TypeWeight.get(explorer.getContextValue()));
		systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
	}
	
	@Override
	public void handleSelfLoop(NodeJANI nodeJANI) {
		player.set(Player.STOCHASTIC);
		/*
		NodeJANI[] successors = explorer.getSuccessors();
		successors[0].set(nodeJANI);
		successors[0].getValue(explorer.getSelfLoopVariable()).set(false);
		*/
	}
	
	@Override
	public void handleNoSuccessors(NodeJANI nodeJANI) {
		player.set(Player.ONE);
		/*
		NodeJANI[] successors = explorer.getSuccessors();
		successors[0].set(nodeJANI);
		successors[0].getValue(explorer.getSelfLoopVariable()).set(true);
		*/
	}
		
	@Override
	public ExplorerEdgeProperty getEdgeProperty(Object property) throws EPMCException {
		if (property == CommonProperties.WEIGHT) {
			return weight;
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
