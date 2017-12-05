/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.jani.type.smg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import epmc.expression.Expression;
import epmc.expression.standard.ExpressionIdentifier;
import epmc.expression.standard.ExpressionIdentifierStandard;
import epmc.expression.standard.ExpressionLiteral;
import epmc.expression.standard.SMGPlayer;
import epmc.expression.standard.evaluatorexplicit.UtilEvaluatorExplicit;
import epmc.graph.CommonProperties;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.jani.explorer.ExplorerComponent;
import epmc.jani.explorer.ExplorerComponentAutomaton;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.explorer.NodeJANI;
import epmc.jani.explorer.PropertyEdgeAction;
import epmc.jani.explorer.UtilExplorer;
import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.ModelExtension;
import epmc.value.Value;
import epmc.value.ValueInteger;

public final class ExplorerExtensionSMG implements ExplorerExtension {
    public final static String IDENTIFIER = "smg";

    private int[] actionToPlayer;
    private int[] automatonToPlayer;
    private int activeAutomation;
    private ExplorerComponent system;
    private PropertyEdgeAction actions;
    private PropertyNodeSMGPlayer[] numberToPlayer;
    private Map<String,PropertyNodeSMGPlayer> nameToPlayer = new HashMap<>();

    private int nodePlayer;

    private ModelExtension modelExtension;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModelExtension(ModelExtension modelExtension) {
        assert modelExtension instanceof ModelExtensionSMG;
        this.modelExtension = modelExtension;
    }

    private PropertyNodeSMGPlayer[] buildPlayers(ExplorerComponent system, PlayersJANI players) {
        PropertyNodeSMGPlayer[] numberToPlayer = new PropertyNodeSMGPlayer[players.size()];
        for (int playerNr = 0; playerNr < players.size(); playerNr++) {
            PlayerJANI player = players.get(playerNr);
            PropertyNodeSMGPlayer property = new PropertyNodeSMGPlayer(system.getExplorer(), this, playerNr);
            nameToPlayer.put(player.getName(), property);
            numberToPlayer[playerNr] = property;
        }
        return numberToPlayer;
    }

    @Override
    public void setExplorer(ExplorerJANI explorer) {
        Map<Action, Integer> actionToNumber = UtilExplorer.computeActionToInteger(modelExtension.getModel());
        actionToPlayer = new int[actionToNumber.size()];
        automatonToPlayer = new int[modelExtension.getModel().getAutomata().size()];
        Arrays.fill(actionToPlayer, -1);
        Arrays.fill(automatonToPlayer, -1);
        system = explorer.getExplorerSystem();
        actions = (PropertyEdgeAction) system.getEdgeProperty(CommonProperties.TRANSITION_LABEL);
        ModelExtensionSMG modelExtensionSMG = (ModelExtensionSMG) modelExtension;
        PlayersJANI players = modelExtensionSMG.getPlayers();
        int playerNr = 0;
        for (PlayerJANI player : players) {
            for (Action action : player.getActionsOrEmpty()) {
                int actionNr = actionToNumber.get(action);
                actionToPlayer[actionNr] = playerNr;
            }
            for (Automaton automaton : player.getAutomataOrEmpty()) {
                assert automaton != null;
                int automatonNr = automaton.getNumber();
                automatonToPlayer[automatonNr] = playerNr;
            }
            playerNr++;
        }
        this.numberToPlayer = buildPlayers(system, players);
    }

    @Override
    public void beforeQuerySystem(NodeJANI nodeJANI) {
        activeAutomation = -1;
    }

    @Override
    public void afterQuerySystem(NodeJANI node) {
        int nodePlayer = -1;
        if (system.isState()) {
            int numSuccessors = system.getNumSuccessors();
            for (int succNr = 0; succNr < numSuccessors; succNr++) {
                // TODO CHECK
                int action = actions.getInt(succNr);
                int actionPlayer = actionToPlayer[action];
                if (actionPlayer == -1) {
                    nodePlayer = automatonToPlayer[activeAutomation];
                } else {
                    // TODO check non-agreeing player transitions
                    nodePlayer = actionPlayer;
                }
                if (nodePlayer != -1) {
                    break;
                }
            }
        }
        this.nodePlayer = nodePlayer;
    }

    @Override
    public void afterQueryAutomaton(ExplorerComponentAutomaton automaton) {
        if (!automaton.isState()) {
            return;
        }
        int numSuccessors = automaton.getNumSuccessors();
        PropertyEdgeAction autActions = automaton.getActions();
        for (int succNr = 0; succNr < numSuccessors; succNr++) {
            if (this.actionToPlayer[autActions.getInt(succNr)] == -1) {
                activeAutomation = automaton.getNumber();
                break;
            }
        }
    }

    public int getNodePlayer() {
        return nodePlayer;
    }

    @Override
    public ExplorerNodeProperty getNodeProperty(Object property) {
        if (property instanceof SMGPlayer) {
            return getPRISMGamesPlayer((SMGPlayer) property);
        }
        return null;
    }

    private PropertyNodeSMGPlayer getPRISMGamesPlayer(SMGPlayer player) {
        assert player != null;
        Expression expression = player.getExpression();
        assert expression != null;
        assert expression instanceof ExpressionIdentifier
        || expression instanceof ExpressionLiteral;
        if (ExpressionLiteral.is(expression)) {
            ExpressionLiteral expressionLiteral = (ExpressionLiteral) expression;
            Value value = UtilEvaluatorExplicit.evaluate(expressionLiteral);
            assert ValueInteger.is(value);
            int intValue = ValueInteger.as(value).getInt() - 1;
            assert intValue >= 0 : intValue;
            assert intValue < numberToPlayer.length;
            return numberToPlayer[intValue];
        } else {
            ExpressionIdentifierStandard expressionIdentifier = (ExpressionIdentifierStandard) expression;
            String name = expressionIdentifier.getName();
            assert nameToPlayer.containsKey(name);
            return nameToPlayer.get(name);
        }
    }

    @Override
    public ExplorerEdgeProperty getEdgeProperty(Object property) {
        if (property == CommonProperties.WEIGHT) {
            return system.getEdgeProperty(CommonProperties.WEIGHT);
        }
        return null;
    }
}
