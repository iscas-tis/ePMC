package epmc.util;

import java.util.Arrays;

// TODO decide whether or not this class stays in - not currently used

public class BitStack {
    private static final int LOG2LONGSIZE = 6;
    private final int INIT_NUM_BITS = 1024;
    private long[] bits;
    private int size;
    
    public BitStack() {
        int numLongs = ((INIT_NUM_BITS - 1) >> LOG2LONGSIZE) + 1;
        this.bits = new long[numLongs];
    }
    
    public void push(boolean value) {
        // overflow check (Integer.MAX_VALUE -> Integer.MIN_VALUE)
        assert size >= 0;
        int offset = size >> LOG2LONGSIZE;
        if (offset >= bits.length) {
            bits = Arrays.copyOf(bits, bits.length * 2);
        }
        if (value) {
            bits[offset] |= 1L << size;
        } else {
            bits[offset] &= ~(1L << size);
        }
        size++;
    }
    
    public boolean pop() {
        assert size > 0 || size == Integer.MIN_VALUE;
        size--;
        int offset = size >> 6;
        return (bits[offset] & (1L << size)) != 0;
    }
}
