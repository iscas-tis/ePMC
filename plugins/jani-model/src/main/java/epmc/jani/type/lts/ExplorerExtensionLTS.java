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

package epmc.jani.type.lts;

import epmc.graph.CommonProperties;
import epmc.graph.Player;
import epmc.graph.explorer.ExplorerNodeProperty;
import epmc.jani.explorer.ExplorerExtension;
import epmc.jani.explorer.ExplorerJANI;
import epmc.jani.explorer.NodeJANI;
import epmc.jani.explorer.PropertyNodeGeneral;
import epmc.value.TypeBoolean;
import epmc.value.TypeEnum;

public final class ExplorerExtensionLTS implements ExplorerExtension {
    public final static String IDENTIFIER = "lts";
    private ExplorerJANI explorer;
    private PropertyNodeGeneral state;
    private PropertyNodeGeneral player;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setExplorer(ExplorerJANI explorer) {
        assert this.explorer == null;
        assert explorer != null;
        this.explorer = explorer;
        state = new PropertyNodeGeneral(explorer, TypeBoolean.get());
        state.set(true);
        player = new PropertyNodeGeneral(explorer, TypeEnum.get(Player.class));
        player.set(Player.ONE);
    }

    @Override
    public void handleNoSuccessors(NodeJANI nodeJANI) {
        /*
		NodeJANI[] successors = explorer.getSuccessors();
		successors[0].set(nodeJANI);
         */
    }

    @Override
    public ExplorerNodeProperty getNodeProperty(Object property) {
        if (property == CommonProperties.STATE) {
            return state;
        } else if (property == CommonProperties.PLAYER) {
            return player;
        } else {
            return null;
        }
    }
}
