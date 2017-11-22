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

package epmc.jani.model;

import static epmc.error.UtilError.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.util.UtilJSON;

/**
 * The set of destinations of an {@link Edge}.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Destinations implements JANINode, Iterable<Destination> {
    /** List of variables destinations can potentially assign to. */
    private Map<String, JANIIdentifier> validIdentifiers;
    /** List of locations destinations can potentially move to. */
    private Map<String, Location> validLocations;

    /** List of destinations. */
    private final List<Destination> destinations = new ArrayList<>();;
    /** Unmodifiable list of destinations. */
    private final List<Destination> destinationsExternal = Collections.unmodifiableList(destinations);
    private ModelJANI model;

    /**
     * Set the list if variables which destinations can assign to.
     * This method should be called before the parsing phase with a non-{@code
     * null} parameter.
     */
    public void setValidIdentifiers(Map<String,JANIIdentifier> variables) {
        this.validIdentifiers = variables;
    }

    public void setValidLocations(Map<String,Location> locations) {
        this.validLocations = locations;
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
    public JANINode parse(JsonValue value) {
        assert model != null;
        assert value != null;
        JsonArray array = UtilJSON.toArray(value);
        ensure(!array.isEmpty(), ProblemsJANIParser.JANI_PARSER_DESTINATIONS_NOT_EMPTY);
        for (JsonValue destValue : array) {
            Destination destination = new Destination();
            destination.setValidLocations(validLocations);
            destination.setValidIdentifiers(validIdentifiers);
            destination.setModel(model);
            destination.parse(destValue);
            destinations.add(destination);
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (Destination destination : destinations) {
            result.add(destination.generate());
        }
        return result.build();
    }

    @Override
    public Iterator<Destination> iterator() {
        return destinationsExternal.iterator();
    }

    public int size() {
        return destinations.size();
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    public void addDestination(Destination destination) {
        destinations.add(destination);
    }
}
