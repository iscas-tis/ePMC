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

package epmc.jani.type.dtmc;

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
import epmc.value.TypeWeight;
import epmc.value.Value;

public final class ExplorerExtensionDTMC implements ExplorerExtension {
	public final static String IDENTIFIER = "dtmc";
	private ExplorerJANI explorer;
	private ExplorerComponent system;
	private PropertyNodeGeneral player;
	private PropertyEdge systemWeight;
	private NodeJANI[] noNondetHelperNode;
	private boolean allowMulti;
	private Value dtmcSum;
	private Value dtmcAligned;

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void setExplorer(ExplorerJANI explorer) throws EPMCException {
		assert this.explorer == null;
		assert explorer != null;
		this.explorer = explorer;
		this.system = explorer.getExplorerSystem();
		player = new PropertyNodeGeneral(explorer, TypeEnum.get(explorer.getContextValue(), Player.class));
		player.set(Player.STOCHASTIC);
		noNondetHelperNode = new NodeJANI[1];
		noNondetHelperNode[0] = system.newNode();
		systemWeight = system.getEdgeProperty(CommonProperties.WEIGHT);
		allowMulti = explorer.getOptions().getBoolean(OptionsJANIDTMC.JANI_DTMC_ALLOW_MULTI_TRANSITION);
		dtmcSum = TypeWeight.get(explorer.getContextValue()).newValue();
		dtmcAligned = TypeWeight.get(explorer.getContextValue()).newValue();
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
		if (property == CommonProperties.PLAYER) {
			return player;
		} else {
			return null;
		}
	}
	
	@Override
	public ExplorerEdgeProperty getEdgeProperty(Object property) throws EPMCException {
		if (property == CommonProperties.WEIGHT) {
			return systemWeight;
		}
		return null;
	}
	
	@Override
	public void afterQueryAutomaton(ExplorerComponentAutomaton automaton) throws EPMCException {
		assert automaton != null;
		UtilExplorer.checkAutomatonProbabilitySum(automaton);
	}
}
