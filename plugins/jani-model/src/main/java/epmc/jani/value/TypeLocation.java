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

package epmc.jani.value;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import epmc.jani.model.Location;
import epmc.jani.model.Locations;
import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeAlgebra;
import epmc.value.TypeArrayAlgebra;
import epmc.value.TypeEnumerable;
import epmc.value.TypeNumBitsKnown;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * Type to generate values storing a location from a set of locations.
 * Locations are mapped to number from zero to the number of locations minus
 * one.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TypeLocation implements TypeEnumerable, TypeNumBitsKnown, TypeAlgebra {
    public static TypeLocation get(Locations locations) {
        assert locations != null;
        TypeLocation type = new TypeLocation(locations);
        return ContextValue.get().makeUnique(type);
    }

    public static boolean is(Type type) {
        return type instanceof TypeLocation;
    }
    
    public static TypeLocation as(Type type) {
        if (is(type)) {
            return (TypeLocation) type;
        } else {
            return null;
        }
    }
    
    /** String used for the {@link #toString()} method. */
    private final static String LOCATION = "location";

    /** Number of bits used to store a value of this type. */
    private int numBits;
    /** Set of locations which this type represents. */
    //	private Locations locations;
    /** Map to enumerate locations. */
    private final Object2IntOpenHashMap<String> locationToNumber = new Object2IntOpenHashMap<>();
    /** Maps a number to corresponding location. */
    private final String[] numberToLocation;
    private final List<String> locations;

    TypeLocation(Locations locations) {
        this(locationsToStringList(locations));
    }

    /**
     * Generate a new location storing type.
     * None of the parameters may be {@code null}.
     * @param locations set of locations
     */
    TypeLocation(List<String> locations) {
        assert locations != null;
        this.numberToLocation = new String[locations.size()];
        int locNr = 0;
        for (String location : locations) {
            locationToNumber.put(location, locNr);
            numberToLocation[locNr] = location;
            locNr++;
        }
        this.locations = locations;
        numBits = Integer.SIZE - Integer.numberOfLeadingZeros(locations.size() - 1);
    }

    private static List<String> locationsToStringList(Locations locations) {
        assert locations != null;
        List<String> result = new ArrayList<>();
        for (Location location : locations) {
            result.add(location.getName());
        }
        return result;
    }

    @Override
    public ValueLocation newValue() {
        return new ValueLocation(this);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash = locations.hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof TypeLocation)) {
            return false;
        }
        TypeLocation other = (TypeLocation) obj;
        if (!this.locations.equals(other.locations)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(LOCATION);
        Set<String> locationToNumber = new LinkedHashSet<>();
        for (String location : numberToLocation) {
            locationToNumber.add(location);
        }
        result.append(locationToNumber);
        return result.toString();
    }

    @Override
    public int getNumBits() {
        return numBits;
    }

    /**
     * Get location by its number.
     * The parameter must be a valid location number.
     * 
     * @param locationNumber number of the location
     * @return location with this number
     */
    public String getLocation(int locationNumber) {
        assert locationNumber >= 0;
        assert locationNumber < numberToLocation.length;
        return numberToLocation[locationNumber];
    }

    /**
     * Get the number of a location.
     * The parameter may not be {@code null}.
     * 
     * @param location location of which to get the number
     * @return number of the location
     */
    public int getNumber(Location location) {
        assert location != null;
        assert locationToNumber.containsKey(location.getName());
        return locationToNumber.getInt(location.getName());
    }

    /**
     * Check whether a location is contained in the locations of this type.
     * The location parameter may not be {@code null}.
     * 
     * @param location location of which to check whether it is contained
     * @return {@code true} if contained, else {@code false}
     */
    public boolean contains(Location location) {
        assert location != null;
        return locationToNumber.containsKey(location.getName());
    }

    /**
     * Generate a new value representing the given location.
     * The given location may not be {@code null}.
     * 
     * @param location location to generate value of
     * @return value representing the given location
     */
    public ValueLocation newValue(Location location) {
        assert location != null;
        assert locationToNumber.containsKey(location);
        ValueLocation result = newValue();
        result.set(location);
        return result;
    }

    /**
     * Obtain the number of possible locations of this type.
     * 
     * @return number of locations of this type
     */
    int getNumLocations() {
        return locationToNumber.size();
    }

    @Override
    public int getNumValues() {
        return locations.size();
    }

    @Override
    public TypeArrayAlgebra getTypeArray() {
        // TODO
        return null;
//        return ContextValue.get().makeUnique(new TypeArrayGeneric(this));
    }
}
