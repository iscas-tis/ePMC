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

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.graph.Semantics;
import epmc.graph.SemanticsSMG;
import epmc.jani.model.JANINode;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;
import epmc.jani.model.UtilModelParser;
import epmc.util.UtilJSON;

// TODO not quite clear where to finally put this stuff

public final class ModelExtensionSMG implements ModelExtensionSemantics {
    public final static String IDENTIFIER = "smg";
    private final static String PLAYERS = "players";

    private ModelJANI model;
    private JsonValue value;
    private JANINode node;
    private PlayersJANI players;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Semantics getSemantics() {
        return SemanticsSMG.SMG;
    }

    @Override
    public void setModel(ModelJANI model) {
        this.model = model;
    }

    @Override
    public ModelJANI getModel() {
        return model;
    }

    @Override
    public void setJsonValue(JsonValue value) {
        this.value = value;
    }

    @Override
    public void setNode(JANINode node) {
        this.node = node;
    }

    @Override
    public void parseAfter() {
        if (!(node instanceof ModelJANI)) {
            return;
        }
        ModelJANI model = (ModelJANI) node;
        JsonObject object = UtilJSON.toObject(value);

        players = UtilModelParser.parse(model, () -> {
            PlayersJANI result = new PlayersJANI();
            result.setValidActions(model.getActionsOrEmpty());
            result.setValidAutomata(model.getAutomata());
            return result;
        }, object, PLAYERS);
    }

    public void setPlayers(PlayersJANI players) {
        this.players = players;
    }

    @Override
    public void generate(JsonObjectBuilder generate) {
        assert generate != null;
        generate.add(PLAYERS, players.generate());
    }

    public PlayersJANI getPlayers() {
        return players;
    }
}
