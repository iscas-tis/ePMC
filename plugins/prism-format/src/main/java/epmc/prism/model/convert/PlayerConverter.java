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

package epmc.prism.model.convert;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import epmc.graph.SemanticsSMG;
import epmc.jani.model.Action;
import epmc.jani.model.Automaton;
import epmc.jani.model.ModelJANI;
import epmc.jani.type.smg.ModelExtensionSMG;
import epmc.jani.type.smg.PlayerJANI;
import epmc.jani.type.smg.PlayersJANI;
import epmc.prism.model.ModelPRISM;
import epmc.prism.model.PlayerDefinition;

final class PlayerConverter {
    private ModelPRISM modelPRISM;
    private ModelJANI modelJANI;

    void setPRISMModel(ModelPRISM modelPrism) {
        this.modelPRISM = modelPrism;
    }

    void setJANIModel(ModelJANI modelJani) {
        this.modelJANI = modelJani;
    }

    void attachPlayers() {
        if (!SemanticsSMG.isSMG(modelPRISM.getSemantics())) {
            return;
        }
        PlayersJANI players = convertPlayers(modelPRISM.getPlayers());
        ModelExtensionSMG extension = (ModelExtensionSMG) modelJANI.getSemanticsExtension();
        extension.setPlayers(players);
    }

    private PlayersJANI convertPlayers(List<PlayerDefinition> playersPRISM) {
        PlayersJANI players = new PlayersJANI();
        players.setModel(modelJANI);
        for (PlayerDefinition playerPRISM : playersPRISM) {
            PlayerJANI playerJANI = new PlayerJANI();
            playerJANI.setModel(modelJANI);
            playerJANI.setName(playerPRISM.getName());
            Set<Action> actions = new HashSet<>();
            for (String actionName : playerPRISM.getLabels()) {
                actions.add(modelJANI.getActions().get(actionName));
            }
            playerJANI.setActions(actions);
            Set<Automaton> automata = new HashSet<>();
            for (String moduleName : playerPRISM.getModules()) {
                Automaton automaton = modelJANI.getAutomata().get(moduleName);
                assert automaton != null : moduleName + " " + modelJANI.getAutomata().getAutomata().keySet();
                automata.add(automaton);
            }
            playerJANI.setAutomata(automata);
            players.add(playerJANI);
        }
        return players;
    }

}
