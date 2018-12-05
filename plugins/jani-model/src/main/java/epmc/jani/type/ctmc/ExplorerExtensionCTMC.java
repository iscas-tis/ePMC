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

package epmc.jani.type.ctmc;

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
import epmc.options.Options;
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
    public void setExplorer(ExplorerJANI explorer) {
        this.explorer = explorer;
    }

    @Override
    public void afterSystemCreation() {
        system = explorer.getExplorerSystem();
        player = new PropertyNodeGeneral(explorer, TypeEnum.get(Player.class));
        player.set(Player.STOCHASTIC);
        noNondetHelperNode = new NodeJANI[1];
        noNondetHelperNode[0] = system.newNode();
        systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
        systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
        allowMulti = Options.get().getBoolean(OptionsJANICTMC.JANI_CTMC_ALLOW_MULTI_TRANSITION);
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
        UtilExplorer.checkAutomatonProbabilitySum(automaton);
    }
}
