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

package epmc.jani.type.mdp;

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
import epmc.jani.explorer.PropertyEdgeDecision;
import epmc.jani.explorer.PropertyNodeGeneral;
import epmc.jani.explorer.UtilExplorer;
import epmc.value.TypeEnum;

public final class ExplorerExtensionMDP implements ExplorerExtension {
    public final static String IDENTIFIER = "mdp";
    private PropertyNodeGeneral player;
    private PropertyNodeGeneral systemState;
    private PropertyEdge systemWeight;
    private PropertyEdgeDecision decision;
    private ExplorerComponent system;
    private ExplorerJANI explorer;

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
        system = explorer.getExplorerSystem();
        systemState = (PropertyNodeGeneral) system.getNodeProperty(CommonProperties.STATE);
        systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
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
    public void afterQuerySystem(NodeJANI node) {
        player.set(systemState.getBoolean() ? Player.ONE : Player.STOCHASTIC);
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
    public void afterQueryAutomaton(ExplorerComponentAutomaton automaton) {
        assert automaton != null;
        UtilExplorer.checkAutomatonProbabilitySum(automaton);
    }
}
