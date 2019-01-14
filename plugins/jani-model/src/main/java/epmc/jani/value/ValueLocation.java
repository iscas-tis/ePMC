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

import epmc.jani.model.Location;
import epmc.util.BitStream;
import epmc.value.Value;
import epmc.value.ValueAlgebra;
import epmc.value.ValueBitStoreable;
import epmc.value.ValueEnumerable;
import epmc.value.ValueNumBitsKnown;

/**
 * Value storing a location from a set of locations.
 * Locations are mapped to number from zero to the number of locations minus
 * one.
 * 
 * @author Ernst Moritz Hahn
 */
public final class ValueLocation implements ValueEnumerable, ValueNumBitsKnown, ValueBitStoreable, ValueAlgebra {
    /** String used in the {@link #toString()} method. */
    private final static String LOCATION = "location:";

    /** Indicates whether this value is immutable. */
    private boolean immutable;
    /** Type of the value. */
    private TypeLocation type;
    /** Number of bits needed to store this value. */
    private int numBits;
    /** Number of the location represented by this value. */
    private int locationNumber;

    public static boolean is(Value value) {
        return value instanceof ValueLocation;
    }
    
    public static ValueLocation as(Value value) {
        if (is(value)) {
            return (ValueLocation) value;
        } else {
            return null;
        }
    }
    
    /**
     * Constructs a new value.
     * The possible locations are obtained from the type parameter. The type
     * parameter may not be {@code null}.
     * 
     * @param type type of this value
     */
    ValueLocation(TypeLocation type) {
        assert type != null;
        this.type = type;
        numBits = type.getNumBits();
    }

    @Override
    public void read(BitStream reader) {
        assert reader != null;
        locationNumber = 0;
        int marker = 1;
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            if (reader.read()) {
                locationNumber |= marker;
            }
            marker <<= 1;
        }
    }

    @Override
    public void write(BitStream writer) {
        assert writer != null;
        int marker = 1;
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            writer.write((locationNumber & marker) != 0);
            marker <<= 1;
        }
    }

    @Override
    public ValueLocation clone() {
        ValueLocation result = new ValueLocation(type);
        result.immutable = this.immutable;
        result.locationNumber = locationNumber;
        return result;
    }

    @Override
    public TypeLocation getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return locationNumber;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (!(obj instanceof ValueLocation)) {
            return false;
        }
        ValueLocation other = (ValueLocation) obj;
        if (this.type != other.type) {
            return false;
        }
        if (this.locationNumber != other.locationNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(LOCATION);
        result.append(type.getLocation(locationNumber));
        return result.toString();
    }

    /**
     * Set the location of this value.
     * The location parameter may not be {@code null}.
     * 
     * @param location location to set for this value
     */
    public void set(Location location) {
        assert location != null;
        assert type.contains(location) : location + " " + type;
        locationNumber = type.getNumber(location);
    }

    public void set(Object location) {
        set((Location) location);
    }

    @Override
    public int getNumBits() {
        return numBits;
    }

    @Override
    public int getValueNumber() {
        return locationNumber;
    }

    @Override
    public void setValueNumber(int number) {
        assert locationNumber >= 0 : locationNumber;
        assert locationNumber < type.getNumLocations();
        this.locationNumber = number;
    }

    public void set(int value) {
        locationNumber = value;
    }
}
