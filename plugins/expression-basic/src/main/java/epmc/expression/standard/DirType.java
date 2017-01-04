package epmc.expression.standard;

/**
 * Enum specifying whether minimal/maximal/exists/forall values be computed.
 * 
 * @author Ernst Moritz Hahn
 */
public enum DirType {
    /** Enum for maximising values. */
    MAX("max"),
    /** Enum for minimising values. */
    MIN("min"),
    EXISTS("exists"),
    FORALL("forall"),    
    /** Enum for unspecified direction. */
    NONE("");

    /** User-readable {@link String} representing the direction. */
    private String string;

    /**
     * Construct new direction type.
     * The parameter must not be {@code null}.
     * 
     * @param string string representing direction type.
     */
    private DirType(String string) {
        assert string != null;
        this.string = string;
    }
    
    @Override
    public String toString() {
        return string;
    }
    
    /**
     * Check whether this object represents the minimising direction.
     * 
     * @return whether this object represents the minimising direction
     */
    public boolean isMin() {
        return this == MIN;
    }
    
    /**
     * Check whether this object represents the maximising direction.
     * 
     * @return whether this object represents the maximising direction
     */
    public boolean isMax() {
        return this == MAX;
    }
    
    public boolean isExists() {
        return this == EXISTS;
    }
    
    public boolean isForall() {
        return this == FORALL;
    }
    
    /**
     * Check whether this object represents the unspecified direction.
     * 
     * @return whether this object represents the unspecified direction
     */
    public boolean isNone() {
        return this == NONE;
    }
}
