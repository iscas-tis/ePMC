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

package epmc.value;

import epmc.error.EPMCException;

// TODO should be divided into two interfaces (not classes). One which is just
// TODO for one-dimensional arrays. One which allows multi-dimensional arrays.
// TODO For the multi-dimensional, we should provide a default implementation
// TODO wrapping around the single-dimensional array. It is only needed for JANI
// TODO and QMC at the moment anyway. Having one-dimensional arrays as
// TODO interfaces allows for a cleaner implementation in the plugins.

/**
 * {@link Value} storing multiple {@link Value}s of given {@link Type}.
 * In principle, multiple values could just be stored as Java arrays.
 * Using a subinterface of {@link Value} instead of Java arrays has the
 * advantage of flexibility, such that for instance
 * <ul>
 * <li>they can use specialised data structures to be more memory-efficient,
 * </li>
 * <li>they can be stored in native memory for preparing calls to native
 * code,</li>
 * <li>or in the hard-disk to safe memory,</li>
 * <li>while they can still be created using the same API as arrays in Java
 * memory.</li>
 * </ul>
 * 
 * @author Ernst Moritz Hahn
 */
public abstract class ValueArray implements Value {
    /**
     * Checks whether given value is an array value.
     * 
     * @param value value for which to check whether it is an array value
     * @return whether given value is an array value
     */
    public static boolean isArray(Value value) {
        return value instanceof ValueArray;
    }
    
    /**
     * Cast given value to array type.
     * If the type is not an array value, {@code null} will be returned.
     * 
     * @param value value to cast to array type
     * @return value casted to array value, or {@null} if not possible to cast
     */
    public static ValueArray asArray(Value value) {
        if (isArray(value)) {
            return (ValueArray) value;
        } else { 
            return null;
        }
    }

    private int size;
    
    @Override
    public abstract TypeArray getType();
    
    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof ValueArray)) {
            return false;
        }
        ValueArray other = (ValueArray) obj;
        if (this.size != other.size) {
            return false;
        }
        int totalSize = size();
        Value entryAccThis = getType().getEntryType().newValue();
        Value entryAccOther = getType().getEntryType().newValue();
        for (int entry = 0; entry < totalSize; entry++) {
            try {
                get(entryAccThis, entry);
                other.get(entryAccOther, entry);
                if (!entryAccThis.isEq(entryAccOther)) {
                    return false;
                }
            } catch (EPMCException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
    
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        Value entry = getType().getEntryType().newValue();
        builder.append("[");
        for (int entryNr = 0; entryNr < size(); entryNr++) {
        	get(entry, entryNr);
        	builder.append(entry);
        	if (entryNr < size() - 1) {
        		builder.append(",");
        	}
        }
        builder.append("]");
        return builder.toString();
    }
        
    protected abstract void setDimensionsContent();
    
    public final void setSize(int size) {
        assert !isImmutable();
        this.size = size;
        setDimensionsContent();
        Value entryAcc = getType().getEntryType().newValue();
        for (int index = 0; index < size; index++) {
            set(entryAcc, index);
        }
    }
    
    public final int size() {
        return size;
    }

    @Override
    public void set(Value op) {
        assert !isImmutable();
        ValueArray opArray = ValueArray.asArray(op);
        setSize(opArray.size());
        setDimensionsContent();
        int totalSize = opArray.size();
        Value entryAcc = getType().getEntryType().newValue();
        for (int index = 0; index < totalSize; index++) {
            opArray.get(entryAcc, index);
            set(entryAcc, index);
        }
    }    
    
    @Override
    public boolean isEq(Value other) throws EPMCException {
    	assert other != null;
    	assert isArray(other);
        ValueArray otherArray = ValueArray.asArray(other);
        if (size() != otherArray.size()) {
        	return false;
        }
        Value entryAccThis = getType().getEntryType().newValue();
        Value entryAccOther = getType().getEntryType().newValue();
        for (int entry = 0; entry < size(); entry++) {
            get(entryAccThis, entry);
            otherArray.get(entryAccOther, entry);
            if (!entryAccThis.isEq(entryAccOther)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int compareTo(Value other) {
    	assert other != null;
        assert !isImmutable();
        ValueArray opArray = ValueArray.asArray(other);
        int sizeCmp = Integer.compare(size(), opArray.size());
        if (sizeCmp != 0) {
            return sizeCmp;
        }
        Value entryAccThis = getType().getEntryType().newValue();
        Value entryAccOther = getType().getEntryType().newValue();
        int totalSize = size();
        for (int entry = 0; entry < totalSize; entry++) {
            get(entryAccThis, entry);
            opArray.get(entryAccOther, entry);
            int cmpEntry = entryAccThis.compareTo(entryAccOther);
            if (cmpEntry != 0) {
                return cmpEntry;
            }
        }
        return 0;
    }
    
    @Override
    public double distance(Value other) throws EPMCException {
        assert other != null;
        if (!isArray(other)) {
            return Double.POSITIVE_INFINITY;
        }
        ValueArray otherArray = ValueArray.asArray(other);
        if (this.size() != otherArray.size()) {
            return Double.POSITIVE_INFINITY;
        }
        ValueArray opArray = ValueArray.asArray(other);
        double maxDistance = 0.0;
        int totalSize = size();
        Value entryAccThis = getType().getEntryType().newValue();
        Value entryAccOther = opArray.getType().getEntryType().newValue();
        for (int entry = 0; entry < totalSize; entry++) {
            get(entryAccThis, entry);
            opArray.get(entryAccOther, entry);
            double entryDistance = entryAccThis.distance(entryAccOther);
            Math.max(maxDistance, entryDistance);
        }
        return maxDistance;
    }

    public abstract void get(Value presStateProb, int state);
    
    public abstract void set(Value value, int index);
}
