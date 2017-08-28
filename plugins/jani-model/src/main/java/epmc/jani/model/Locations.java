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

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import epmc.util.UtilJSON;

/**
 * The locations of an automaton.
 * 
 * @author Ernst Moritz Hahn
 */
public final class Locations implements JANINode, Iterable<Location>, Serializable {
    /** 1L, as I don't know any better. */
    private static final long serialVersionUID = 1L;

    /** Map from names of locations to locations. */
    private final Map<String,Location> locations = new LinkedHashMap<>();
    /** Unmodifiable map from names of locations to locations. */
    private final Map<String,Location> locationsExternal = Collections.unmodifiableMap(locations);

    private transient ModelJANI model;

    private Map<String, JANIIdentifier> validVariables;

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
        for (JsonValue locVar : array) {
            Location location = new Location();
            location.setValidIdentifiers(validVariables);
            location.setModel(model);
            location.parse(locVar);
            UtilJSON.ensureUnique(location.getName(), locations);
            locations.put(location.getName(), location);
        }
        return this;
    }

    @Override
    public JsonValue generate() {
        JsonArrayBuilder result = Json.createArrayBuilder();
        for (Location location : locations.values()) {
            result.add(location.generate());
        }
        return result.build();
    }

    /**
     * Obtain a map from location names to locations.
     * The map returned is unmodifiable. The method may only be called after the
     * object has been parsed.
     * 
     * @return map from location names to locations
     */
    public Map<String, Location> getLocations() {
        return locationsExternal;
    }

    @Override
    public Iterator<Location> iterator() {
        return locationsExternal.values().iterator();
    }

    public void add(Location location) {
        this.locations.put(location.getName(), location);
    }

    /**
     * Obtain number of locations.
     * 
     * @return number of locations
     */
    public int size() {
        return locationsExternal.size();
    }

    @Override
    public String toString() {
        return UtilModelParser.toString(this);
    }

    public void setValidIdentifiers(Map<String, JANIIdentifier> validVariables) {
        this.validVariables = validVariables;
    }
}
