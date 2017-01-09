package epmc.util;

/**
 * Interface for objects which shall be written to a {@link BitStream}.
 * The purpose of this interface is thus to provide a way for very compact
 * serialization of objects, which do not store type information, but only the
 * current state of the object.
 * 
 * @author Ernst Moritz Hahn
 */
public interface BitStoreable {
    /**
     * Read the object content from given bit stream.
     * 
     * @param reader bit stream to read object from
     */
    void read(BitStream reader);
    
    /**
     * Write object to given bit stream.
     * 
     * @param writer bit stream to write object to
     */
    void write(BitStream writer);
}
