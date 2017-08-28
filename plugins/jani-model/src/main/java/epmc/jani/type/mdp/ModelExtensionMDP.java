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

import static epmc.error.UtilError.ensure;

import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import epmc.graph.Semantics;
import epmc.graph.SemanticsMDP;
import epmc.jani.model.Edge;
import epmc.jani.model.JANINode;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;

public final class ModelExtensionMDP implements ModelExtensionSemantics {
    public final static String IDENTIFIER = "mdp";
    private JANINode node;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModel(ModelJANI model) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setNode(JANINode node) {
        this.node = node;
    }

    @Override
    public void setJsonValue(JsonValue value) {
        // TODO Auto-generated method stub

    }

    @Override
    public void parseBefore() {
        // TODO Auto-generated method stub

    }

    @Override
    public void parseAfter() {
        if (node instanceof Edge) {
            Edge edge = (Edge) node;
            ensure(edge.getRate() == null, ProblemsJANIMDP.JANI_MDP_EDGE_FORBIDS_RATE);
        }
        if (node instanceof Location) {
            Location location = (Location) node;
            ensure(location.getTimeProgress() == null, ProblemsJANIMDP.JANI_MDP_DISALLOWED_TIME_PROGRESSES);
        }
    }

    @Override
    public void generate(JsonObjectBuilder generate) {
        // TODO Auto-generated method stub

    }

    @Override
    public Semantics getSemantics() {
        return SemanticsMDP.MDP;
    }
}
