package epmc.jani.type.imdp;

import static epmc.error.UtilError.ensure;

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
import epmc.jani.explorer.ProblemsJANIExplorer;
import epmc.jani.explorer.PropertyEdge;
import epmc.jani.explorer.PropertyNodeGeneral;
import epmc.value.TypeEnum;
import epmc.value.TypeReal;
import epmc.value.Value;
import epmc.value.ValueInterval;

public final class ExplorerExtensionIMDP implements ExplorerExtension {
	public final static String IDENTIFIER = "imdp";
	private PropertyNodeGeneral player;
	private PropertyNodeGeneral systemState;
	private PropertyEdge systemWeight;
	private ExplorerComponent system;
	private Value realZero;
	private Value realOne;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setExplorer(ExplorerJANI explorer) throws EPMCException {
		player = new PropertyNodeGeneral(explorer, TypeEnum.get(explorer.getContextValue(), Player.class));
		system = explorer.getExplorerSystem();
		systemState = (PropertyNodeGeneral) system.getNodeProperty(CommonProperties.STATE);
		systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
		this.realZero = TypeReal.get(explorer.getContextValue()).getZero();
		this.realOne = TypeReal.get(explorer.getContextValue()).getOne();
	}
	
	@Override
	public void handleSelfLoop(NodeJANI nodeJANI) {
		player.set(Player.STOCHASTIC);
		/*
		explorer.setNumSuccessors(1);
		NodeJANI[] successors = explorer.getSuccessors();
		successors[0].set(nodeJANI);
		successors[0].getValue(explorer.getSelfLoopVariable()).set(false);
		*/
	}
	
	@Override
	public void handleNoSuccessors(NodeJANI nodeJANI) {
		player.set(Player.ONE);
//		system.setNumSuccessors(1);
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
	public void afterQuerySystem(NodeJANI node) throws EPMCException {
		player.set(systemState.getBoolean() ? Player.ONE : Player.STOCHASTIC);
	}

	@Override
	public void afterQueryAutomaton(ExplorerComponentAutomaton automaton) throws EPMCException {
		assert automaton != null;
		if (automaton.isState()) {
			return;
		}
		int numSuccessors = automaton.getNumSuccessors();
		ValueInterval probabilitySum = ValueInterval.asInterval(automaton.getProbabilitySum());
		ensure(numSuccessors == 0
				|| (probabilitySum.getIntervalLower().isGe(realZero)
						&& probabilitySum.getIntervalLower().isLe(realOne)
						&& probabilitySum.getIntervalUpper().isGe(realOne)),
				ProblemsJANIExplorer.JANI_EXPLORER_PROBABILIY_SUM_NOT_ONE,
				probabilitySum);
	}
}
