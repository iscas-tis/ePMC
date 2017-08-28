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

package epmc.jani.type.ctmdp;

import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explorer.ExplorerEdgeProperty;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.jani.explorer.ExplorerComponentAutomaton;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.explorer.NodeJANI;
import epmc.jani.explorer.PropertyEdgeGeneral;
import epmc.jani.explorer.PropertyNodeGeneral;
import epmc.jani.explorer.UtilExplorer;
import epmc.value.TypeEnum;
import epmc.value.TypeWeight;

public final class ExplorerExtensionCTMDP implements ExplorerExtension {
    public final static String IDENTIFIER = "ctmdp";
    private ExplorerJANI explorer;
    private PropertyNodeGeneral player;
    private PropertyEdgeGeneral weight;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setExplorer(ExplorerJANI explorer) {
        this.explorer = explorer;
        player = new PropertyNodeGeneral(explorer, TypeEnum.get(Player.class));
        weight = new PropertyEdgeGeneral(explorer, TypeWeight.get());
    }

    @Override
    public void handleSelfLoop(NodeJANI nodeJANI) {
        player.set(Player.STOCHASTIC);
        /*
		NodeJANI[] successors = explorer.getSuccessors();
		successors[0].set(nodeJANI);
		successors[0].getValue(explorer.getSelfLoopVariable()).set(false);
		successors[0].unmark();
         */
    }

    @Override
    public void handleNoSuccessors(NodeJANI nodeJANI) {
        player.set(Player.ONE);
        /*
		NodeJANI[] successors = explorer.getSuccessors();
		successors[0].set(nodeJANI);
		successors[0].getValue(explorer.getSelfLoopVariable()).set(true);
		successors[0].unmark();
         */
    }

    @Override
    public ExplorerEdgeProperty getEdgeProperty(Object property) {
        if (property == CommonProperties.WEIGHT) {
            return weight;
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
