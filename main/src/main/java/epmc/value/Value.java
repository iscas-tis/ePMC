package epmc.value;

import epmc.error.EPMCException;

/**
 * Interface to represent values used during analysis.
 * TODO complete documentation
 * 
 * @author Ernst Moritz Hahn
 */
public interface Value extends Comparable<Value> {
    @Override
    boolean equals(Object obj);
   
    @Override
    int hashCode();

    @Override
    String toString();

    /**
     * Get type with which this value was created.
     * 
     * @return type with which this value was created
     */
    Type getType();

    /**
     * Set value to this value.
     * The value parameter must not be {@code null}.
     * 
     * @param value value to set this value to
     */
    void set(Value value);
    
    void set(String value) throws EPMCException;

    void setImmutable();

    boolean isImmutable();

    double distance(Value other) throws EPMCException;

    boolean isEq(Value other) throws EPMCException;
}
