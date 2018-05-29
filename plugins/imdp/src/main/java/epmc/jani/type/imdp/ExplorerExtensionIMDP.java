package epmc.jani.type.imdp;

import static epmc.error.UtilError.ensure;

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
import epmc.jani.explorer.PropertyEdgeDecision;
import epmc.jani.explorer.PropertyNodeGeneral;
import epmc.operator.OperatorGe;
import epmc.operator.OperatorLe;
import epmc.value.ContextValue;
import epmc.value.OperatorEvaluator;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;
import epmc.value.TypeReal;
import epmc.value.UtilValue;
import epmc.value.Value;
import epmc.value.ValueBoolean;
import epmc.value.ValueInterval;

public final class ExplorerExtensionIMDP implements ExplorerExtension {
    public final static String IDENTIFIER = "imdp";
    private PropertyNodeGeneral player;
    private PropertyNodeGeneral systemState;
    private PropertyEdge systemWeight;
    private ExplorerComponent system;
    private Value realZero;
    private Value realOne;
    private ExplorerJANI explorer;
    private PropertyEdgeDecision decision;
    private OperatorEvaluator le;
    private OperatorEvaluator ge;
    private ValueBoolean cmpLeL;
    private ValueBoolean cmpGeL;
    private ValueBoolean cmpGeU;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setExplorer(ExplorerJANI explorer) {
        player = new PropertyNodeGeneral(explorer, TypeEnum.get(Player.class));
        system = explorer.getExplorerSystem();
        systemState = (PropertyNodeGeneral) system.getNodeProperty(CommonProperties.STATE);
        systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
        this.realZero = UtilValue.newValue(TypeReal.get(), 0);
        this.realOne = UtilValue.newValue(TypeReal.get(), 1);
        this.explorer = explorer;
        le = ContextValue.get().getEvaluator(OperatorLe.LE, TypeReal.get(), TypeReal.get());
        ge = ContextValue.get().getEvaluator(OperatorGe.GE, TypeReal.get(), TypeReal.get());
        cmpLeL = TypeBoolean.get().newValue();
        cmpGeL = TypeBoolean.get().newValue();
        cmpGeU = TypeBoolean.get().newValue();
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
    public ExplorerEdgeProperty getEdgeProperty(Object property) {
        if (property == CommonProperties.WEIGHT) {
            return systemWeight;
        }
        if (property == CommonProperties.DECISION) {
            if (decision == null) {
                decision = new PropertyEdgeDecision(explorer);
            }
            return decision;
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
    public void afterQuerySystem(NodeJANI node) {
        player.set(systemState.getBoolean() ? Player.ONE : Player.STOCHASTIC);
    }

    @Override
    public void afterQueryAutomaton(ExplorerComponentAutomaton automaton) {
        assert automaton != null;
        if (automaton.isState()) {
            return;
        }
        int numSuccessors = automaton.getNumSuccessors();
        ValueInterval probabilitySum = ValueInterval.as(automaton.getProbabilitySum());
        ge.apply(cmpGeL, probabilitySum.getIntervalLower(), realZero);
        ge.apply(cmpGeU,  probabilitySum.getIntervalUpper(), realOne);
        le.apply(cmpLeL, probabilitySum.getIntervalLower(), realOne);
        ensure(numSuccessors == 0
                || (cmpGeL.getBoolean()
                        && cmpLeL.getBoolean()
                        && cmpGeU.getBoolean()),
                ProblemsJANIExplorer.JANI_EXPLORER_PROBABILIY_SUM_NOT_ONE,
                probabilitySum);
    }
}
