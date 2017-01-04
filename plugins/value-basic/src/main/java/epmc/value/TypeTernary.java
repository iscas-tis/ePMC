package epmc.value;

import epmc.value.ContextValue;
import epmc.value.Type;
import epmc.value.TypeArray;

/**
 * Type allowing to store ternary truth values.
 * 
 * @author Ernst Moritz Hahn
 */
public final class TypeTernary implements TypeEnumerable, TypeNumBitsKnown {
	static boolean isTernary(Type type) {
		return type instanceof TypeTernary;
	}
	
    /** String "ternary", for {@link #toString()}. */
    private final static String TERNARY = "ternary";
    /** Three different values: {@code true}, {@code false}, {@code unknown}. */
    final static int NUM_VALUES = 3;
    /** We need ceil(log2(NUM_VALUES)) = 2 bits to store values of this type. */
    private final static int NUM_BITS = 2;
    /** Integer representing value {@code false}. */
    final static int FALSE_NUMBER = 0;
    /** Integer representing value {@code unknown}. */
    final static int UNKNOWN_NUMBER = 1;
    /** Integer representing value {@code true}. */
    final static int TRUE_NUMBER = 2;
    
    /** Context to which this type belongs. */
    private final ContextValue context;
    /** Value storing {@code false} (made immutable in constructor). */
    private final ValueTernary valueFalse = new ValueTernary(this, Ternary.FALSE);
    /** Value storing {@code true} (made immutable in constructor). */
    private final ValueTernary valueTrue = new ValueTernary(this, Ternary.TRUE);
    /** Value storing {@code unknown} (made immutable in constructor). */
    private final ValueTernary valueUnknown = new ValueTernary(this, Ternary.UNKNOWN);

    /**
     * Construct new three-valued truth value type.
     * The value context parameter may not be {@code null}.
     * 
     * @param context value context to which the type belongs
     */
    public TypeTernary(ContextValue context) {
        assert context != null;
        this.context = context;
        valueFalse.setImmutable();
        valueTrue.setImmutable();
        valueUnknown.setImmutable();
    }
    
    @Override
    public boolean canImport(Type a) {
        assert a != null;
        return TypeBoolean.isBoolean(a);
    }

    public ValueTernary getFalse() {
        return valueFalse;
    }
    
    public ValueTernary getTrue() {
        return valueTrue;
    }
    
    public ValueTernary getUnknown() {
        return valueUnknown;
    }
        
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(TERNARY);
        return builder.toString();
    }

    @Override
    public ValueTernary newValue() {
        return new ValueTernary(this);
    }

    @Override
    public int getNumValues() {
        return NUM_VALUES;
    }
    
    @Override
    public int getNumBits() {
        return NUM_BITS;
    }
    
    @Override
    public ContextValue getContext() {
        return context;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj != null;
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        Type other = (Type) obj;
        if (this.getContext() != other.getContext()) {
            return false;
        }
        if (!canImport(other) || !other.canImport(this)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash = getClass().hashCode() + (hash << 6) + (hash << 16) - hash;
        return hash;
    }    
    
	@Override
    public TypeArray getTypeArray() {
        return getContext().makeUnique(new TypeArrayGeneric(this));
    }
}
