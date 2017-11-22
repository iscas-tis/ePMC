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

import static epmc.error.UtilError.ensure;

import epmc.graph.Semantics;
import epmc.graph.SemanticsLTS;
import epmc.jani.model.Destination;
import epmc.jani.model.Destinations;
import epmc.jani.model.JANINode;
import epmc.jani.model.Location;
import epmc.jani.model.ModelExtensionSemantics;
import epmc.jani.model.ModelJANI;

public class ModelExtensionLTS implements ModelExtensionSemantics {
    public final static String IDENTIFIER = "lts";

    private ModelJANI model;
    private JANINode node;

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public void setModel(ModelJANI model) {
        assert this.model == null;
        assert model != null;
        this.model = model;
    }

    @Override
    public void setNode(JANINode node) {
        this.node = node;
    }

    @Override
    public void parseAfter() {
        if (node instanceof Destinations) {
            Destinations destinations = (Destinations) node;
            ensure(destinations.size() == 1, ProblemsJANILTS.JANI_LTS_ONLY_ONE_DESTINATIONS);
        }
        if (node instanceof Destination) {
            Destination destination = (Destination) node;
            ensure(destination.getProbability() == null, ProblemsJANILTS.JANI_LTS_NO_PROBABILITIES);
        }
        if (node instanceof Location) {
            Location location = (Location) node;
            ensure(location.getTimeProgress() == null, ProblemsJANILTS.JANI_LTS_DISALLOWED_TIME_PROGRESSES);
        }
    }

    @Override
    public Semantics getSemantics() {
        return SemanticsLTS.LTS;
    }
}
