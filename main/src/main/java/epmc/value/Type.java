package epmc.value;

/**
 * Type of given {@link Value}.
 * The main usage of this class is to create new {@link Value} objects of a
 * given type using {@Link #newValue()}.
 * 
 * @author Ernst Moritz Hahn
 * @see Value
 */
public interface Type {
    /**
     * Get value context used.
     * 
     * @return value context used
     */
    ContextValue getContext();

    /**
     * Create a new value of this type.
     * 
     * @return new value of this type
     */
    Value newValue();
    
    /**
     * Check whether values of given type can be imported.
     * Importing is possible means that for the
     * {@link Value}s
     * created using
     * {@link #newValue()}
     * of this type can successfully call
     * {@link Value#set(Value)}
     * for values of the type given as parameter.
     * The type parameter must not be {@code null}.
     * 
     * @param type type of which to check whether values are importable
     * @return whether values of given type can be imported
     */
    boolean canImport(Type type);

    /**
     * Get array type for this type.
     * 
     * @return array type for this type
     */
    TypeArray getTypeArray();
}
