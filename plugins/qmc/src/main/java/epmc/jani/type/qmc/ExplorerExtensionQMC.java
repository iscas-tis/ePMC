package epmc.jani.type.qmc;

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
import epmc.value.TypeEnum;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;

public final class ExplorerExtensionQMC implements ExplorerExtension {
    public final static String IDENTIFIER = "qmc";
    private ExplorerJANI explorer;
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
    public void setExplorer(ExplorerJANI explorer) {
        this.explorer = explorer;
    }

    @Override
    public void afterSystemCreation() {
        player = new PropertyNodeGeneral(explorer, TypeEnum.get(Player.class));
        player.set(Player.ONE);
        system = explorer.getExplorerSystem();
        systemState = (PropertyNodeGeneral) system.getNodeProperty(CommonProperties.STATE);
        systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
        this.realZero = UtilValue.newValue(TypeReal.get(), 0);
        this.realOne = UtilValue.newValue(TypeReal.get(), 1);
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
    public ExplorerEdgeProperty getEdgeProperty(Object property) {
        if (property == CommonProperties.WEIGHT) {
            return systemWeight;
        }
        return null;
    }

    @Override
    public ExplorerNodeProperty getNodeProperty(Object property) {
        if (property == CommonProperties.PLAYER) {
            return player;
        } else {
            return null;
        }
    }

    @Override
    public void afterQueryAutomaton(ExplorerComponentAutomaton automaton) {
        assert automaton != null;
        int numSuccessors = automaton.getNumSuccessors();
        Value probabilitySum = automaton.getProbabilitySum();
        /*
		ensure(numSuccessors == 0
				|| (probabilitySum.getIntervalLower().isGe(realZero)
						&& probabilitySum.getIntervalLower().isLe(realOne)
						&& probabilitySum.getIntervalUpper().isGe(realOne)),
				ProblemsJANIExplorer.JANI_EXPLORER_PROBABILIY_SUM_NOT_ONE,
				probabilitySum);
         */
    }
}
