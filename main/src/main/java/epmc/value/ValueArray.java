package epmc.value;

import java.util.Arrays;

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

    private static final int NUM_IMPORT_VALUES = 2;
    private int[] offset;
    private int totalSize;
    private int[] dimensions;
    private final ValueArray importArrays[] = new ValueArray[NUM_IMPORT_VALUES];
    
    protected ValueArray() {
        this.offset = new int[]{1};
        this.dimensions = new int[]{0};
        this.totalSize = 0;
    }

    @Override
    public abstract TypeArray getType();
    
    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof ValueArray)) {
            return false;
        }
        ValueArray other = (ValueArray) obj;
        if (this.totalSize != other.totalSize) {
            return false;
        }
        if (!Arrays.equals(this.dimensions, other.dimensions)) {
            return false;
        }
        int totalSize = getTotalSize();
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
    public abstract ValueArray clone();
    
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        Value entry = getType().getEntryType().newValue();
        if (getNumDimensions() == 0) {
            builder.append("(zero-dimensional-array)");
        } else if (getNumDimensions() == 1) {
            builder.append("[");
            for (int entryNr = 0; entryNr < getLength(0); entryNr++) {
                get(entry, entryNr);
                builder.append(entry);
                if (entryNr < getLength(0) - 1) {
                    builder.append(",");
                }
            }
            builder.append("]");
        } else if (getNumDimensions() == 2) {
            int maxEntrySize = 0;
            for (int row = 0; row < getLength(0); row++) {
                for (int column = 0; column < getLength(1); column++) {
                    int[] indices = {row, column};
                    try {
                        get(entry, indices);
                    } catch (EPMCException e) {
                        return e.toString();
                    }
                    String entryString = entry.toString();
                    maxEntrySize = Math.max(maxEntrySize, entryString.length());
                }
            }
            builder.append("\n");
            for (int row = 0; row < getLength(0); row++) {
                builder.append("    ");
                for (int column = 0; column < getLength(1); column++) {
                    int[] indices = {row, column};
                    try {
                        get(entry, indices);
                    } catch (EPMCException e) {
                        return e.toString();
                    }
                    String entryString = entry.toString();
                    int missing = maxEntrySize - entryString.length();
                    String fill = new String(new char[missing]).replace("\0", " ");
                    entryString = fill + entryString;
                    builder.append(entryString);
                    if (column < getLength(1) - 1) {
                        builder.append("  ");
                    }
                }
                if (row < getLength(0) - 1) {
                    builder.append("\n");
                }
            }
        } else {
            builder.append(((Object) this).toString());
        }
        return builder.toString();
    }
    
    public final int[] getDimensions() {
        return dimensions;
    }
    
    public final void setDimensions(int... dimensions) {
        assert !isImmutable();
        this.dimensions = new int[dimensions.length];
        offset = new int[dimensions.length];
        System.arraycopy(dimensions, 0, this.dimensions, 0, dimensions.length);
        totalSize = 1;
        for (int index = dimensions.length - 1; index >= 0; index--) {
            offset[index] = totalSize;
            totalSize *= dimensions[index];
        }
        setDimensionsContent();
    }
    
    protected abstract void setDimensionsContent() ;
    
    public final void resize(int size) {
        assert !isImmutable();
        ValueArray newArray = getType().newValue();
        newArray.setSize(size);
        Value entry = getType().getEntryType().newValue();
        int min = Math.min(size(), size);
        for (int entryNr = 0; entryNr < min; entryNr++) {
            get(entry, entryNr);
            newArray.set(entry, entryNr);
        }
        this.set(newArray);
    }
    
    public final void setSize(int size) {
        assert !isImmutable();
        this.totalSize = size;
        this.offset = new int[]{1};
        this.dimensions = new int[]{size};
        setDimensionsContent();
        Value entryAcc = getType().getEntryType().newValue();
        for (int index = 0; index < size; index++) {
            set(entryAcc, index);
        }
    }
    
    public final int getTotalSize() {
        return totalSize;
    }
    
    public final int size() {
        assert dimensions.length == 1;
        return totalSize;
    }

    @Override
    public void set(Value op) {
        assert !isImmutable();
        ValueArray opArray = castOrImport(op, 0, false);
        setDimensions(opArray.getDimensions());
        setDimensionsContent();
        int totalSize = opArray.getTotalSize();
        Value entryAcc = getType().getEntryType().newValue();
        for (int index = 0; index < totalSize; index++) {
            opArray.get(entryAcc, index);
            set(entryAcc, index);
        }
    }    
    
    @Override
    public boolean isEq(Value other) throws EPMCException {
        ValueArray otherArray = castOrImport(other, 0, false);
        if (getNumDimensions() != otherArray.getNumDimensions()) {
            return false;
        }
        for (int dim = 0; dim < getNumDimensions(); dim++) {
            if (getLength(dim) != otherArray.getLength(dim)) {
                return false;
            }
        }
        Value entryAccThis = getType().getEntryType().newValue();
        Value entryAccOther = getType().getEntryType().newValue();
        for (int entry = 0; entry < getTotalSize(); entry++) {
            get(entryAccThis, entry);
            otherArray.get(entryAccOther, entry);
            if (!entryAccThis.isEq(entryAccOther)) {
                return false;
            }
        }
        return true;
    }
    
    protected ValueArray castOrImport(Value operand, int number, boolean multiply) {
        assert operand != null;
        assert number >= 0;
        assert number < NUM_IMPORT_VALUES;
        if (isArray(operand)) {
            return asArray(operand);
        } else if (getType().getEntryType().canImport(operand.getType())) {
            if (importArrays[number] == null) {
                importArrays[number] = (ValueArray) getType().newValue();
            }
            importArrays[number].setDimensions(getDimensions());
            if (multiply) {
                for (int entryNr = 0; entryNr < getLength(0); entryNr++) {
                    set(operand, entryNr, entryNr);
                }
            } else {
                for (int entryNr = 0; entryNr < this.totalSize; entryNr++) {
                    set(operand, entryNr);
                }                
            }
            return importArrays[number];
        } else {
            assert false : this + " " + operand;
            return null;
        }
    }
    
    @Override
    public int compareTo(Value other) {
        assert !isImmutable();
        ValueArray opArray = castOrImport(other, 0, false);
        int dimLengthCmp = Integer.compare(dimensions.length,
                opArray.dimensions.length);
        if (dimLengthCmp != 0) {
            return dimLengthCmp;
        }
        for (int i = 0; i < dimensions.length; i++) {
            int cmpDim = Integer.compare(dimensions[i], opArray.dimensions[i]);
            if (cmpDim != 0) {
                return cmpDim;
            }
        }
        Value entryAccThis = getType().getEntryType().newValue();
        Value entryAccOther = getType().getEntryType().newValue();
        int totalSize = getTotalSize();
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
        if (!Arrays.equals(this.getDimensions(), otherArray.getDimensions())) {
            return Double.POSITIVE_INFINITY;
        }
        ValueArray opArray = ValueArray.asArray(other);
        double maxDistance = 0.0;
        int totalSize = getTotalSize();
        Value entryAccThis = getType().getEntryType().newValue();
        Value entryAccOther = getType().getEntryType().newValue();
        for (int entry = 0; entry < totalSize; entry++) {
            get(entryAccThis, entry);
            opArray.get(entryAccOther, entry);
            double entryDistance = entryAccThis.distance(entryAccOther);
            Math.max(maxDistance, entryDistance);
        }
        return maxDistance;
    }

    public void get(Value value, int... indices) throws EPMCException {
        int absIndex = 0;
        for (int i = 0; i < getNumDimensions(); i++) {
            absIndex += indices[i] * offset[i];
        }
        get(value, absIndex);
    }
    
    public void set(Value entry, int... indices) {
        assert !isImmutable();
        int absIndex = 0;
        for (int i = 0; i < getNumDimensions(); i++) {
            absIndex += indices[i] * offset[i];
        }
        set(entry, absIndex);
    }
    

    public int getNumDimensions() {
        return getDimensions().length;
    }
    
    public int getLength(int i) {
        return getDimensions()[i];
    }
    
    public abstract void get(Value presStateProb, int state);
    
    public abstract void set(Value value, int index);
}
