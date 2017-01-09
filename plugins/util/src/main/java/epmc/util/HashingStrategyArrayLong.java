package epmc.util;

import java.util.Arrays;
import gnu.trove.strategy.HashingStrategy;

/**
 * Trove hashing strategy for long arrays.
 * This strategy is used to allow hashing arrays in Trove hash maps without
 * having to use a wrapper object for the arrays. The {@link Object#hashCode()}
 * and {@link Object#equals(Object)} methods of arrays the ones of
 * {@link Object}, which are based on object identity, not object content. This
 * hash function provides hash and equality functions based on the content of
 * long arrays.
 * 
 * @author Ernst Moritz Hahn
 */
public final class HashingStrategyArrayLong implements HashingStrategy<long[]> {
    /** 1L, as I don't know any better. */
    private static final long serialVersionUID = 1L;
    /** We only need a single instance of the hash strategy. */
    private static final HashingStrategyArrayLong INSTANCE
    = new HashingStrategyArrayLong();

    @Override
    public int computeHashCode(long[] arg0) {
        assert arg0 != null;
        return Arrays.hashCode(arg0);
    }

    @Override
    public boolean equals(long[] arg0, long[] arg1) {
        assert arg0 != null;
        assert arg1 != null;
        return Arrays.equals(arg0, arg1);
    }
    
    /**
     * Get instance of the hashing strategy.
     * 
     * @return instance of the hashing strategy
     */
    public static HashingStrategyArrayLong getInstance() {
        return INSTANCE;
    }
    
    /**
     * Private constructor to ensure that only one instance is created.
     */
    private HashingStrategyArrayLong() {
    }
}
