package epmc.util;

/**
 * Represents a stream of bits.
 * 
 * @author Ernst Moritz Hahn
 */
public interface BitStream {
    /**
     * Read next bit from bit stream and increase read cursor by one.
     * 
     * @return bit read
     */
    boolean read();

    default int read(int numBits) {
        int result = 0;
        int mark = 1;
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            result |= read() ? mark : 0;
            mark <<= 1;
        }
        return result;
    }
    
    /**
     * Write next bit to bit stream and increate write cursor by one.
     * 
     * @param value bit to write
     */
    void write(boolean value);
    
    default void write(int value, int numBits) {
        int mark = 1;
        for (int bitNr = 0; bitNr < numBits; bitNr++) {
            write((value & mark) > 0);
            mark <<= 1;
        }
    }
}
