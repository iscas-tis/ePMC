package epmc.value;

// TODO complete documentation

/**
 * Type to create {@link Value}s storing several {@link Value}s of given {@link Type}.
 * 
 * @author Ernst Moritz Hahn
 */
public interface TypeArray extends Type {
    /**
     * Checks whether given type is an array type.
     * 
     * @param type type for which to check whether it is an array type
     * @return whether given type is an array type
     */
    static boolean isArray(Type type) {
        return type instanceof TypeArray;
    }
    
    /**
     * Cast given type to array type.
     * If the type is not an array type, {@code null} will be returned.
     * 
     * @param type type to cast to array type
     * @return type casted to array type, or {@null} if not possible to cast
     */
    static TypeArray asArray(Type type) {
        if (isArray(type)) {
            return (TypeArray) type;
        } else {
            return null;
        }
    }

    /**
     * Get entry type of this array type.
     * 
     * 
     * @return entry type
     */
    Type getEntryType();

    @Override
    ValueArray newValue();

    @Override
    default boolean canImport(Type type) {
        assert type != null;
        if (TypeArray.isArray(type)) {
            TypeArray other = asArray(type);
            return getEntryType().canImport(other.getEntryType());
        }
        return false;
    }
    
    @Override
    boolean equals(Object obj);
        
    @Override
    int hashCode();
    
    @Override
    String toString();
}
